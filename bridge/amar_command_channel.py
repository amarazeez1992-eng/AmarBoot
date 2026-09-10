"""B31 secure command-channel primitives for the AMAR MT5 bridge.

Execution remains FAIL-CLOSED. The module provides validation, replay protection,
idempotency and BOT-1 scope checks; the HTTP bridge keeps live execution disabled
unless AMAR_LIVE_EXECUTION=1 is explicitly configured on the host.
"""
import hashlib
import hmac
import json
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
            expired = [k for k, v in self._items.items() if v <= now]
            for k in expired:
                self._items.pop(k, None)
            if key in self._items:
                return False
            self._items[key] = expires_at_ms
            self._items.move_to_end(key)
            while len(self._items) > self.capacity:
                self._items.popitem(last=False)
            return True

REPLAY_STORE = CommandReplayStore()

def canonical(payload):
    fields = [
        payload.get("requestId", ""), payload.get("idempotencyKey", ""),
        payload.get("nonce", ""), str(payload.get("issuedAtMs", "")),
        str(payload.get("expiresAtMs", "")), str(payload.get("accountLogin", "")),
        str(payload.get("botMagic", "")), payload.get("symbol", ""),
        json.dumps(payload.get("command", {}), sort_keys=True, separators=(",", ":")),
    ]
    return "|".join(fields)

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
    now = int(time.time() * 1000)
    issued = int(payload["issuedAtMs"]); expires = int(payload["expiresAtMs"])
    if issued > now + MAX_CLOCK_SKEW_MS or expires <= now or expires - issued > COMMAND_TTL_MS:
        return False, "expired command"
    if not valid_signature(payload):
        return False, "invalid signature"
    if int(payload["accountLogin"]) != int(expected_login) or int(payload["botMagic"]) != int(expected_magic) or payload["symbol"] != expected_symbol:
        return False, "invalid execution scope"
    if not REPLAY_STORE.claim(str(payload["nonce"]), expires):
        return False, "replayed command"
    return True, "accepted"
