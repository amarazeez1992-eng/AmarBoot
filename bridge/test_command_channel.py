import os
import unittest

os.environ["AMAR_COMMAND_SIGNING_SECRET"] = "test-secret"

from amar_command_channel import canonical, valid_signature, validate


class CommandChannelTest(unittest.TestCase):
    def envelope(self):
        payload = {
            "requestId": "r1",
            "idempotencyKey": "i1",
            "nonce": "n1",
            "issuedAtMs": 1_000,
            "expiresAtMs": 10_000,
            "accountLogin": 123,
            "botMagic": 20260908,
            "symbol": "XAUUSD",
            "command": {"requestId": "r1", "side": "BUY", "quantity": 0.01, "price": None},
        }
        import hmac, hashlib
        payload["signature"] = hmac.new(b"test-secret", canonical(payload).encode(), hashlib.sha256).hexdigest()
        return payload

    def test_signature_is_valid(self):
        self.assertTrue(valid_signature(self.envelope()))

    def test_scope_is_enforced(self):
        payload = self.envelope()
        ok, _ = validate(payload, 123, 20260908, "XAUUSD")
        self.assertTrue(ok)
        payload["botMagic"] = 7
        ok, _ = validate(payload, 123, 20260908, "XAUUSD")
        self.assertFalse(ok)

    def test_replay_is_rejected(self):
        payload = self.envelope()
        ok, _ = validate(payload, 123, 20260908, "XAUUSD")
        self.assertTrue(ok)
        ok, _ = validate(payload, 123, 20260908, "XAUUSD")
        self.assertFalse(ok)


if __name__ == "__main__":
    unittest.main()
