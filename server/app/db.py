from __future__ import annotations

from contextlib import contextmanager
from datetime import UTC, datetime, timedelta
import json
from pathlib import Path
from threading import Lock
from typing import Iterable

from .schemas import DeviceIn, SmsIn


_DB_LOCK = Lock()


def utc_now() -> datetime:
    return datetime.now(UTC)


def epoch_ms(value: datetime) -> int:
    if value.tzinfo is None:
        value = value.replace(tzinfo=UTC)
    return int(value.timestamp() * 1000)


def iso_utc(value: datetime) -> str:
    if value.tzinfo is None:
        value = value.replace(tzinfo=UTC)
    return value.astimezone(UTC).isoformat().replace("+00:00", "Z")


def _empty_data() -> dict:
    return {
        "next_sms_id": 1,
        "devices": {},
        "messages": [],
    }


class JsonStore:
    def __init__(self, path: str) -> None:
        self.path = Path(path)
        self.data = self._read()

    def _read(self) -> dict:
        if not self.path.exists():
            return _empty_data()
        try:
            data = json.loads(self.path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            backup = self.path.with_suffix(f"{self.path.suffix}.broken")
            self.path.replace(backup)
            return _empty_data()

        data.setdefault("next_sms_id", 1)
        data.setdefault("devices", {})
        data.setdefault("messages", [])
        return data

    def commit(self) -> None:
        self.path.parent.mkdir(parents=True, exist_ok=True)
        tmp_path = self.path.with_suffix(f"{self.path.suffix}.tmp")
        tmp_path.write_text(
            json.dumps(self.data, ensure_ascii=False, indent=2),
            encoding="utf-8",
        )
        tmp_path.replace(self.path)


def init_db(db_path: str) -> None:
    store = JsonStore(db_path)
    store.commit()


@contextmanager
def connect(db_path: str):
    store = JsonStore(db_path)
    try:
        yield store
    finally:
        pass


def upsert_device(conn: JsonStore, device: DeviceIn) -> None:
    now = iso_utc(utc_now())
    existing = conn.data["devices"].get(device.id, {})
    conn.data["devices"][device.id] = {
        "id": device.id,
        "name": device.name,
        "role": device.role,
        "last_seen_at": now,
        "created_at": existing.get("created_at", now),
    }


def insert_messages(
    conn: JsonStore,
    device: DeviceIn,
    messages: Iterable[SmsIn],
) -> tuple[list[dict], int]:
    inserted: list[dict] = []
    duplicates = 0
    now = iso_utc(utc_now())
    upsert_device(conn, device)

    seen = {
        (message.get("device_id"), message.get("sms_id"))
        for message in conn.data["messages"]
    }

    for item in messages:
        key = (device.id, item.sms_id)
        if key in seen:
            duplicates += 1
            continue

        row = {
            "id": int(conn.data["next_sms_id"]),
            "device_id": device.id,
            "sms_id": item.sms_id,
            "sender": item.sender,
            "body": item.body,
            "received_at": iso_utc(item.received_at),
            "received_at_ms": epoch_ms(item.received_at),
            "sim_slot": item.sim_slot,
            "direction": item.direction,
            "created_at": now,
        }
        conn.data["next_sms_id"] = row["id"] + 1
        conn.data["messages"].append(row)
        seen.add(key)
        inserted.append(row)

    conn.commit()
    return inserted, duplicates


def list_messages(
    conn: JsonStore,
    *,
    device_id: str | None,
    limit: int,
    q: str | None,
    since_ms: int | None,
    until_ms: int | None,
) -> list[dict]:
    query = q.casefold() if q else None
    rows: list[dict] = []

    for message in conn.data["messages"]:
        if device_id and message.get("device_id") != device_id:
            continue
        if since_ms is not None and int(message.get("received_at_ms", 0)) < since_ms:
            continue
        if until_ms is not None and int(message.get("received_at_ms", 0)) > until_ms:
            continue
        if query:
            sender = str(message.get("sender") or "").casefold()
            body = str(message.get("body") or "").casefold()
            if query not in sender and query not in body:
                continue
        rows.append(dict(message))

    rows.sort(key=lambda item: (int(item.get("received_at_ms", 0)), int(item.get("id", 0))), reverse=True)
    return rows[:limit]


def purge_old_messages(conn: JsonStore, retention_days: int) -> int:
    cutoff = utc_now() - timedelta(days=retention_days)
    cutoff_ms = epoch_ms(cutoff)
    before = len(conn.data["messages"])
    conn.data["messages"] = [
        message
        for message in conn.data["messages"]
        if int(message.get("received_at_ms", 0)) >= cutoff_ms
    ]
    removed = before - len(conn.data["messages"])
    if removed:
        conn.commit()
    return removed


def locked() -> Lock:
    return _DB_LOCK
