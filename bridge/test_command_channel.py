import hashlib
import hmac
import os
import time
import unittest

os.environ["AMAR_COMMAND_SIGNING_SECRET"] = "test-secret"

from amar_command_channel import REPLAY_STORE, canonical, decimal_string, valid_signature, validate


class CommandChannelTest(unittest.TestCase):
    def envelope(self, request_id="r1", idempotency="i1", nonce="n1", quantity=0.01, price=None):
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
            "command": {"requestId": request_id, "side": "BUY", "quantity": quantity, "price": price},
        }
        payload["signature"] = hmac.new(b"test-secret", canonical(payload).encode(), hashlib.sha256).hexdigest()
        return payload

    def test_signature_is_valid(self):
        self.assertTrue(valid_signature(self.envelope()))

    def test_decimal_canonicalization_is_stable(self):
        self.assertEqual(decimal_string(1.0), "1")
        self.assertEqual(decimal_string("1.2500"), "1.25")
        self.assertEqual(decimal_string("1000.000"), "1000")
        a = self.envelope(request_id="decimal-a", idempotency="decimal-a", nonce="decimal-a", quantity=1.0)
        b = self.envelope(request_id="decimal-b", idempotency="decimal-b", nonce="decimal-b", quantity="1.000")
        self.assertTrue(canonical(a).endswith("|1|"))
        self.assertTrue(canonical(b).endswith("|1|"))

    def test_scope_is_enforced(self):
        payload = self.envelope(request_id="scope", idempotency="scope", nonce="scope")
        self.assertTrue(validate(payload, 123, 20260908, "XAUUSD")[0])
        bad = self.envelope(request_id="scope-bad", idempotency="scope-bad", nonce="scope-bad")
        bad["botMagic"] = 7
        self.assertFalse(validate(bad, 123, 20260908, "XAUUSD")[0])

    def test_replay_and_idempotency_are_rejected(self):
        first = self.envelope(request_id="replay", idempotency="id-1", nonce="nonce-1")
        self.assertTrue(validate(first, 123, 20260908, "XAUUSD")[0])
        self.assertFalse(validate(first, 123, 20260908, "XAUUSD")[0])
        second = self.envelope(request_id="replay-2", idempotency="id-1", nonce="nonce-2")
        self.assertFalse(validate(second, 123, 20260908, "XAUUSD")[0])

    def test_failed_execution_can_release_idempotency_for_retry(self):
        first = self.envelope(request_id="retry-1", idempotency="retry-key", nonce="retry-nonce-1")
        self.assertTrue(validate(first, 123, 20260908, "XAUUSD")[0])
        REPLAY_STORE.release_idempotency("idempotency:retry-key")
        second = self.envelope(request_id="retry-2", idempotency="retry-key", nonce="retry-nonce-2")
        self.assertTrue(validate(second, 123, 20260908, "XAUUSD")[0])


if __name__ == "__main__":
    unittest.main()
