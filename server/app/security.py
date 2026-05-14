from hmac import compare_digest

from fastapi import Depends, Header, HTTPException, WebSocket, status

from .config import Settings, get_settings


def _extract_bearer(authorization: str | None) -> str | None:
    if not authorization:
        return None
    scheme, _, value = authorization.partition(" ")
    if scheme.lower() != "bearer" or not value:
        return None
    return value.strip()


def require_api_token(
    authorization: str | None = Header(default=None),
    x_api_token: str | None = Header(default=None),
    settings: Settings = Depends(get_settings),
) -> None:
    token = _extract_bearer(authorization) or x_api_token
    if not token or not compare_digest(token, settings.api_token):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid API token",
        )


async def require_ws_token(websocket: WebSocket, settings: Settings) -> bool:
    query_token = websocket.query_params.get("token")
    auth_token = _extract_bearer(websocket.headers.get("authorization"))
    token = query_token or auth_token
    return bool(token and compare_digest(token, settings.api_token))
