from functools import lru_cache
from pathlib import Path
import os
import secrets

from pydantic import BaseModel, Field


PLACEHOLDER_TOKENS = {"change-this-to-a-long-secret", "dev-secret-change-me"}


class Settings(BaseModel):
    host: str = Field(default="0.0.0.0")
    port: int = Field(default=9201, ge=1, le=65535)
    reload: bool = Field(default=False)
    api_token: str
    db_path: str
    retention_days: int = Field(default=1, ge=1, le=365)
    cors_origins: list[str] = Field(default_factory=lambda: ["*"])


def _server_dir() -> Path:
    return Path(__file__).resolve().parents[1]


def _default_db_path() -> str:
    return str(_server_dir() / "sms.json")


def _env_path() -> Path:
    return _server_dir() / ".env"


def _generate_api_token() -> str:
    return secrets.token_hex(32)


def ensure_env_file() -> None:
    path = _env_path()
    if path.exists():
        return

    path.write_text(
        "\n".join(
            [
                "READSMS_HOST=0.0.0.0",
                "READSMS_PORT=9201",
                "READSMS_RELOAD=false",
                f"READSMS_API_TOKEN={_generate_api_token()}",
                "READSMS_DB_PATH=./sms.json",
                "READSMS_RETENTION_DAYS=1",
                "READSMS_CORS_ORIGINS=*",
                "",
            ],
        ),
        encoding="utf-8",
    )


def _parse_bool(value: str | None, default: bool) -> bool:
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "y", "on"}


def _resolve_db_path(value: str | None) -> str:
    if not value:
        return _default_db_path()
    path = Path(value)
    if not path.is_absolute():
        path = _server_dir() / path
    return str(path)


def load_env_file() -> None:
    ensure_env_file()
    path = _env_path()

    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        key = key.strip()
        value = value.strip().strip('"').strip("'")
        if key:
            os.environ.setdefault(key, value)


@lru_cache
def get_settings() -> Settings:
    load_env_file()
    origins = os.getenv("READSMS_CORS_ORIGINS", "*")
    api_token = os.getenv("READSMS_API_TOKEN")
    if not api_token:
        raise RuntimeError(f"Missing READSMS_API_TOKEN in {_env_path()}")
    if api_token in PLACEHOLDER_TOKENS:
        raise RuntimeError(
            f"READSMS_API_TOKEN in {_env_path()} is still a placeholder. "
            "Replace it with a long random token.",
        )

    return Settings(
        host=os.getenv("READSMS_HOST", "0.0.0.0"),
        port=int(os.getenv("READSMS_PORT", "9201")),
        reload=_parse_bool(os.getenv("READSMS_RELOAD"), False),
        api_token=api_token,
        db_path=_resolve_db_path(os.getenv("READSMS_DB_PATH")),
        retention_days=int(os.getenv("READSMS_RETENTION_DAYS", "1")),
        cors_origins=[item.strip() for item in origins.split(",") if item.strip()],
    )
