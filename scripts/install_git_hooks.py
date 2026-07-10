"""Install this repository's version-controlled Git hooks for the current clone."""

from __future__ import annotations

import subprocess
import sys
from pathlib import Path


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
    sys.exit(main())
