import json

import pytest

from amar_bot1_file_queue import ACK_FILE, COMMAND_FILE, QueueBusyError, enqueue


def _command(request_id: str):
    return json.dumps({
        "request_id": request_id,
        "idempotency_key": f"idem-{request_id}",
        "nonce": f"nonce-{request_id}",
        "issued_at_ms": 1000,
        "expires_at_ms": 5000,
        "symbol": "XAUUSD",
        "command": "REBUILD",
    })


def test_authenticated_queue_is_single_flight(tmp_path):
    enqueue(_command("req-1"), tmp_path)
    with pytest.raises(QueueBusyError):
        enqueue(_command("req-2"), tmp_path)


def test_authenticated_queue_can_advance_after_matching_ack(tmp_path):
    enqueue(_command("req-1"), tmp_path)
    (tmp_path / ACK_FILE).write_text(
        json.dumps({"request_id": "req-1", "status": "VERIFIED"}) + "\n",
        encoding="utf-8",
    )
    enqueue(_command("req-2"), tmp_path)
    record = json.loads((tmp_path / COMMAND_FILE).read_text(encoding="utf-8").splitlines()[-1])
    assert record["request_id"] == "req-2"


def test_legacy_queue_caller_remains_compatible(tmp_path):
    enqueue(json.dumps({"command": "REBUILD", "symbol": "XAUUSD"}), tmp_path)
    assert (tmp_path / COMMAND_FILE).exists()
