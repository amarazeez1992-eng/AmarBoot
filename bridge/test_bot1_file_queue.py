import json
import time
import unittest
from pathlib import Path
from tempfile import TemporaryDirectory

from amar_bot1_file_queue import ACK_FILE, COMMAND_FILE, QueueBusyError, enqueue


def _command(request_id: str, expires_at_ms: int | None = None):
    now = int(time.time() * 1000)
    return json.dumps({
        "request_id": request_id,
        "idempotency_key": f"idem-{request_id}",
        "nonce": f"nonce-{request_id}",
        "issued_at_ms": now,
        "expires_at_ms": expires_at_ms if expires_at_ms is not None else now + 5000,
        "symbol": "XAUUSD",
        "command": "REBUILD",
    })


class Bot1FileQueueTests(unittest.TestCase):
    def test_authenticated_queue_is_single_flight(self):
        with TemporaryDirectory() as directory:
            enqueue(_command("req-1"), directory)
            with self.assertRaises(QueueBusyError):
                enqueue(_command("req-2"), directory)

    def test_authenticated_queue_can_advance_after_matching_ack(self):
        with TemporaryDirectory() as directory:
            enqueue(_command("req-1"), directory)
            (Path(directory) / ACK_FILE).write_text(
                json.dumps({"request_id": "req-1", "status": "VERIFIED"}) + "\n",
                encoding="utf-8",
            )
            enqueue(_command("req-2"), directory)
            record = json.loads((Path(directory) / COMMAND_FILE).read_text(encoding="utf-8").splitlines()[-1])
            self.assertEqual(record["request_id"], "req-2")

    def test_expired_pending_command_can_be_superseded(self):
        with TemporaryDirectory() as directory:
            enqueue(_command("req-1", expires_at_ms=int(time.time() * 1000) + 2000), directory)
            time.sleep(2.1)
            enqueue(_command("req-2"), directory)
            record = json.loads((Path(directory) / COMMAND_FILE).read_text(encoding="utf-8").splitlines()[-1])
            self.assertEqual(record["request_id"], "req-2")

    def test_legacy_queue_caller_remains_compatible(self):
        with TemporaryDirectory() as directory:
            enqueue(json.dumps({"command": "REBUILD", "symbol": "XAUUSD"}), directory)
            self.assertTrue((Path(directory) / COMMAND_FILE).exists())


if __name__ == "__main__":
    unittest.main()
