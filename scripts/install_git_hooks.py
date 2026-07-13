"""Install this repository's version-controlled Git hooks for the current clone."""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path


def harden_console_encoding() -> None:
    """cp949 등 non-UTF-8 콘솔에서 인코딩 불가 문자로 인한 UnicodeEncodeError 크래시를 막는다."""
    for stream in (sys.stdout, sys.stderr):
        if hasattr(stream, "reconfigure"):
            stream.reconfigure(errors="backslashreplace")


def main() -> int:
    repository_root = Path(__file__).resolve().parents[1]
    result = subprocess.run(
        ["git", "config", "--local", "core.hooksPath", ".githooks"],
        cwd=repository_root,
        text=True,
        check=False,
    )
    if result.returncode != 0:
        print("ERROR: could not set core.hooksPath=.githooks for this repository.", file=sys.stderr)
        return result.returncode or 1

    print("Installed repository Git hooks: core.hooksPath=.githooks")
    return 0


if __name__ == "__main__":
    harden_console_encoding()
    sys.exit(main())
