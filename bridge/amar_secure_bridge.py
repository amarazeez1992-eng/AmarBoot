"""AMAR secure bridge entrypoint.

Extends the read-only bridge with /commands and the authenticated BOT 1
lifecycle channel. Live execution is fail-closed: a command must pass bearer
access, HMAC, TTL/clock checks, account+magic scope, symbol allow-listing and
replay/idempotency gates before entering the MT5 FILE_COMMON queue.
"""
import json
import os
from pathlib import Path

import MetaTrader5 as mt5

from amar_mt5_bridge import Handler, HOST, PORT, CERT, KEY, TOKEN, BOT_MAGIC, json_response, mt5_ready
from amar_command_channel import validate, REPLAY_STORE
from amar_bot1_secure_channel import validate as validate_bot1, canonical as canonical_bot1
from amar_bot1_file_queue import enqueue as enqueue_bot1, QueueBusyError, ACK_FILE

LIVE_ENABLED = os.environ.get("AMAR_LIVE_EXECUTION", "0") == "1"
ALLOWED_SYMBOLS = frozenset(x.strip() for x in os.environ.get("AMAR_ALLOWED_SYMBOLS", "").split(",") if x.strip())
COMMON_FILES_DIR = os.environ.get("AMAR_MT5_COMMON_FILES_DIR", "").strip()


def _authorized_account():
    if not mt5_ready():
        return None
    return mt5.account_info()


def _read_latest_ack(request_id: str) -> dict:
    if not COMMON_FILES_DIR:
        return {"status": "UNAVAILABLE", "request_id": request_id}
    path = Path(COMMON_FILES_DIR) / ACK_FILE
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
        if not lines:
            return {"status": "PENDING", "request_id": request_id}
        value = json.loads(lines[-1])
        if not isinstance(value, dict):
            return {"status": "PENDING", "request_id": request_id}
        if str(value.get("request_id", "")) != request_id:
            return {"status": "PENDING", "request_id": request_id}
        return value
    except (OSError, UnicodeError, json.JSONDecodeError):
        return {"status": "PENDING", "request_id": request_id}


def execute_market_command(payload):
    if not LIVE_ENABLED:
        return False, False, payload.get("requestId", ""), "التنفيذ المباشر مقفول على الخادم"
    if not mt5_ready():
        return False, False, payload.get("requestId", ""), "منصة MT5 غير جاهزة"
    account = mt5.account_info()
    if account is None:
        return False, False, payload.get("requestId", ""), "الحساب غير متاح"
    symbol = str(payload["symbol"])
    if not ALLOWED_SYMBOLS or symbol not in ALLOWED_SYMBOLS:
        return False, False, payload["requestId"], "الرمز غير مصرح به"
    command = payload["command"]
    side = str(command.get("side", "")).upper()
    try:
        quantity = float(command.get("quantity", 0.0))
    except (TypeError, ValueError):
        return False, False, payload["requestId"], "حجم التداول غير صالح"
    if side not in ("BUY", "SELL") or quantity <= 0 or not quantity == quantity:
        return False, False, payload["requestId"], "أمر تداول غير صالح"
    if command.get("price") is not None:
        return False, False, payload["requestId"], "هذا المسار يدعم تنفيذ السوق فقط"
    if not mt5.symbol_select(symbol, True):
        return False, False, payload["requestId"], "الرمز غير متاح"
    info = mt5.symbol_info(symbol)
    tick = mt5.symbol_info_tick(symbol)
    if info is None or tick is None or tick.bid <= 0 or tick.ask <= 0:
        return False, False, payload["requestId"], "سعر السوق غير متاح"
    step = float(info.volume_step or 0.0)
    minimum = float(info.volume_min or 0.0)
    maximum = float(info.volume_max or 0.0)
    if step <= 0 or quantity < minimum or quantity > maximum:
        return False, False, payload["requestId"], "حجم التداول خارج حدود الوسيط"
    normalized = round(round(quantity / step) * step, 8)
    if normalized < minimum or normalized > maximum:
        return False, False, payload["requestId"], "حجم التداول لا يطابق خطوة الوسيط"
    order_type = mt5.ORDER_TYPE_BUY if side == "BUY" else mt5.ORDER_TYPE_SELL
    price = tick.ask if side == "BUY" else tick.bid
    request = {
        "action": mt5.TRADE_ACTION_DEAL, "symbol": symbol, "volume": normalized,
        "type": order_type, "price": price,
        "deviation": int(os.environ.get("AMAR_MAX_DEVIATION_POINTS", "20")),
        "magic": BOT_MAGIC, "comment": "AMAR_B31",
        "type_time": mt5.ORDER_TIME_GTC, "type_filling": mt5.ORDER_FILLING_RETURN,
    }
    result = mt5.order_send(request)
    if result is None:
        return False, False, payload["requestId"], "لم تصل نتيجة تنفيذ من MT5"
    executed = result.retcode == mt5.TRADE_RETCODE_DONE
    return executed, executed, payload["requestId"], f"نتيجة MT5: {result.retcode}"


