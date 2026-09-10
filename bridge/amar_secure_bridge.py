"""AMAR secure bridge entrypoint.

Extends the hardened read-only bridge with /commands. Live execution is
FAIL-CLOSED and disabled unless AMAR_LIVE_EXECUTION=1 is explicitly enabled.
Every command is authenticated, HMAC signed, time bounded, replay protected,
account scoped, BOT-MAGIC scoped and symbol allow-listed before MT5 execution.
"""
import json
import os

import MetaTrader5 as mt5

from amar_mt5_bridge import Handler, HOST, PORT, CERT, KEY, TOKEN, BOT_MAGIC, json_response, mt5_ready
from amar_command_channel import validate

LIVE_ENABLED = os.environ.get("AMAR_LIVE_EXECUTION", "0") == "1"
ALLOWED_SYMBOLS = frozenset(x.strip() for x in os.environ.get("AMAR_ALLOWED_SYMBOLS", "").split(",") if x.strip())


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
    quantity = float(command.get("quantity", 0.0))
    if side not in ("BUY", "SELL") or quantity <= 0 or quantity != quantity:
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
        "action": mt5.TRADE_ACTION_DEAL,
        "symbol": symbol,
        "volume": normalized,
        "type": order_type,
        "price": price,
        "deviation": int(os.environ.get("AMAR_MAX_DEVIATION_POINTS", "20")),
        "magic": BOT_MAGIC,
        "comment": "AMAR_B31",
        "type_time": mt5.ORDER_TIME_GTC,
        "type_filling": mt5.ORDER_FILLING_RETURN,
    }
    result = mt5.order_send(request)
    if result is None:
        return False, False, payload["requestId"], "لم تصل نتيجة تنفيذ من MT5"
    executed = result.retcode == mt5.TRADE_RETCODE_DONE
    return executed, executed, payload["requestId"], f"نتيجة MT5: {result.retcode}"


class SecureHandler(Handler):
    def do_POST(self):
        if self.path.split("?", 1)[0] != "/commands":
            return json_response(self, 404, {"ok": False, "message": "not found"})
        if not TOKEN or self.headers.get("Authorization", "").strip() != f"Bearer {TOKEN}":
            return json_response(self, 401, {"ok": False, "accepted": False, "message": "unauthorized"})
        length = int(self.headers.get("Content-Length", "0"))
        if length <= 0 or length > 32_768:
            return json_response(self, 413, {"ok": False, "accepted": False, "message": "payload too large"})
        try:
            payload = json.loads(self.rfile.read(length).decode("utf-8"))
            account = mt5.account_info() if mt5_ready() else None
            expected_login = account.login if account else -1
            symbol = str(payload.get("symbol", ""))
            if not ALLOWED_SYMBOLS or symbol not in ALLOWED_SYMBOLS:
                return json_response(self, 403, {"ok": False, "accepted": False, "requestId": payload.get("requestId", ""), "message": "الرمز غير مصرح به"})
            valid, message = validate(payload, expected_login, BOT_MAGIC, symbol)
            if not valid:
                return json_response(self, 403, {"ok": False, "accepted": False, "requestId": payload.get("requestId", ""), "message": message})
            executed, accepted, request_id, result_message = execute_market_command(payload)
            return json_response(self, 200 if accepted else 409, {
                "accepted": accepted, "executed": executed,
                "requestId": request_id, "message": result_message,
            })
        except (ValueError, TypeError, json.JSONDecodeError):
            return json_response(self, 400, {"ok": False, "accepted": False, "message": "invalid command"})
        except Exception as exc:
            return json_response(self, 500, {"ok": False, "accepted": False, "message": f"command failure: {type(exc).__name__}"})


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
    print(f"AMAR secure bridge listening on https://{HOST}:{PORT}; live={LIVE_ENABLED}")
    server.serve_forever()


if __name__ == "__main__":
    main()
