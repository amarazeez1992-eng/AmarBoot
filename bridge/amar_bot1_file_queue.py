"""Local BOT 1 command queue for the MT5 terminal-side receiver.

The bridge writes one authenticated, already-validated command per line. The
EA polls the shared MT5 Files/Common directory. This module does not authorize
live trading; the secure HTTP command layer must validate the request first.
"""
from __future__ import annotations

import json
from pathlib import Path
import tempfile

COMMAND_FILE = "AMAR_BOT1_COMMANDS.jsonl"


def enqueue(command_json: str, common_files_dir: str | Path) -> Path:
    if not command_json or "\n" in command_json or "\r" in command_json:
        raise ValueError("invalid command")
    directory = Path(common_files_dir)
    directory.mkdir(parents=True, exist_ok=True)
    path = directory / COMMAND_FILE
    record = json.loads(command_json)
    if not isinstance(record, dict) or record.get("command") not in {
        "START", "STOP", "REBUILD", "CLOSE_ALL", "SET_BUY_ENABLED",
        "SET_SELL_ENABLED", "UPDATE_SETTINGS",
    }:
        raise ValueError("unsupported BOT 1 command")
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
