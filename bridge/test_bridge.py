import os
import sys
import unittest
from types import SimpleNamespace

os.environ.setdefault("AMAR_BRIDGE_TOKEN", "test-token")
sys.path.insert(0, os.path.dirname(__file__))
import amar_mt5_bridge as bridge


class BridgeContractTests(unittest.TestCase):
    def test_symbol_and_magic_filter(self):
        items = [
            SimpleNamespace(symbol="XAUUSD", magic=20260908),
            SimpleNamespace(symbol="EURUSD", magic=20260908),
            SimpleNamespace(symbol="XAUUSD", magic=7),
        ]
        selected = bridge.selected_items(items, {"symbol": ["XAUUSD"], "magic": ["20260908"]})
        self.assertEqual(1, len(selected))
        self.assertEqual("XAUUSD", selected[0].symbol)

    def test_filter_without_arguments_preserves_all(self):
        items = [SimpleNamespace(symbol="XAUUSD", magic=1)]
        self.assertEqual(items, bridge.selected_items(items, {}))

    def test_invalid_magic_is_rejected(self):
        with self.assertRaises(ValueError):
            bridge.parse_magic({"magic": ["not-a-number"]})
        with self.assertRaises(ValueError):
            bridge.parse_magic({"magic": ["-1"]})

    def test_position_mapping_is_safe_and_complete(self):
        position = SimpleNamespace(
            ticket=1, symbol="XAUUSD", type=0, volume=0.01,
            price_open=2500.0, price_current=2501.0, sl=0.0, tp=0.0,
            profit=1.0, swap=0.0, magic=20260908, comment="BOT1"
        )
        mapped = bridge.position_to_dict(position)
        self.assertEqual(1, mapped["ticket"])
        self.assertEqual(20260908, mapped["magic"])
        self.assertEqual(2501.0, mapped["priceCurrent"])


if __name__ == "__main__":
    unittest.main()
