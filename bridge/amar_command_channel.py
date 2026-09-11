"""B31/B49 secure command-channel primitives for the AMAR MT5 bridge.

Validation is fail-closed. Canonicalization deliberately matches Android's
BigDecimal.toPlainString representation so signatures are language-stable.
Nonce replay and idempotency are tracked separately; a failed broker execution
can release its idempotency reservation so a caller can safely retry with the
same idempotency key and a fresh nonce.

B49 adds an optional durable replay ledger. When
AMAR_COMMAND_REPLAY_LEDGER_PATH is configured, replay state is persisted with
atomic replace + fsync and ledger corruption/write failures fail closed.
Live execution additionally requires an explicitly configured absolute ledger
path so restart-safe replay protection cannot be accidentally omitted.
"""
import hashlib
import hmac
import json
import os
import tempfile
import threading
import time
from collections import OrderedDict
from decimal import Decimal, InvalidOperation

MAX_CLOCK_SKEW_MS = int(os.environ.get("AMAR_COMMAND_MAX_SKEW_MS", "30000"))
COMMAND_TTL_MS = int(os.environ.get("AMAR_COMMAND_TTL_MS", "15000"))
MAX_CACHE = int(os.environ.get("AMAR_COMMAND_CACHE_SIZE", "4096"))
SIGNING_SECRET = os.environ.get("AMAR_COMMAND_SIGNING_SECRET", "")
REPLAY_LEDGER_PATH = os.environ.get("AMAR_COMMAND_REPLAY_LEDGER_PATH", "").strip()


def require_live_replay_ledger(live_enabled: bool) -> None:
    """Fail closed when live execution lacks restart-durable replay storage."""
    if not live_enabled:
        return
    if not REPLAY_LEDGER_PATH:
        raise RuntimeError("AMAR live execution requires AMAR_COMMAND_REPLAY_LEDGER_PATH")
    if not os.path.isabs(REPLAY_LEDGER_PATH):
        raise RuntimeError("AMAR replay ledger path must be absolute in live mode")


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
    def __init__(self, capacity=MAX_CACHE, persistence_path=REPLAY_LEDGER_PATH):
        if capacity < 128:
            raise ValueError("capacity too small")
        self.capacity = capacity
        self.persistence_path = persistence_path
        self._nonces = OrderedDict()
        self._idempotency = OrderedDict()
        self._sequences = OrderedDict()
        self._lock = threading.Lock()
        self._load()

    def _purge(self, now):
        changed = False
        for store in (self._nonces, self._idempotency, self._sequences):
            for key, item in list(store.items()):
                expiry = item if isinstance(item, int) else item[0]
                if expiry <= now:
                    store.pop(key, None)
                    changed = True
        return changed

    def _serialize(self):
        return {
            "version": 1,
            "nonces": list(self._nonces.items()),
            "idempotency": list(self._idempotency.items()),
            "sequences": list(self._sequences.items()),
        }

    def _load(self):
        if not self.persistence_path:
            return
        try:
            with open(self.persistence_path, "r", encoding="utf-8") as handle:
                payload = json.load(handle)
            if payload.get("version") != 1:
                raise ValueError("unsupported replay ledger version")
            self._nonces = OrderedDict(payload.get("nonces", []))
            self._idempotency = OrderedDict(
                (key, (int(value[0]), str(value[1])))
                for key, value in payload.get("idempotency", [])
            )
            self._sequences = OrderedDict(
                (key, (int(value[0]), int(value[1])))
                for key, value in payload.get("sequences", [])
            )
            self._purge(int(time.time() * 1000))
            self._trim()
        except FileNotFoundError:
            return
        except (OSError, ValueError, TypeError, json.JSONDecodeError) as exc:
            raise RuntimeError("AMAR replay ledger cannot be trusted") from exc

    def _trim(self):
        for store in (self._nonces, self._idempotency, self._sequences):
            while len(store) > self.capacity:
                store.popitem(last=False)

    def _persist(self):
        if not self.persistence_path:
            return
        directory = os.path.dirname(os.path.abspath(self.persistence_path)) or "."
        os.makedirs(directory, mode=0o700, exist_ok=True)
        fd, temp_path = tempfile.mkstemp(prefix=".amar-replay-", suffix=".tmp", dir=directory)
        try:
            os.fchmod(fd, 0o600)
            data = json.dumps(self._serialize(), separators=(",", ":"), ensure_ascii=True).encode("utf-8")
            with os.fdopen(fd, "wb") as handle:
                fd = -1
                handle.write(data)
                handle.flush()
                os.fsync(handle.fileno())
            os.replace(temp_path, self.persistence_path)
            directory_fd = os.open(directory, os.O_DIRECTORY)
            try:
                os.fsync(directory_fd)
            finally:
                os.close(directory_fd)
        except OSError:
            if fd >= 0:
                os.close(fd)
            try:
                os.unlink(temp_path)
            except OSError:
                pass
            raise

    def claim_nonce(self, key, expires_at_ms):
        now = int(time.time() * 1000)
        with self._lock:
            changed = self._purge(now)
            if key in self._nonces:
                return False
            self._nonces[key] = int(expires_at_ms)
            self._nonces.move_to_end(key)
            self._trim()
            try:
                self._persist()
            except OSError:
                self._nonces.pop(key, None)
                if changed:
                    self._persist_best_effort()
                return False
            return True

    def claim_idempotency(self, key, fingerprint, expires_at_ms):
        now = int(time.time() * 1000)
        with self._lock:
            changed = self._purge(now)
            existing = self._idempotency.get(key)
            if existing is not None:
                return existing[1] == fingerprint and False
            value = (int(expires_at_ms), fingerprint)
            self._idempotency[key] = value
            self._idempotency.move_to_end(key)
            self._trim()
            try:
                self._persist()
            except OSError:
                self._idempotency.pop(key, None)
                if changed:
                    self._persist_best_effort()
                return False
            return True

    def claim_sequence(self, device_id, sequence, expires_at_ms):
        now = int(time.time() * 1000)
        with self._lock:
            changed = self._purge(now)
            current = self._sequences.get(device_id)
            if current is not None and sequence <= current[1]:
                return False
            previous = current
            self._sequences[device_id] = (int(expires_at_ms), int(sequence))
            self._sequences.move_to_end(device_id)
            self._trim()
            try:
                self._persist()
            except OSError:
                if previous is None:
                    self._sequences.pop(device_id, None)
                else:
                    self._sequences[device_id] = previous
                if changed:
                    self._persist_best_effort()
                return False
            return True

    def release_idempotency(self, key):
        with self._lock:
            removed = self._idempotency.pop(key, None)
            if removed is None:
                return
            try:
                self._persist()
            except OSError:
                # The reservation is retained in memory only if persistence fails.
                self._idempotency[key] = removed
                self._idempotency.move_to_end(key)

    def _persist_best_effort(self):
        try:
            self._persist()
        except OSError:
            pass


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
