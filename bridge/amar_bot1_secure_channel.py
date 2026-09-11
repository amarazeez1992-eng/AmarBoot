"""B37 secure BOT 1 lifecycle command validation.

Separate from market-order canonicalization so the grid settings contract can
remain stable. The bridge authenticates this envelope before it reaches the
MT5 common-file queue.
"""
from __future__ import annotations

import hashlib
import hmac
import os
import time
from decimal import Decimal, InvalidOperation

MAX_CLOCK_SKEW_MS = int(os.environ.get("AMAR_COMMAND_MAX_SKEW_MS", "30000"))
COMMAND_TTL_MS = int(os.environ.get("AMAR_COMMAND_TTL_MS", "15000"))
SIGNING_SECRET = os.environ.get("AMAR_COMMAND_SIGNING_SECRET", "")


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
    ]
    return "|".join(str(v) for v in values)


def valid_signature(payload: dict) -> bool:
    if not SIGNING_SECRET:
        return False
    expected = hmac.new(SIGNING_SECRET.encode(), canonical(payload).encode(), hashlib.sha256).hexdigest()
    return hmac.compare_digest(expected, str(payload.get("signature", "")))


def validate(payload: dict, expected_login: int, expected_magic: int, allowed_symbols: set[str]) -> tuple[bool, str]:
    if not isinstance(payload, dict):
        return False, "invalid request"
    required = ("request_id", "idempotency_key", "nonce", "issued_at_ms", "expires_at_ms", "account_login", "bot_magic", "symbol", "command", "signature")
    if any(not payload.get(k) for k in required):
        return False, "missing command fields"
    try:
        issued = int(payload["issued_at_ms"]); expires = int(payload["expires_at_ms"])
        login = int(payload["account_login"]); magic = int(payload["bot_magic"])
    except (TypeError, ValueError):
        return False, "invalid numeric fields"
    now = int(time.time() * 1000)
    if issued > now + MAX_CLOCK_SKEW_MS or expires <= now or expires - issued > COMMAND_TTL_MS:
        return False, "expired command"
    if login != int(expected_login) or magic != int(expected_magic):
        return False, "invalid execution scope"
    symbol = str(payload["symbol"])
    if symbol not in allowed_symbols:
        return False, "symbol not allow-listed"
    target = payload.get("target_symbol")
    if target is not None and (not isinstance(target, str) or not target.strip() or "\n" in target or "\r" in target):
        return False, "invalid target symbol"
    commands = {"START", "STOP", "REBUILD", "CLOSE_ALL", "SET_BUY_ENABLED", "SET_SELL_ENABLED", "UPDATE_SETTINGS"}
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
    if not valid_signature(payload):
        return False, "invalid signature"
    return True, "accepted"
