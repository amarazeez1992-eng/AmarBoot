"""AMAR MT5 Bridge — B26/B27/B28 read-only foundation.

Default policy is fail-closed: the bridge exposes MT5 account/market reads only.
No trade command is implemented here. BOT 1 execution remains untouched.

Environment:
  AMAR_BRIDGE_TOKEN  required shared secret
  AMAR_BIND_HOST     default 127.0.0.1 (use LAN IP only with HTTPS + firewall)
  AMAR_BRIDGE_PORT   default 8765
  AMAR_TLS_CERT      required PEM certificate
  AMAR_TLS_KEY       required PEM private key
"""
import json
import os
import ssl
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

try:
    import MetaTrader5 as mt5
except ImportError:
    mt5 = None

TOKEN = os.environ.get("AMAR_BRIDGE_TOKEN", "")
HOST = os.environ.get("AMAR_BIND_HOST", "127.0.0.1")
PORT = int(os.environ.get("AMAR_BRIDGE_PORT", "8765"))
CERT = os.environ.get("AMAR_TLS_CERT", "")
KEY = os.environ.get("AMAR_TLS_KEY", "")


def response(handler, status, payload):
    body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    handler.send_response(status)
    handler.send_header("Content-Type", "application/json; charset=utf-8")
    handler.send_header("Content-Length", str(len(body)))
    handler.end_headers()
    handler.wfile.write(body)


def authorized(handler):
    supplied = handler.headers.get("Authorization", "")
    return bool(TOKEN) and supplied == f"Bearer {TOKEN}"


class Handler(BaseHTTPRequestHandler):
    server_version = "AMAR-MT5-Bridge/0.1"

    def log_message(self, fmt, *args):
        print(fmt % args)

    def do_GET(self):
        if not authorized(self):
            return response(self, 401, {"ok": False, "message": "unauthorized"})
        if self.path == "/health":
            connected = bool(mt5 and mt5.terminal_info() is not None)
            return response(self, 200, {"ok": True, "connected": connected, "terminal": "MetaTrader 5"})
        if self.path == "/account":
            if not mt5 or not mt5.initialize():
                return response(self, 503, {"ok": False, "message": "MT5 unavailable"})
            info = mt5.account_info()
            if info is None:
                return response(self, 503, {"ok": False, "message": "account unavailable"})
            return response(self, 200, {"ok": True, "login": info.login, "server": info.server,
                                        "currency": info.currency, "balance": info.balance,
                                        "equity": info.equity, "margin": info.margin})
        if self.path.startswith("/market?symbol="):
            symbol = self.path.split("=", 1)[1]
            if not mt5 or not mt5.initialize():
                return response(self, 503, {"ok": False, "message": "MT5 unavailable"})
            tick = mt5.symbol_info_tick(symbol)
            if tick is None:
                return response(self, 404, {"ok": False, "message": "symbol unavailable"})
            info = mt5.symbol_info(symbol)
            point = info.point if info and info.point else 0.0
            spread = ((tick.ask - tick.bid) / point) if point else 0.0
            return response(self, 200, {"ok": True, "symbol": symbol, "bid": tick.bid,
                                        "ask": tick.ask, "spreadPoints": spread,
                                        "timestampMs": int(tick.time_msc)})
        if self.path == "/positions":
            if not mt5 or not mt5.initialize():
                return response(self, 503, {"ok": False, "message": "MT5 unavailable"})
            positions = mt5.positions_get() or ()
            return response(self, 200, {"ok": True, "items": [position_to_dict(x) for x in positions]})
        if self.path == "/pending-orders":
            if not mt5 or not mt5.initialize():
                return response(self, 503, {"ok": False, "message": "MT5 unavailable"})
            orders = mt5.orders_get() or ()
            return response(self, 200, {"ok": True, "items": [order_to_dict(x) for x in orders]})
        if self.path == "/bot-status":
            return response(self, 200, {"ok": True, "available": False,
                                        "message": "BOT 1 status channel will be added through the approved MT5 integration contract"})
        return response(self, 404, {"ok": False, "message": "not found"})

    def do_POST(self):
        # B28: no execution endpoint exists in this bridge foundation.
        return response(self, 403, {"ok": False, "accepted": False,
                                    "message": "live execution is locked in B28"})


def position_to_dict(x):
    return {"ticket": x.ticket, "symbol": x.symbol, "type": x.type, "volume": x.volume,
            "priceOpen": x.price_open, "sl": x.sl, "tp": x.tp, "profit": x.profit, "magic": x.magic}


def order_to_dict(x):
    return {"ticket": x.ticket, "symbol": x.symbol, "type": x.type, "volume": x.volume_initial,
            "priceOpen": x.price_open, "sl": x.sl, "tp": x.tp, "magic": x.magic}


def main():
    if not TOKEN:
        raise SystemExit("AMAR_BRIDGE_TOKEN is required")
    if not CERT or not KEY:
        raise SystemExit("AMAR_TLS_CERT and AMAR_TLS_KEY are required; HTTP is intentionally disabled")
    if mt5 is None:
        raise SystemExit("Install requirements.txt before starting the bridge")
    mt5.initialize()
    server = ThreadingHTTPServer((HOST, PORT), Handler)
    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    context.load_cert_chain(CERT, KEY)
    server.socket = context.wrap_socket(server.socket, server_side=True)
    print(f"AMAR MT5 Bridge listening on https://{HOST}:{PORT}")
    server.serve_forever()


if __name__ == "__main__":
    main()
