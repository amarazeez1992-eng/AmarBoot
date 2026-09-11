import json
import os
import tempfile
import unittest

import amar_sync_service as sync


class SyncServiceTests(unittest.TestCase):
    def test_digest_and_snapshot_validation(self):
        snapshot = '[{"botNumber":1,"name":"BOT 1","strategies":[]}]'
        self.assertEqual(sync.digest(snapshot), sync.digest(snapshot))
        self.assertTrue(sync.valid_snapshot(snapshot))
        self.assertFalse(sync.valid_snapshot('{"botNumber":1}'))

    def test_store_is_atomic_and_integrity_checked(self):
        with tempfile.TemporaryDirectory() as d:
            old = sync.STORE
            sync.STORE = os.path.join(d, "vault.json")
            try:
                sync.write_store(1, '[]')
                value = sync.read_store()
                self.assertEqual(value["revision"], 1)
                self.assertEqual(value["snapshot"], "[]")
                with open(sync.STORE, "r+", encoding="utf-8") as f:
                    data = json.load(f)
                    data["snapshot"] = '[1]'
                    f.seek(0); json.dump(data, f); f.truncate()
                with self.assertRaises(RuntimeError):
                    sync.read_store()
            finally:
                sync.STORE = old


if __name__ == "__main__":
    unittest.main()
