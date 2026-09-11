"""AMAR secure bridge entrypoint.

Authenticated BOT 1 lifecycle channel: bearer access + HMAC + TTL/clock
validation + account/magic scope + source/target symbol allow-lists +
replay/idempotency + single-flight MT5 queue + terminal ACK verification.
"""
import fnmatch
import hmac
import json
import os
import re
from pathlib import Path

import MetaTrader5 as mt5

from amar_mt5_bridge import Handler, HOST, PORT, CERT, KEY, TOKEN, BOT_MAGIC, json_response, mt5_ready
from amar_command_channel import validate, REPLAY_STORE, require_live_replay_ledger
from amar_bot1_secure_channel import validate as validate_bot1, canonical as canonical_bot1
from amar_bot1_file_queue import enqueue as enqueue_bot1, QueueBusyError, ACK_FILE

LIVE_ENABLED = os.environ.get("AMAR_LIVE_EXECUTION", "0") == "1"
require_live_replay_ledger(LIVE_ENABLED)
ALLOWED_SYMBOLS = frozenset(x.strip() for x in os.environ.get("AMAR_ALLOWED_SYMBOLS", "").split(",") if x.strip())
ALLOWED_TARGET_SYMBOLS = frozenset(x.strip() for x in os.environ.get("AMAR_ALLOWED_TARGET_SYMBOLS", "").split(",") if x.strip()) or ALLOWED_SYMBOLS
ALLOWED_TARGET_PATTERNS = frozenset(x.strip() for x in os.environ.get("AMAR_ALLOWED_TARGET_PATTERNS", "").split(",") if x.strip())
COMMON_FILES_DIR = os.environ.get("AMAR_MT5_COMMON_FILES_DIR", "").strip()
MAX_BODY_BYTES = min(max(int(os.environ.get("AMAR_MAX_BODY_BYTES", "32768")), 1024), 262144)
MAX_STATE_AGE_MS = max(int(os.environ.get("AMAR_BOT1_STATE_MAX_AGE_MS", "5000")), 1000)
REQUEST_ID_RE = re.compile(r"^[A-Za-z0-9._:-]{1,128}$")
STATE_FILE = "AMAR_BOT1_STATE.json"


def _authorized_account():
    if not mt5_ready():
        return None
    return mt5.account_info()


def _target_allowed(symbol: str) -> bool:
    return symbol in ALLOWED_TARGET_SYMBOLS or any(fnmatch.fnmatchcase(symbol, pattern) for pattern in ALLOWED_TARGET_PATTERNS)


def _read_ack_for_request(request_id: str) -> dict:
    """Read the newest matching ACK, not merely the last ACK in the ledger."""
    if not COMMON_FILES_DIR:
        return {"status": "UNAVAILABLE", "request_id": request_id}
    path = Path(COMMON_FILES_DIR) / ACK_FILE
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
        for raw in reversed(lines[-256:]):
            try:
                value = json.loads(raw)
            except json.JSONDecodeError:
                continue
            if isinstance(value, dict) and str(value.get("request_id", "")) == request_id:
                return value
        return {"status": "PENDING", "request_id": request_id}
    except (OSError, UnicodeError):
        return {"status": "PENDING", "request_id": request_id}


def _read_bot1_state() -> dict:
    if not COMMON_FILES_DIR:
        return {"available": False, "reason": "queue_not_configured"}
    path = Path(COMMON_FILES_DIR) / STATE_FILE
    try:
        state = json.loads(path.read_text(encoding="utf-8"))
        if not isinstance(state, dict):
            return {"available": False, "reason": "invalid_state"}
        heartbeat = int(state.get("heartbeat_ms", 0) or 0)
        age = max(0, int(__import__("time").time() * 1000) - heartbeat) if heartbeat else None
        state["available"] = True
        state["age_ms"] = age
        state["fresh"] = bool(age is not None and age <= MAX_STATE_AGE_MS)
        if not state["fresh"]:
            state["runtime_health"] = "STALE"
        return state
    except (OSError, UnicodeError, json.JSONDecodeError, TypeError, ValueError):
        return {"available": False, "reason": "state_unavailable"}


