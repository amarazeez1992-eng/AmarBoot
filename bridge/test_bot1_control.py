import json
import tempfile
import unittest
from pathlib import Path

from amar_bot1_control import Bot1Settings, CommandType, encode
from amar_bot1_file_queue import enqueue, COMMAND_FILE


class Bot1ControlTest(unittest.TestCase):
    def setUp(self):
        self.settings = Bot1Settings(0.01, 30.0, 10, 2.0, 50.0, -30.0, 0.0, True, True, "XAUUSD")

    def test_all_commands_are_serializable(self):
        for command in CommandType:
            if command == CommandType.UPDATE_SETTINGS:
                payload = encode(command, self.settings)
            elif command in (CommandType.SET_BUY_ENABLED, CommandType.SET_SELL_ENABLED):
                payload = encode(command, enabled=False)
            else:
                payload = encode(command)
            self.assertEqual(json.loads(payload)["command"], command.value)

    def test_target_symbol_is_serialized_explicitly(self):
        payload = json.loads(encode(CommandType.REBUILD, target_symbol="XAUUSDm"))
        self.assertEqual(payload["target_symbol"], "XAUUSDm")

    def test_target_symbol_is_part_of_settings(self):
        payload = json.loads(encode(CommandType.UPDATE_SETTINGS, self.settings))
        self.assertEqual(payload["settings"]["target_symbol"], "XAUUSD")

    def test_invalid_settings_are_rejected(self):
        with self.assertRaises(ValueError):
            Bot1Settings(0.0, 30.0, 10, 2.0, 50.0, -30.0, 0.0, True, True)

    def test_invalid_target_symbol_is_rejected(self):
        with self.assertRaises(ValueError):
            Bot1Settings(0.01, 30.0, 10, 2.0, 50.0, -30.0, 0.0, True, True, "XAU\nUSD")
        with self.assertRaises(ValueError):
            encode(CommandType.REBUILD, target_symbol="   ")

    def test_queue_is_atomic_and_writes_valid_json(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = enqueue(encode(CommandType.REBUILD, target_symbol="XAUUSDm"), tmp)
            self.assertEqual(path.name, COMMAND_FILE)
            lines = Path(path).read_text(encoding="utf-8").splitlines()
            self.assertEqual(len(lines), 1)
            record = json.loads(lines[0])
            self.assertEqual(record["command"], "REBUILD")
            self.assertEqual(record["target_symbol"], "XAUUSDm")


if __name__ == "__main__":
    unittest.main()
