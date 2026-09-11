import base64
import hashlib
import hmac
import os
import time
import unittest

from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding, rsa

os.environ.setdefault("AMAR_COMMAND_SIGNING_SECRET", "test-secret")
os.environ.setdefault("AMAR_REQUIRE_DEVICE_SIGNATURE", "1")

from amar_bot1_secure_channel import canonical, validate
import amar_bot1_secure_channel as channel
from amar_command_channel import REPLAY_STORE


def _payload(command="REBUILD", expires=None, target="XAUUSD", sequence=None):
    now = int(time.time() * 1000)
    secret = channel.SIGNING_SECRET or "test-secret"
    channel.SIGNING_SECRET = secret
    private = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    public_der = private.public_key().public_bytes(serialization.Encoding.DER, serialization.PublicFormat.SubjectPublicKeyInfo)
    device_id = hashlib.sha256(public_der).hexdigest()
    payload = {
        "request_id": f"req-{now}", "idempotency_key": f"idem-{now}", "nonce": f"nonce-{now}",
        "issued_at_ms": now, "expires_at_ms": expires if expires is not None else now + 5000,
        "account_login": 123, "bot_magic": 20260908, "symbol": "XAUUSD",
        "command": command, "target_symbol": target, "enabled": None, "settings": None,
        "device_id": device_id, "sequence": sequence if sequence is not None else now,
        "device_public_key": base64.b64encode(public_der).decode("ascii"),
        "device_signature": "", "signature": "",
    }
    payload["device_signature"] = base64.b64encode(private.sign(canonical(payload).encode(), padding.PKCS1v15(), hashes.SHA256())).decode("ascii")
    payload["signature"] = hmac.new(secret.encode(), canonical(payload).encode(), hashlib.sha256).hexdigest()
    return payload


class Bot1SecureChannelTests(unittest.TestCase):
    def setUp(self):
        REPLAY_STORE._nonces.clear()
        REPLAY_STORE._idempotency.clear()
        REPLAY_STORE._sequences.clear()

    def test_valid_bot1_command(self):
        ok, message = validate(_payload(), 123, 20260908, {"XAUUSD", "BTCUSD"}, {"XAUUSD", "BTCUSD", "XAUUSDm"})
        self.assertTrue(ok)
        self.assertEqual(message, "accepted")

    def test_side_close_commands_are_valid(self):
        for command in ("CLOSE_BUY", "CLOSE_SELL"):
            with self.subTest(command=command):
                ok, message = validate(_payload(command=command), 123, 20260908, {"XAUUSD"})
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

    def test_device_signature_tamper_is_rejected(self):
        payload = _payload()
        payload["device_id"] = "0" * 64
        ok, message = validate(payload, 123, 20260908, {"XAUUSD"})
        self.assertFalse(ok)
        self.assertEqual(message, "invalid device signature")

    def test_malformed_device_public_key_fails_closed(self):
        payload = _payload()
        payload["device_public_key"] = "not-a-key"
        ok, message = validate(payload, 123, 20260908, {"XAUUSD"})
        self.assertFalse(ok)
        self.assertEqual(message, "invalid device signature")

    def test_device_signature_tamper_fails_closed(self):
        payload = _payload()
        payload["device_signature"] = base64.b64encode(b"tampered").decode("ascii")
        ok, message = validate(payload, 123, 20260908, {"XAUUSD"})
        self.assertFalse(ok)
        self.assertEqual(message, "invalid device signature")

    def test_same_device_lower_sequence_is_rejected_after_valid_signature(self):
        private = rsa.generate_private_key(public_exponent=65537, key_size=2048)
        public_der = private.public_key().public_bytes(serialization.Encoding.DER, serialization.PublicFormat.SubjectPublicKeyInfo)
        device_id = hashlib.sha256(public_der).hexdigest()

        def make(seq):
            p = _payload(sequence=seq)
            p["device_id"] = device_id
            p["device_public_key"] = base64.b64encode(public_der).decode("ascii")
            p["device_signature"] = base64.b64encode(private.sign(canonical(p).encode(), padding.PKCS1v15(), hashes.SHA256())).decode("ascii")
            p["signature"] = hmac.new(channel.SIGNING_SECRET.encode(), canonical(p).encode(), hashlib.sha256).hexdigest()
            return p

        self.assertTrue(validate(make(200), 123, 20260908, {"XAUUSD"})[0])
        ok, message = validate(make(199), 123, 20260908, {"XAUUSD"})
        self.assertFalse(ok)
        self.assertEqual(message, "out-of-order command sequence")


if __name__ == "__main__":
    unittest.main()
