import os
import time

os.environ.setdefault("AMAR_COMMAND_SIGNING_SECRET", "test-secret")

from amar_bot1_secure_channel import canonical, validate
import amar_bot1_secure_channel as channel


def _payload(command="REBUILD", expires=None):
    now = int(time.time() * 1000)
    payload = {
        "request_id": "req-1", "idempotency_key": "idem-1", "nonce": "nonce-1",
        "issued_at_ms": now, "expires_at_ms": expires or now + 5000,
        "account_login": 123, "bot_magic": 20260908, "symbol": "XAUUSD",
        "command": command, "target_symbol": "XAUUSD", "enabled": None,
        "settings": None, "signature": "",
    }
    import hashlib, hmac
    payload["signature"] = hmac.new(b"test-secret", canonical(payload).encode(), hashlib.sha256).hexdigest()
    return payload


def test_valid_bot1_command():
    ok, message = validate(_payload(), 123, 20260908, {"XAUUSD", "BTCUSD"})
    assert ok is True
    assert message == "accepted"


def test_wrong_scope_is_rejected():
    ok, _ = validate(_payload(), 999, 20260908, {"XAUUSD"})
    assert ok is False


def test_expired_command_is_rejected():
    now = int(time.time() * 1000)
    ok, _ = validate(_payload(expires=now - 1), 123, 20260908, {"XAUUSD"})
    assert ok is False
