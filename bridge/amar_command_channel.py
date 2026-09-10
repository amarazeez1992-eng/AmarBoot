"""B31 secure command-channel primitives for the AMAR MT5 bridge.

Validation is fail-closed. Canonicalization deliberately matches Android's
BigDecimal.toPlainString representation so signatures are language-stable.
Nonce replay and idempotency are tracked separately; a failed broker execution
can release its idempotency reservation so a caller can safely retry with the
same idempotency key and a fresh nonce.
"""
import hashlib
import hmac
import os
import threading
import time
from collections import OrderedDict
from decimal import Decimal, InvalidOperation

MAX_CLOCK_SKEW_MS = int(os.environ.get("AMAR_COMMAND_MAX_SKEW_MS", "30000"))
COMMAND_TTL_MS = int(os.environ.get("AMAR_COMMAND_TTL_MS", "15000"))
MAX_CACHE = int(os.environ.get("AMAR_COMMAND_CACHE_SIZE", "4096"))
SIGNING_SECRET = os.environ.get("AMAR_COMMAND_SIGNING_SECRET", "")


def decimal_string(value):
    try:
        d = Decimal(str(value))
    except (InvalidOperation, ValueError, TypeError):
        return str(value)
    if not d.is_finite():
        return str(value)
    text = format(d, "f")
    if "." in text:
        text = text.rstrip("0").rstrip(".")
    return text or "0"


class CommandReplayStore:
    def __init__(self, capacity=MAX_CACHE):
        if capacity < 128:
            raise ValueError("capacity too small")
        self.capacity = capacity
        self._nonces = OrderedDict()
        self._idempotency = OrderedDict()
        self._lock = threading.Lock()

    def _purge(self, now):
        for store in (self._nonces, self._idempotency):
            for key, item in list(store.items()):
                expiry = item if isinstance(item, int) else item[0]
                if expiry <= now:
                    store.pop(key, None)

    def claim_nonce(self, key, expires_at_ms):
        now = int(time.time() * 1000)
        with self._lock:
            self._purge(now)
            if key in self._nonces:
                return False
            self._nonces[key] = expires_at_ms
            self._nonces.move_to_end(key)
            while len(self._nonces) > self.capacity:
                self._nonces.popitem(last=False)
            return True

    def claim_idempotency(self, key, fingerprint, expires_at_ms):
        now = int(time.time() * 1000)
        with self._lock:
            self._purge(now)
            existing = self._idempotency.get(key)
            if existing is not None:
                return existing[1] == fingerprint and False
            self._idempotency[key] = (expires_at_ms, fingerprint)
            self._idempotency.move_to_end(key)
            while len(self._idempotency) > self.capacity:
                self._idempotency.popitem(last=False)
            return True

    def release_idempotency(self, key):
        with self._lock:
            self._idempotency.pop(key, None)


REPLAY_STORE = CommandReplayStore()


def canonical(payload):
    command = payload.get("command") or {}
    fields = [
        payload.get("requestId", ""), payload.get("idempotencyKey", ""),
        payload.get("nonce", ""), str(payload.get("issuedAtMs", "")),
        str(payload.get("expiresAtMs", "")), str(payload.get("accountLogin", "")),
        str(payload.get("botMagic", "")), payload.get("symbol", ""),
        command.get("requestId", ""), command.get("side", ""),
        decimal_string(command.get("quantity", "")),
        "" if command.get("price") is None else decimal_string(command.get("price")),
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
    if not REPLAY_STORE.claim_nonce(f"nonce:{payload['nonce']}", expires):
        return False, "replayed command"
    if not REPLAY_STORE.claim_idempotency(f"idempotency:{payload['idempotencyKey']}", canonical(payload), expires):
        return False, "duplicate command"
    return True, "accepted"
