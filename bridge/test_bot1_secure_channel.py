import hashlib
import hmac
import os
import time
import unittest

os.environ.setdefault("AMAR_COMMAND_SIGNING_SECRET", "test-secret")

from amar_bot1_secure_channel import canonical, validate
import amar_bot1_secure_channel as channel


def _payload(command="REBUILD", expires=None, target="XAUUSD"):
    now = int(time.time() * 1000)
    secret = channel.SIGNING_SECRET or "test-secret"
    channel.SIGNING_SECRET = secret
    payload = {
        "request_id": "req-1", "idempotency_key": "idem-1", "nonce": "nonce-1",
        "issued_at_ms": now, "expires_at_ms": expires if expires is not None else now + 5000,
        "account_login": 123, "bot_magic": 20260908, "symbol": "XAUUSD",
        "command": command, "target_symbol": target, "enabled": None,
        "settings": None, "signature": "",
    }
    payload["signature"] = hmac.new(secret.encode(), canonical(payload).encode(), hashlib.sha256).hexdigest()
    return payload


class Bot1SecureChannelTests(unittest.TestCase):
    def test_valid_bot1_command(self):
        ok, message = validate(_payload(), 123, 20260908, {"XAUUSD", "BTCUSD"}, {"XAUUSD", "BTCUSD", "XAUUSDm"})
        self.assertTrue(ok)
        self.assertEqual(message, "accepted")

    def test_wrong_scope_is_rejected(self):
        ok, _ = validate(_payload(), 999, 20260908, {"XAUUSD"})
        self.assertFalse(ok)

    def test_expired_command_is_rejected(self):
        now = int(time.time() * 1000)
        ok, _ = validate(_payload(expires=now - 1), 123, 20260908, {"XAUUSD"})
        self.assertFalse(ok)

    def test_target_symbol_must_be_allow_listed(self):
        ok, _ = validate(_payload(target="XAUUSDm"), 123, 20260908, {"XAUUSD"}, {"XAUUSD"})
        self.assertFalse(ok)

    def test_custom_broker_target_can_be_explicitly_allowed(self):
        ok, _ = validate(_payload(target="XAUUSDm"), 123, 20260908, {"XAUUSD"}, {"XAUUSDm"})
        self.assertTrue(ok)

    def test_target_symbol_pattern_can_be_explicitly_allowed(self):
        ok, _ = validate(_payload(target="XAUUSDm"), 123, 20260908, {"XAUUSD"}, {"XAUUSD"}, {"XAUUSD*"})
        self.assertTrue(ok)

    def test_unmatched_target_pattern_is_rejected(self):
        ok, _ = validate(_payload(target="BTCUSDm"), 123, 20260908, {"XAUUSD"}, {"XAUUSD"}, {"XAUUSD*"})
        self.assertFalse(ok)


if __name__ == "__main__":
    unittest.main()
