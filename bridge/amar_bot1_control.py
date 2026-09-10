"""BOT 1 control protocol for the local MT5 bridge.

This module is transport/execution-neutral: it validates the complete command
vocabulary and serializes it for an MT5-side command receiver. It never enables
live trading by itself.
"""
from __future__ import annotations

from dataclasses import asdict, dataclass
from enum import Enum
import json
import math


class CommandType(str, Enum):
    START = "START"
    STOP = "STOP"
    REBUILD = "REBUILD"
    CLOSE_ALL = "CLOSE_ALL"
    SET_BUY_ENABLED = "SET_BUY_ENABLED"
    SET_SELL_ENABLED = "SET_SELL_ENABLED"
    UPDATE_SETTINGS = "UPDATE_SETTINGS"


@dataclass(frozen=True)
class Bot1Settings:
    lot_start: float
    grid_step: float
    max_orders: int
    martingale: float
    basket_tp: float
    basket_sl: float
    trailing: float
    buy_enabled: bool
    sell_enabled: bool

    def __post_init__(self) -> None:
        if not math.isfinite(self.lot_start) or self.lot_start <= 0:
            raise ValueError("invalid lot_start")
        if not math.isfinite(self.grid_step) or self.grid_step <= 0:
            raise ValueError("invalid grid_step")
        if self.max_orders <= 0:
            raise ValueError("invalid max_orders")
        if not math.isfinite(self.martingale) or self.martingale <= 0:
            raise ValueError("invalid martingale")
        if not math.isfinite(self.basket_tp) or not math.isfinite(self.basket_sl):
            raise ValueError("invalid basket target")
        if not math.isfinite(self.trailing) or self.trailing < 0:
            raise ValueError("invalid trailing")


def encode(command: CommandType, settings: Bot1Settings | None = None, enabled: bool | None = None) -> str:
    if command == CommandType.UPDATE_SETTINGS and settings is None:
        raise ValueError("settings required")
    if command in (CommandType.SET_BUY_ENABLED, CommandType.SET_SELL_ENABLED) and enabled is None:
        raise ValueError("enabled required")
    payload = {"command": command.value}
    if settings is not None:
        payload["settings"] = asdict(settings)
    if enabled is not None:
        payload["enabled"] = enabled
    return json.dumps(payload, separators=(",", ":"), sort_keys=True, allow_nan=False)
