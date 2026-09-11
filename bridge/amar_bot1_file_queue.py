"""Fail-closed single-flight BOT 1 command queue for MT5.

The authenticated bridge writes one command at a time into the MT5
FILE_COMMON sandbox. A command cannot be overwritten while it is still
pending. Expired commands are safely superseded because the terminal can no
longer be expected to execute them.
"""
from __future__ import annotations

import json
from pathlib import Path
import tempfile
import time

COMMAND_FILE = "AMAR_BOT1_COMMANDS.jsonl"
ACK_FILE = "AMAR_BOT1_ACK.jsonl"
SUPPORTED_COMMANDS = {
    "START", "STOP", "REBUILD", "CLOSE_ALL", "SET_BUY_ENABLED",
    "SET_SELL_ENABLED", "UPDATE_SETTINGS",
}


class QueueBusyError(RuntimeError):
    """A previous authenticated command is still awaiting terminal ACK."""


def _read_last_json(path: Path) -> dict | None:
    if not path.exists():
        return None
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
        if not lines:
            return None
        value = json.loads(lines[-1])
        return value if isinstance(value, dict) else None
    except (OSError, UnicodeError, json.JSONDecodeError):
        return None


def enqueue(command_json: str, common_files_dir: str | Path) -> Path:
    if not command_json or "\n" in command_json or "\r" in command_json:
        raise ValueError("invalid command")
    directory = Path(common_files_dir)
    directory.mkdir(parents=True, exist_ok=True)
    path = directory / COMMAND_FILE
    record = json.loads(command_json)
    if not isinstance(record, dict) or record.get("command") not in SUPPORTED_COMMANDS:
        raise ValueError("unsupported BOT 1 command")

    identity_keys = ("request_id", "idempotency_key", "nonce", "issued_at_ms", "expires_at_ms")
    has_identity = any(key in record for key in identity_keys)
    if has_identity:
        for key in identity_keys + ("symbol",):
            if key not in record or not record[key]:
                raise ValueError(f"missing {key}")
        issued = int(record["issued_at_ms"])
        expires = int(record["expires_at_ms"])
        if expires <= issued:
            raise ValueError("invalid command expiry")

        pending = _read_last_json(path)
        if pending:
            pending_id = str(pending.get("request_id", ""))
            pending_expires = int(pending.get("expires_at_ms", 0) or 0)
            ack = _read_last_json(directory / ACK_FILE)
            ack_id = str(ack.get("request_id", "")) if ack else ""
            still_pending = pending_expires > int(time.time() * 1000) and pending_id != ack_id
            if pending_id and pending_id != str(record["request_id"]) and still_pending:
                raise QueueBusyError("previous BOT 1 command is awaiting MT5 acknowledgement")

    target_symbol = record.get("target_symbol")
    if target_symbol is not None:
        if not isinstance(target_symbol, str) or not target_symbol.strip():
            raise ValueError("invalid target_symbol")
        if "\n" in target_symbol or "\r" in target_symbol:
            raise ValueError("invalid target_symbol")
        record["target_symbol"] = target_symbol.strip()

    data = (json.dumps(record, separators=(",", ":"), sort_keys=True, allow_nan=False) + "\n").encode("utf-8")
    fd, tmp = tempfile.mkstemp(prefix="AMAR_BOT1_", dir=directory)
    try:
        with open(fd, "wb", closefd=True) as handle:
            handle.write(data)
            handle.flush()
        Path(tmp).replace(path)
    except Exception:
        Path(tmp).unlink(missing_ok=True)
        raise
    return path
