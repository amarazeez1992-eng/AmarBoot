"""AMAR MT5 Bridge — hardened read-only integration layer.

B26/B27/B28/B30 remain fail-closed: this service exposes only authenticated
read operations. No trade execution endpoint exists. BOT 1 MQL5 code is
not modified here.
"""
import hmac
import json
import os
import ssl
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse

try:
    import MetaTrader5 as mt5
except ImportError:
    mt5 = None

TOKEN = os.environ.get("AMAR_BRIDGE_TOKEN", "")
HOST = os.environ.get("AMAR_BIND_HOST", "127.0.0.1")
PORT = int(os.environ.get("AMAR_BRIDGE_PORT", "8765"))
CERT = os.environ.get("AMAR_TLS_CERT", "")
KEY = os.environ.get("AMAR_TLS_KEY", "")
BOT_MAGIC = int(os.environ.get("AMAR_BOT_MAGIC", "20260908"))

TIMEFRAMES = {
    "M1": mt5.TIMEFRAME_M1 if mt5 else 1,
    "M5": mt5.TIMEFRAME_M5 if mt5 else 5,
    "M15": mt5.TIMEFRAME_M15 if mt5 else 15,
    "M30": mt5.TIMEFRAME_M30 if mt5 else 30,
    "H1": mt5.TIMEFRAME_H1 if mt5 else 60,
    "H4": mt5.TIMEFRAME_H4 if mt5 else 240,
}


def json_response(handler, status, payload):
    body = json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
    handler.send_response(status)
    handler.send_header("Content-Type", "application/json; charset=utf-8")
    handler.send_header("Cache-Control", "no-store")
    handler.send_header("Content-Length", str(len(body)))
    handler.end_headers()
    handler.wfile.write(body)


def authorized(handler):
    supplied = handler.headers.get("Authorization", "")
    expected = f"Bearer {TOKEN}" if TOKEN else ""
    return bool(TOKEN) and hmac.compare_digest(supplied, expected)


def mt5_ready():
    if mt5 is None:
        return False
    try:
        return bool(mt5.initialize())
    except Exception:
        return False


def parse_magic(params):
    value = params.get("magic", [None])[0]
    if value is None or value == "":
        return None
    try:
        magic = int(value)
    except ValueError:
        raise ValueError("magic must be an integer")
    if magic < 0:
        raise ValueError("magic must be non-negative")
    return magic


def selected_items(items, params):
    symbol = params.get("symbol", [None])[0]
    magic = parse_magic(params)
    if symbol is None and magic is None:
        return list(items)
    return [
        x for x in items
        if (symbol is None or getattr(x, "symbol", None) == symbol)
        and (magic is None or getattr(x, "magic", None) == magic)
    ]


def position_to_dict(x):
    return {
        "ticket": x.ticket,
        "symbol": x.symbol,
        "type": x.type,
        "volume": x.volume,
        "priceOpen": x.price_open,
        "priceCurrent": getattr(x, "price_current", 0.0),
        "sl": x.sl,
        "tp": x.tp,
        "profit": x.profit,
        "swap": getattr(x, "swap", 0.0),
        "magic": x.magic,
        "comment": getattr(x, "comment", ""),
    }


def order_to_dict(x):
    return {
        "ticket": x.ticket,
        "symbol": x.symbol,
        "type": x.type,
        "volume": getattr(x, "volume_initial", getattr(x, "volume_current", 0.0)),
        "priceOpen": x.price_open,
        "sl": x.sl,
        "tp": x.tp,
        "magic": x.magic,
        "comment": getattr(x, "comment", ""),
    }


def candle_to_dict(x):
    return {
        "timestampMs": int(x.time) * 1000,
        "open": float(x.open),
        "high": float(x.high),
        "low": float(x.low),
        "close": float(x.close),
        "tickVolume": int(x.tick_volume),
    }


def bot_status(params):
    requested_magic = parse_magic(params)
    effective_magic = BOT_MAGIC if requested_magic is None else requested_magic
    filtered = dict(params)
    filtered["magic"] = [str(effective_magic)]
    if not mt5_ready():
        return {
            "ok": False,
            "available": False,
            "magic": effective_magic,
            "symbol": params.get("symbol", [None])[0],
            "positions": 0,
            "pendingOrders": 0,
            "floatingProfit": 0.0,
            "lastCheckMs": int(time.time() * 1000),
        }
    positions = selected_items(mt5.positions_get() or (), filtered)
    orders = selected_items(mt5.orders_get() or (), filtered)
    return {
        "ok": True,
        "available": True,
        "magic": effective_magic,
        "symbol": params.get("symbol", [None])[0],
        "positions": len(positions),
        "pendingOrders": len(orders),
        "floatingProfit": round(sum(float(getattr(x, "profit", 0.0)) for x in positions), 8),
        "lastCheckMs": int(time.time() * 1000),
    }


