import hashlib
import hmac
import os
import time
import unittest

os.environ["AMAR_COMMAND_SIGNING_SECRET"] = "test-secret"

from amar_command_channel import canonical, valid_signature, validate


class CommandChannelTest(unittest.TestCase):
    def envelope(self, request_id="r1", idempotency="i1", nonce="n1"):
        now = int(time.time() * 1000)
        payload = {
            "requestId": request_id,
            "idempotencyKey": idempotency,
            "nonce": nonce,
            "issuedAtMs": now,
            "expiresAtMs": now + 10_000,
            "accountLogin": 123,
            "botMagic": 20260908,
            "symbol": "XAUUSD",
            "command": {"requestId": request_id, "side": "BUY", "quantity": 0.01, "price": None},
        }
        payload["signature"] = hmac.new(b"test-secret", canonical(payload).encode(), hashlib.sha256).hexdigest()
        return payload

    def test_signature_is_valid(self):
        self.assertTrue(valid_signature(self.envelope()))

    def test_scope_is_enforced(self):
        payload = self.envelope(request_id="scope")
        self.assertEqual(validate(payload, 123, 20260908, "XAUUSD")[0], True)
        bad = self.envelope(request_id="scope-bad")
        bad["botMagic"] = 7
        self.assertFalse(validate(bad, 123, 20260908, "XAUUSD")[0])

    def test_replay_and_idempotency_are_rejected(self):
        first = self.envelope(request_id="replay", idempotency="id-1", nonce="nonce-1")
        self.assertTrue(validate(first, 123, 20260908, "XAUUSD")[0])
        self.assertFalse(validate(first, 123, 20260908, "XAUUSD")[0])
        second = self.envelope(request_id="replay-2", idempotency="id-1", nonce="nonce-2")
        self.assertFalse(validate(second, 123, 20260908, "XAUUSD")[0])


if __name__ == "__main__":
    unittest.main()
