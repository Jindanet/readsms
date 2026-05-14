from contextlib import asynccontextmanager
from datetime import datetime

from fastapi import Depends, FastAPI, Query, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware

from .config import Settings, get_settings
from .db import connect, epoch_ms, init_db, insert_messages, list_messages, locked, purge_old_messages
from .schemas import DeviceIn, SmsListResponse, SmsSyncRequest, SmsSyncResponse
from .security import require_api_token, require_ws_token


_initialized_db_paths: set[str] = set()


def ensure_db(settings: Settings) -> None:
    if settings.db_path in _initialized_db_paths:
        return
    with locked():
        if settings.db_path not in _initialized_db_paths:
            init_db(settings.db_path)
            with connect(settings.db_path) as conn:
                purge_old_messages(conn, settings.retention_days)
            _initialized_db_paths.add(settings.db_path)


@asynccontextmanager
async def lifespan(_app: FastAPI):
    ensure_db(get_settings())
    yield


app = FastAPI(title="ReadSMS API", version="0.1.0", lifespan=lifespan)


class ConnectionManager:
    def __init__(self) -> None:
        self._connections: set[WebSocket] = set()

    async def connect(self, websocket: WebSocket) -> None:
        await websocket.accept()
        self._connections.add(websocket)

    def disconnect(self, websocket: WebSocket) -> None:
        self._connections.discard(websocket)

    async def broadcast(self, payload: dict) -> None:
        stale: list[WebSocket] = []
        for websocket in list(self._connections):
            try:
                await websocket.send_json(payload)
            except Exception:
                stale.append(websocket)
        for websocket in stale:
            self.disconnect(websocket)


manager = ConnectionManager()


@app.middleware("http")
async def purge_retention_middleware(request, call_next):
    response = await call_next(request)
    if request.url.path.startswith("/api/sms"):
        settings = get_settings()
        ensure_db(settings)
        with locked():
            with connect(settings.db_path) as conn:
                purge_old_messages(conn, settings.retention_days)
    return response


def configure_cors() -> None:
    settings = get_settings()
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.cors_origins,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )


configure_cors()


@app.get("/health")
def health(settings: Settings = Depends(get_settings)) -> dict:
    return {
        "ok": True,
        "retention_days": settings.retention_days,
    }


@app.get("/api/validate", dependencies=[Depends(require_api_token)])
def validate_api_token() -> dict:
    return {"ok": True}


@app.post("/api/devices/register", dependencies=[Depends(require_api_token)])
def register_device(
    device: DeviceIn,
    settings: Settings = Depends(get_settings),
) -> dict:
    ensure_db(settings)
    with locked():
        with connect(settings.db_path) as conn:
            from .db import upsert_device

            upsert_device(conn, device)
            conn.commit()
    return {"ok": True, "device_id": device.id}


@app.post("/api/sms/sync", response_model=SmsSyncResponse, dependencies=[Depends(require_api_token)])
async def sync_sms(
    payload: SmsSyncRequest,
    settings: Settings = Depends(get_settings),
) -> SmsSyncResponse:
    ensure_db(settings)
    with locked():
        with connect(settings.db_path) as conn:
            messages, duplicates = insert_messages(conn, payload.device, payload.messages)

    if messages:
        await manager.broadcast({"type": "sms.inserted", "messages": messages})

    return SmsSyncResponse(
        inserted=len(messages),
        duplicates=duplicates,
        messages=messages,
    )


@app.get("/api/sms/recent", response_model=SmsListResponse, dependencies=[Depends(require_api_token)])
def recent_sms(
    limit: int = Query(default=100, ge=1, le=1000),
    settings: Settings = Depends(get_settings),
) -> SmsListResponse:
    ensure_db(settings)
    with locked():
        with connect(settings.db_path) as conn:
            messages = list_messages(
                conn,
                device_id=None,
                limit=limit,
                q=None,
                since_ms=None,
                until_ms=None,
            )
    return SmsListResponse(count=len(messages), messages=messages)


@app.get("/api/sms", response_model=SmsListResponse, dependencies=[Depends(require_api_token)])
def query_sms(
    device_id: str | None = None,
    q: str | None = None,
    since: datetime | None = None,
    until: datetime | None = None,
    limit: int = Query(default=100, ge=1, le=1000),
    settings: Settings = Depends(get_settings),
) -> SmsListResponse:
    ensure_db(settings)
    with locked():
        with connect(settings.db_path) as conn:
            messages = list_messages(
                conn,
                device_id=device_id,
                limit=limit,
                q=q,
                since_ms=epoch_ms(since) if since else None,
                until_ms=epoch_ms(until) if until else None,
            )
    return SmsListResponse(count=len(messages), messages=messages)


@app.websocket("/ws/viewer")
async def websocket_viewer(
    websocket: WebSocket,
    settings: Settings = Depends(get_settings),
) -> None:
    if not await require_ws_token(websocket, settings):
        await websocket.close(code=1008)
        return

    await manager.connect(websocket)
    await websocket.send_json({"type": "connected"})
    try:
        while True:
            await websocket.receive_text()
    except WebSocketDisconnect:
        manager.disconnect(websocket)