class Handler(BaseHTTPRequestHandler):
    server_version = "AMAR-MT5-Bridge/0.4"

    def log_message(self, fmt, *args):
        print(fmt % args)

    def do_GET(self):
        if not authorized(self):
            return json_response(self, 401, {"ok": False, "message": "unauthorized"})

        parsed = urlparse(self.path)
        path = parsed.path
        params = parse_qs(parsed.query, keep_blank_values=False)

        try:
            parse_magic(params)
        except ValueError as exc:
            return json_response(self, 400, {"ok": False, "message": str(exc)})

        if path == "/health":
            connected = mt5_ready()
            return json_response(self, 200, {
                "ok": True,
                "connected": connected,
                "terminal": "MetaTrader 5",
                "message": "متصل" if connected else "غير متصل",
                "bridge": "0.4",
                "timestampMs": int(time.time() * 1000),
            })

        if path == "/account":
            if not mt5_ready():
                return json_response(self, 503, {"ok": False, "message": "MT5 unavailable"})
            info = mt5.account_info()
            if info is None:
                return json_response(self, 503, {"ok": False, "message": "account unavailable"})
            return json_response(self, 200, {
                "ok": True, "login": info.login, "server": info.server,
                "currency": info.currency, "balance": info.balance,
                "equity": info.equity, "margin": info.margin,
                "marginFree": getattr(info, "margin_free", 0.0),
                "marginLevel": getattr(info, "margin_level", 0.0),
                "tradeAllowed": bool(getattr(info, "trade_allowed", False)),
                "tradeExpert": bool(getattr(info, "trade_expert", False)),
                "connected": True,
            })

        if path == "/market":
            symbol = params.get("symbol", [""])[0].strip()
            if not symbol:
                return json_response(self, 400, {"ok": False, "message": "symbol is required"})
            if not mt5_ready():
                return json_response(self, 503, {"ok": False, "message": "MT5 unavailable"})
            if not mt5.symbol_select(symbol, True):
                return json_response(self, 404, {"ok": False, "message": "symbol unavailable"})
            tick = mt5.symbol_info_tick(symbol)
            if tick is None:
                return json_response(self, 404, {"ok": False, "message": "tick unavailable"})
            info = mt5.symbol_info(symbol)
            point = info.point if info and info.point else 0.0
            spread = ((tick.ask - tick.bid) / point) if point else 0.0
            return json_response(self, 200, {
                "ok": True, "symbol": symbol, "bid": tick.bid, "ask": tick.ask,
                "spreadPoints": spread, "timestampMs": int(tick.time_msc),
            })

        if path == "/candles":
            symbol = params.get("symbol", [""])[0].strip()
            timeframe = params.get("timeframe", [""])[0].strip().upper()
            try:
                count = int(params.get("count", ["80"])[0])
            except ValueError:
                return json_response(self, 400, {"ok": False, "message": "count must be an integer"})
            if not symbol or timeframe not in TIMEFRAMES:
                return json_response(self, 400, {"ok": False, "message": "symbol and timeframe are required"})
            if count < 10 or count > 500:
                return json_response(self, 400, {"ok": False, "message": "count must be between 10 and 500"})
            if not mt5_ready():
                return json_response(self, 503, {"ok": False, "message": "MT5 unavailable"})
            if not mt5.symbol_select(symbol, True):
                return json_response(self, 404, {"ok": False, "message": "symbol unavailable"})
            rates = mt5.copy_rates_from_pos(symbol, TIMEFRAMES[timeframe], 0, count)
            if rates is None:
                return json_response(self, 503, {"ok": False, "message": "candles unavailable"})
            return json_response(self, 200, {
                "ok": True, "symbol": symbol, "timeframe": timeframe,
                "items": [candle_to_dict(x) for x in rates],
            })

        if path == "/positions":
            if not mt5_ready():
                return json_response(self, 503, {"ok": False, "message": "MT5 unavailable"})
            items = selected_items(mt5.positions_get() or (), params)
            return json_response(self, 200, {"ok": True, "items": [position_to_dict(x) for x in items]})

        if path == "/pending-orders":
            if not mt5_ready():
                return json_response(self, 503, {"ok": False, "message": "MT5 unavailable"})
            items = selected_items(mt5.orders_get() or (), params)
            return json_response(self, 200, {"ok": True, "items": [order_to_dict(x) for x in items]})

        if path == "/bot-status":
            return json_response(self, 200, bot_status(params))

        return json_response(self, 404, {"ok": False, "message": "not found"})

    def do_POST(self):
        return json_response(self, 403, {
            "ok": False, "accepted": False,
            "message": "live execution is locked; read-only bridge only",
        })


def main():
    if not TOKEN:
        raise SystemExit("AMAR_BRIDGE_TOKEN is required")
    if not CERT or not KEY:
        raise SystemExit("AMAR_TLS_CERT and AMAR_TLS_KEY are required; HTTP is disabled")
    if mt5 is None:
        raise SystemExit("MetaTrader5 package is required")

    server = ThreadingHTTPServer((HOST, PORT), Handler)
    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    context.minimum_version = ssl.TLSVersion.TLSv1_2
    context.load_cert_chain(certfile=CERT, keyfile=KEY)
    server.socket = context.wrap_socket(server.socket, server_side=True)
    print(f"AMAR MT5 bridge listening on https://{HOST}:{PORT}")
    server.serve_forever()


if __name__ == "__main__":
    main()