def _discover_target_symbols():
    if not mt5_ready():
        return []
    items = mt5.symbols_get() or ()
    discovered = []
    for item in items:
        name = str(getattr(item, "name", ""))
        if not name or not _target_allowed(name):
            continue
        trade_mode = getattr(item, "trade_mode", None)
        if trade_mode is not None and int(trade_mode) == 0:
            continue
        discovered.append({
            "symbol": name,
            "visible": bool(getattr(item, "visible", False)),
            "path": str(getattr(item, "path", "")),
            "digits": int(getattr(item, "digits", 0)),
            "point": float(getattr(item, "point", 0.0)),
            "tradeMode": int(trade_mode) if trade_mode is not None else None,
        })
    return sorted(discovered, key=lambda x: x["symbol"].upper())


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
        presented = self.headers.get("Authorization", "").strip()
        expected = f"Bearer {TOKEN}" if TOKEN else ""
        return bool(expected and hmac.compare_digest(presented, expected))

    def _body(self):
        content_type = self.headers.get("Content-Type", "").split(";", 1)[0].strip().lower()
        if content_type != "application/json":
            raise ValueError("application/json required")
        try:
            length = int(self.headers.get("Content-Length", "0"))
        except ValueError:
            length = 0
        if length <= 0 or length > MAX_BODY_BYTES:
            raise ValueError("payload too large")
        raw = self.rfile.read(length)
        if len(raw) != length:
            raise ValueError("incomplete payload")
        return json.loads(raw.decode("utf-8"))

    def do_POST(self):
        path = self.path.split("?", 1)[0]
        if not self._authorized():
            return json_response(self, 401, {"ok": False, "accepted": False, "message": "unauthorized"})
        try:
            payload = self._body()
            if not isinstance(payload, dict):
                raise ValueError("object payload required")
            account = _authorized_account()
            expected_login = account.login if account else -1

            if path == "/bot1/commands":
                if not LIVE_ENABLED:
                    return json_response(self, 423, {"ok": False, "accepted": False, "executed": False, "requestId": payload.get("request_id", ""), "message": "BOT 1 live control is locked"})
                if not COMMON_FILES_DIR:
                    return json_response(self, 503, {"ok": False, "accepted": False, "executed": False, "requestId": payload.get("request_id", ""), "message": "MT5 common-files directory is not configured"})
                valid, message = validate_bot1(payload, expected_login, BOT_MAGIC, set(ALLOWED_SYMBOLS), set(ALLOWED_TARGET_SYMBOLS), set(ALLOWED_TARGET_PATTERNS))
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
            return json_response(self, 200 if accepted else 409, {"accepted": accepted, "executed": executed, "requestId": request_id, "message": result_message})
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
            if not REQUEST_ID_RE.fullmatch(request_id):
                return json_response(self, 400, {"ok": False, "message": "invalid request id"})
            return json_response(self, 200, {"ok": True, **_read_ack_for_request(request_id)})
        if path == "/bot1/symbols":
            if not self._authorized():
                return json_response(self, 401, {"ok": False, "message": "unauthorized"})
            if not mt5_ready():
                return json_response(self, 503, {"ok": False, "message": "MT5 unavailable"})
            return json_response(self, 200, {"ok": True, "items": _discover_target_symbols()})
        if path == "/bot1/state":
            if not self._authorized():
                return json_response(self, 401, {"ok": False, "message": "unauthorized"})
            state = _read_bot1_state()
            return json_response(self, 200 if state.get("fresh") else 503, {"ok": bool(state.get("fresh")), **state})
        if path == "/bot1/health":
            if not self._authorized():
                return json_response(self, 401, {"ok": False, "message": "unauthorized"})
            account = _authorized_account()
            state = _read_bot1_state()
            return json_response(self, 200, {"ok": bool(account), "connected": bool(account), "login": int(account.login) if account else 0, "live": LIVE_ENABLED, "queueConfigured": bool(COMMON_FILES_DIR), "symbolDiscovery": bool(ALLOWED_TARGET_SYMBOLS or ALLOWED_TARGET_PATTERNS), "botStateFresh": bool(state.get("fresh"))})
        return super().do_GET()
