"""Fail-closed BOT 1 lifecycle command validation."""
from __future__ import annotations

import base64
import fnmatch
import hashlib
import hmac
import os
import time
from decimal import Decimal, InvalidOperation

from cryptography.exceptions import InvalidSignature
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding

from amar_command_channel import REPLAY_STORE

MAX_CLOCK_SKEW_MS = int(os.environ.get("AMAR_COMMAND_MAX_SKEW_MS", "30000"))
COMMAND_TTL_MS = int(os.environ.get("AMAR_COMMAND_TTL_MS", "15000"))
SIGNING_SECRET = os.environ.get("AMAR_COMMAND_SIGNING_SECRET", "")
REQUIRE_DEVICE_SIGNATURE = os.environ.get("AMAR_REQUIRE_DEVICE_SIGNATURE", "1") == "1"


def decimal_string(value) -> str:
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


def canonical(payload: dict) -> str:
    settings = payload.get("settings") or {}
    values = [
        payload.get("request_id", ""), payload.get("idempotency_key", ""), payload.get("nonce", ""),
        str(payload.get("issued_at_ms", "")), str(payload.get("expires_at_ms", "")),
        str(payload.get("account_login", "")), str(payload.get("bot_magic", "")), payload.get("symbol", ""),
        payload.get("command", ""), payload.get("target_symbol", "") or "",
        "" if payload.get("enabled") is None else str(payload.get("enabled")).lower(),
        decimal_string(settings.get("lot_start", "")), decimal_string(settings.get("grid_step", "")),
        str(settings.get("max_orders", "")), decimal_string(settings.get("martingale", "")),
        decimal_string(settings.get("basket_tp", "")), decimal_string(settings.get("basket_sl", "")),
        decimal_string(settings.get("trailing", "")),
        "" if settings.get("buy_enabled") is None else str(settings.get("buy_enabled")).lower(),
        "" if settings.get("sell_enabled") is None else str(settings.get("sell_enabled")).lower(),
        payload.get("device_id", ""), str(payload.get("sequence", "")), payload.get("device_public_key", ""),
    ]
    return "|".join(str(v) for v in values)


def valid_signature(payload: dict) -> bool:
    if not SIGNING_SECRET:
        return False
    expected = hmac.new(SIGNING_SECRET.encode(), canonical(payload).encode(), hashlib.sha256).hexdigest()
    return hmac.compare_digest(expected, str(payload.get("signature", "")))


def valid_device_signature(payload: dict) -> bool:
    try:
        public_key = serialization.load_der_public_key(base64.b64decode(payload["device_public_key"], validate=True))
        signature = base64.b64decode(payload["device_signature"], validate=True)
        public_key.verify(signature, canonical(payload).encode("utf-8"), padding.PKCS1v15(), hashes.SHA256())
        digest = hashlib.sha256(public_key.public_bytes(serialization.Encoding.DER, serialization.PublicFormat.SubjectPublicKeyInfo)).hexdigest()
        return hmac.compare_digest(digest, str(payload.get("device_id", "")))
    except (InvalidSignature, ValueError, TypeError, KeyError, AttributeError):
        return False


def _valid_symbol_text(value) -> bool:
    return isinstance(value, str) and 1 <= len(value) <= 64 and value.strip() == value and "\n" not in value and "\r" not in value and "\x00" not in value


def symbol_allowed(value: str, exact: set[str], patterns: set[str] | None = None) -> bool:
    if value in exact:
        return True
    return bool(patterns) and any(fnmatch.fnmatchcase(value, pattern) for pattern in patterns)


def validate(payload: dict, expected_login: int, expected_magic: int, allowed_symbols: set[str], allowed_target_symbols: set[str] | None = None, allowed_target_patterns: set[str] | None = None) -> tuple[bool, str]:
    if not isinstance(payload, dict):
        return False, "invalid request"
    required = ("request_id", "idempotency_key", "nonce", "issued_at_ms", "expires_at_ms", "account_login", "bot_magic", "symbol", "command", "signature", "device_id", "sequence", "device_public_key", "device_signature")
    if any(k not in payload or payload.get(k) in (None, "") for k in required):
        return False, "missing command fields"
    try:
        issued = int(payload["issued_at_ms"]); expires = int(payload["expires_at_ms"])
        login = int(payload["account_login"]); magic = int(payload["bot_magic"]); sequence = int(payload["sequence"])
    except (TypeError, ValueError):
        return False, "invalid numeric fields"
    if sequence <= 0:
        return False, "invalid command sequence"
    now = int(time.time() * 1000)
    if issued > now + MAX_CLOCK_SKEW_MS or expires <= now or expires - issued > COMMAND_TTL_MS:
        return False, "expired command"
    if login != int(expected_login) or magic != int(expected_magic):
        return False, "invalid execution scope"
    symbol = payload["symbol"]
    if not _valid_symbol_text(symbol) or symbol not in allowed_symbols:
        return False, "symbol not allow-listed"
    target = payload.get("target_symbol")
    if target is not None:
        if not _valid_symbol_text(target):
            return False, "invalid target symbol"
        target_allow = allowed_target_symbols if allowed_target_symbols is not None else allowed_symbols
        if not symbol_allowed(target, target_allow, allowed_target_patterns):
            return False, "target symbol not allow-listed"
    commands = {
        "START", "STOP", "REBUILD", "CLOSE_ALL", "CLOSE_BUY", "CLOSE_SELL",
        "SET_BUY_ENABLED", "SET_SELL_ENABLED", "UPDATE_SETTINGS",
    }
    if payload.get("command") not in commands:
        return False, "unsupported command"
    if payload["command"] in {"SET_BUY_ENABLED", "SET_SELL_ENABLED"} and not isinstance(payload.get("enabled"), bool):
        return False, "enabled required"
    if payload["command"] == "UPDATE_SETTINGS":
        settings = payload.get("settings")
        if not isinstance(settings, dict):
            return False, "settings required"
        for key in ("lot_start", "grid_step", "max_orders", "martingale", "basket_tp", "basket_sl", "trailing", "buy_enabled", "sell_enabled"):
            if key not in settings:
                return False, "incomplete settings"
    if REQUIRE_DEVICE_SIGNATURE and not valid_device_signature(payload):
        return False, "invalid device signature"
    if not valid_signature(payload):
        return False, "invalid signature"
    if not REPLAY_STORE.claim_sequence(str(payload["device_id"]), sequence, expires):
        return False, "out-of-order command sequence"
    return True, "accepted"
