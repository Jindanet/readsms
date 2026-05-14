from pathlib import Path
import os
import sys


SERVER_DIR = Path(__file__).resolve().parent / "server"
sys.path.insert(0, str(SERVER_DIR))
os.chdir(SERVER_DIR)

from run import main  # noqa: E402


if __name__ == "__main__":
    main()
