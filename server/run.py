from app.config import get_settings
from pathlib import Path
import socket
import sys


def _best_lan_ip() -> str | None:
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
            sock.connect(("8.8.8.8", 80))
            return sock.getsockname()[0]
    except OSError:
        return None


def _print_startup_hint(settings) -> None:
    lan_ip = _best_lan_ip()
    shown_host = "127.0.0.1" if settings.host in {"0.0.0.0", "::"} else settings.host
    print("")
    print("ReadSMS server")
    print(f"Config: {Path(__file__).resolve().parent / '.env'}")
    print(f"Local:  http://{shown_host}:{settings.port}")
    if settings.host == "0.0.0.0" and lan_ip:
        print(f"Phone:  http://{lan_ip}:{settings.port}")
    print("Stop:   Ctrl+C")
    print("")


def main() -> None:
    try:
        import uvicorn
    except ModuleNotFoundError:
        print("Missing packages. Run this once:")
        print("  pip install -r server\\requirements.txt")
        sys.exit(1)

    try:
        settings = get_settings()
    except RuntimeError as exc:
        print(f"Config error: {exc}")
        sys.exit(1)

    _print_startup_hint(settings)
    uvicorn.run("app.main:app", host=settings.host, port=settings.port, reload=settings.reload)


if __name__ == "__main__":
    main()