class SecureHandler(Handler):
    def _authorized(self):
        return bool(TOKEN and self.headers.get("Authorization", "").strip() == f"Bearer {TOKEN}")

    def _body(self):
        try:
            length = int(self.headers.get("Content-Length", "0"))
        except ValueError:
            length = 0
        if length <= 0 or length > 32_768:
            raise ValueError("payload too large")
        return json.loads(self.rfile.read(length).decode("utf-8"))

    def do_POST(self):
        path = self.path.split("?", 1)[0]
        if not self._authorized():
            return json_response(self, 401, {"ok": False, "accepted": False, "message": "unauthorized"})
        try:
            payload = self._body()
            account = _authorized_account()
            expected_login = account.login if account else -1

            if path == "/bot1/commands":
                if not LIVE_ENABLED:
                    return json_response(self, 423, {"ok": False, "accepted": False, "executed": False, "requestId": payload.get("request_id", ""), "message": "BOT 1 live control is locked"})
                if not COMMON_FILES_DIR:
                    return json_response(self, 503, {"ok": False, "accepted": False, "executed": False, "requestId": payload.get("request_id", ""), "message": "MT5 common-files directory is not configured"})
                valid, message = validate_bot1(payload, expected_login, BOT_MAGIC, set(ALLOWED_SYMBOLS))
                if not valid:
                    return json_response(self, 403, {"ok": False, "accepted": False, "executed": False, "requestId": payload.get("request_id", ""), "message": message})
                expires = int(payload["expires_at_ms"])
                nonce_key = f"bot1-nonce:{payload['nonce']}"
                idem_key = f"bot1-idempotency:{payload['idempotency_key']}"
                if not REPLAY_STORE.claim_nonce(nonce_key, expires):
                    return json_response(self, 409, {"ok": False, "accepted": False, "executed": False, "requestId": payload["request_id"], "message": "replayed command"})
                if not REPLAY_STORE.claim_idempotency(idem_key, canonical_bot1(payload), expires):
                    return json_response(self, 409, {"ok": False, "accepted": False, "executed": False, "requestId": payload["request_id"], "message": "duplicate command"})
                try:
                    enqueue_bot1(json.dumps(payload, separators=(",", ":"), sort_keys=True, allow_nan=False), COMMON_FILES_DIR)
                except QueueBusyError as exc:
                    REPLAY_STORE.release_idempotency(idem_key)
                    return json_response(self, 409, {"ok": False, "accepted": False, "executed": False, "requestId": payload["request_id"], "message": str(exc)})
                except Exception as exc:
                    REPLAY_STORE.release_idempotency(idem_key)
                    return json_response(self, 500, {"ok": False, "accepted": False, "executed": False, "requestId": payload["request_id"], "message": f"queue failure: {type(exc).__name__}"})
                return json_response(self, 202, {"ok": True, "accepted": True, "executed": False, "stage": "QUEUED", "requestId": payload["request_id"], "message": "BOT 1 command queued for MT5 verification"})

            if path != "/commands":
                return json_response(self, 404, {"ok": False, "message": "not found"})

            account_symbol = str(payload.get("symbol", ""))
            if not ALLOWED_SYMBOLS or account_symbol not in ALLOWED_SYMBOLS:
                return json_response(self, 403, {"ok": False, "accepted": False, "requestId": payload.get("requestId", ""), "message": "الرمز غير مصرح به"})
            valid, message = validate(payload, expected_login, BOT_MAGIC, account_symbol)
            if not valid:
                return json_response(self, 403, {"ok": False, "accepted": False, "requestId": payload.get("requestId", ""), "message": message})
            executed, accepted, request_id, result_message = execute_market_command(payload)
            if not executed:
                REPLAY_STORE.release_idempotency(f"idempotency:{payload['idempotencyKey']}")
            return json_response(self, 200 if accepted else 409, {
                "accepted": accepted, "executed": executed,
                "requestId": request_id, "message": result_message,
            })
        except (ValueError, TypeError, json.JSONDecodeError):
            return json_response(self, 400, {"ok": False, "accepted": False, "message": "invalid command"})
        except Exception as exc:
            return json_response(self, 500, {"ok": False, "accepted": False, "message": f"command failure: {type(exc).__name__}"})

    def do_GET(self):
        path = self.path.split("?", 1)[0]
        if path.startswith("/bot1/commands/"):
            if not self._authorized():
                return json_response(self, 401, {"ok": False, "message": "unauthorized"})
            request_id = path.rsplit("/", 1)[-1].strip()
            if not request_id or len(request_id) > 128 or any(c in request_id for c in "/\\\r\n"):
                return json_response(self, 400, {"ok": False, "message": "invalid request id"})
            return json_response(self, 200, {"ok": True, **_read_latest_ack(request_id)})

        if path == "/bot1/health":
            if not self._authorized():
                return json_response(self, 401, {"ok": False, "message": "unauthorized"})
            account = _authorized_account()
            return json_response(self, 200, {
                "ok": bool(account), "connected": bool(account),
                "login": int(account.login) if account else 0,
                "live": LIVE_ENABLED, "queueConfigured": bool(COMMON_FILES_DIR),
            })
        return super().do_GET()


def main():
    if not TOKEN or not CERT or not KEY:
        raise SystemExit("AMAR_BRIDGE_TOKEN, AMAR_TLS_CERT and AMAR_TLS_KEY are required")
    if not os.environ.get("AMAR_COMMAND_SIGNING_SECRET"):
        raise SystemExit("AMAR_COMMAND_SIGNING_SECRET is required")
    if not ALLOWED_SYMBOLS:
        raise SystemExit("AMAR_ALLOWED_SYMBOLS is required")
    from http.server import ThreadingHTTPServer
    import ssl
    server = ThreadingHTTPServer((HOST, PORT), SecureHandler)
    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    context.minimum_version = ssl.TLSVersion.TLSv1_2
    context.load_cert_chain(certfile=CERT, keyfile=KEY)
    server.socket = context.wrap_socket(server.socket, server_side=True)
    print(f"AMAR secure bridge listening on https://{HOST}:{PORT}; live={LIVE_ENABLED}; bot1_queue={bool(COMMON_FILES_DIR)}")
    server.serve_forever()


if __name__ == "__main__":
    main()
