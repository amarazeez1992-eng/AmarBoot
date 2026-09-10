"""B31 secure command-channel primitives for the AMAR MT5 bridge.

Execution remains FAIL-CLOSED. Validation covers authentication, HMAC integrity,
time bounds, replay protection, idempotency and account/BOT/symbol scope.
"""
import hashlib
import hmac
import os
import threading
import time
from collections import OrderedDict

MAX_CLOCK_SKEW_MS = int(os.environ.get("AMAR_COMMAND_MAX_SKEW_MS", "30000"))
COMMAND_TTL_MS = int(os.environ.get("AMAR_COMMAND_TTL_MS", "15000"))
MAX_CACHE = int(os.environ.get("AMAR_COMMAND_CACHE_SIZE", "4096"))
SIGNING_SECRET = os.environ.get("AMAR_COMMAND_SIGNING_SECRET", "")

class CommandReplayStore:
    def __init__(self, capacity=MAX_CACHE):
        if capacity < 128:
            raise ValueError("capacity too small")
        self.capacity = capacity
        self._items = OrderedDict()
        self._lock = threading.Lock()

    def claim(self, key, expires_at_ms):
        now = int(time.time() * 1000)
        with self._lock:
            for existing, expiry in list(self._items.items()):
                if expiry <= now:
                    self._items.pop(existing, None)
            if key in self._items:
                return False
            self._items[key] = expires_at_ms
            self._items.move_to_end(key)
            while len(self._items) > self.capacity:
                self._items.popitem(last=False)
            return True

REPLAY_STORE = CommandReplayStore()

def canonical(payload):
    command = payload.get("command") or {}
    fields = [
        payload.get("requestId", ""), payload.get("idempotencyKey", ""),
        payload.get("nonce", ""), str(payload.get("issuedAtMs", "")),
        str(payload.get("expiresAtMs", "")), str(payload.get("accountLogin", "")),
        str(payload.get("botMagic", "")), payload.get("symbol", ""),
        command.get("requestId", ""), command.get("side", ""),
        command.get("quantity", ""), "" if command.get("price") is None else command.get("price"),
    ]
    return "|".join(str(x) for x in fields)

def valid_signature(payload):
    if not SIGNING_SECRET:
        return False
    expected = hmac.new(SIGNING_SECRET.encode(), canonical(payload).encode(), hashlib.sha256).hexdigest()
    return hmac.compare_digest(expected, str(payload.get("signature", "")))

def validate(payload, expected_login, expected_magic, expected_symbol):
    if not isinstance(payload, dict):
        return False, "invalid request"
    required = ("requestId", "idempotencyKey", "nonce", "issuedAtMs", "expiresAtMs", "accountLogin", "botMagic", "symbol", "command", "signature")
    if any(not payload.get(k) and payload.get(k) != 0 for k in required):
        return False, "missing command fields"
    command = payload.get("command")
    if not isinstance(command, dict) or command.get("requestId") != payload.get("requestId"):
        return False, "invalid command identity"
    now = int(time.time() * 1000)
    try:
        issued = int(payload["issuedAtMs"]); expires = int(payload["expiresAtMs"])
        account_login = int(payload["accountLogin"]); bot_magic = int(payload["botMagic"])
    except (ValueError, TypeError):
        return False, "invalid numeric fields"
    if issued > now + MAX_CLOCK_SKEW_MS or expires <= now or expires - issued > COMMAND_TTL_MS:
        return False, "expired command"
    if not valid_signature(payload):
        return False, "invalid signature"
    if account_login != int(expected_login) or bot_magic != int(expected_magic) or payload["symbol"] != expected_symbol:
        return False, "invalid execution scope"
    if not REPLAY_STORE.claim(f"nonce:{payload['nonce']}", expires):
        return False, "replayed command"
    if not REPLAY_STORE.claim(f"idempotency:{payload['idempotencyKey']}", expires):
        return False, "duplicate command"
    return True, "accepted"
