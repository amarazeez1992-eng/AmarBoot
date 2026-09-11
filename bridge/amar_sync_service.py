"""AMAR strong Bot-Vault sync service.

Offline-first protocol for the Android Bot Vault. It stores only bot/strategy
configuration snapshots; trading credentials and execution secrets are never
accepted. Use HTTPS when exposed beyond a trusted LAN/VPN.
"""
import hashlib
import hmac
import json
import os
import tempfile
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

TOKEN = os.environ.get("AMAR_SYNC_TOKEN", "")
HOST = os.environ.get("AMAR_SYNC_HOST", "127.0.0.1")
PORT = int(os.environ.get("AMAR_SYNC_PORT", "8876"))
STORE = os.environ.get("AMAR_SYNC_STORE", os.path.expanduser("~/.amar/bot_vault_sync.json"))
LOCK = threading.Lock()


def digest(snapshot):
    return hashlib.sha256(snapshot.encode("utf-8")).hexdigest()


def read_store():
    if not os.path.exists(STORE):
        return {"revision": 0, "snapshot": "[]", "snapshotHash": digest("[]")}
    try:
        with open(STORE, "r", encoding="utf-8") as f:
            value = json.load(f)
        snapshot = value.get("snapshot", "[]")
        if not isinstance(snapshot, str):
            raise ValueError("invalid snapshot")
        if value.get("snapshotHash") != digest(snapshot):
            raise ValueError("snapshot integrity failure")
        return {
            "revision": int(value.get("revision", 0)),
            "snapshot": snapshot,
            "snapshotHash": value["snapshotHash"],
        }
    except Exception:
        # Fail closed: corrupted sync state is never silently overwritten.
        raise RuntimeError("sync store integrity failure")


def write_store(revision, snapshot):
    os.makedirs(os.path.dirname(STORE) or ".", exist_ok=True)
    payload = {"revision": revision, "snapshot": snapshot, "snapshotHash": digest(snapshot)}
    directory = os.path.dirname(STORE) or "."
    fd, temp = tempfile.mkstemp(prefix=".amar-sync-", dir=directory, text=True)
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as f:
            json.dump(payload, f, ensure_ascii=False, separators=(",", ":"))
            f.flush()
            os.fsync(f.fileno())
        os.replace(temp, STORE)
    finally:
        if os.path.exists(temp):
            os.unlink(temp)


def valid_snapshot(snapshot):
    try:
        value = json.loads(snapshot)
        if not isinstance(value, list):
            return False
        for bot in value:
            if not isinstance(bot, dict):
                return False
            if not isinstance(bot.get("botNumber"), int) or bot["botNumber"] < 1:
                return False
            if not isinstance(bot.get("strategies", []), list):
                return False
        return True
    except Exception:
        return False


def response(handler, status, payload):
    body = json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
    handler.send_response(status)
    handler.send_header("Content-Type", "application/json; charset=utf-8")
    handler.send_header("Cache-Control", "no-store")
    handler.send_header("Content-Length", str(len(body)))
    handler.end_headers()
    handler.wfile.write(body)


class Handler(BaseHTTPRequestHandler):
    server_version = "AMAR-Sync/1.0"

    def log_message(self, fmt, *args):
        print(fmt % args)

    def do_POST(self):
        if self.path != "/sync":
            return response(self, 404, {"ok": False, "message": "not found"})
        supplied = self.headers.get("Authorization", "")
        expected = f"Bearer {TOKEN}" if TOKEN else ""
        if not TOKEN or not hmac.compare_digest(supplied, expected):
            return response(self, 401, {"ok": False, "message": "unauthorized"})
        try:
            length = int(self.headers.get("Content-Length", "0"))
            if length <= 0 or length > 2_000_000:
                return response(self, 413, {"ok": False, "message": "invalid payload size"})
            payload = json.loads(self.rfile.read(length).decode("utf-8"))
            snapshot = json.dumps(payload.get("snapshot"), ensure_ascii=False, separators=(",", ":"))
            if not valid_snapshot(snapshot):
                return response(self, 400, {"ok": False, "message": "invalid snapshot"})
            if payload.get("snapshotHash") != digest(snapshot):
                return response(self, 400, {"ok": False, "message": "snapshot hash mismatch"})
            client_revision = int(payload.get("revision", 0))
            base_hash = str(payload.get("baseHash", ""))
            with LOCK:
                store = read_store()
                if store["revision"] == 0:
                    write_store(1, snapshot)
                    store = read_store()
                elif client_revision != store["revision"] and base_hash != store["snapshotHash"]:
                    return response(self, 409, {
                        "ok": False,
                        "message": "sync conflict",
                        "revision": store["revision"],
                        "snapshot": json.loads(store["snapshot"]),
                        "snapshotHash": store["snapshotHash"],
                    })
                else:
                    next_revision = store["revision"] + (0 if digest(snapshot) == store["snapshotHash"] else 1)
                    if next_revision != store["revision"]:
                        write_store(next_revision, snapshot)
                    store = read_store()
            return response(self, 200, {
                "ok": True,
                "revision": store["revision"],
                "snapshot": json.loads(store["snapshot"]),
                "snapshotHash": store["snapshotHash"],
            })
        except RuntimeError as exc:
            return response(self, 500, {"ok": False, "message": str(exc)})
        except Exception:
            return response(self, 400, {"ok": False, "message": "invalid sync request"})


if __name__ == "__main__":
    if not TOKEN:
        raise SystemExit("AMAR_SYNC_TOKEN is required")
    print(f"AMAR sync listening on {HOST}:{PORT}")
    ThreadingHTTPServer((HOST, PORT), Handler).serve_forever()
