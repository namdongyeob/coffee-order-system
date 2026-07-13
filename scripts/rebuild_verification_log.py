"""Issue별 검증 정본에서 커밋하지 않는 전역 뷰를 재현한다."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

try:
    from scripts.harness_gate import harden_console_encoding, rebuild_verification_log
except ModuleNotFoundError:
    from harness_gate import harden_console_encoding, rebuild_verification_log


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Render the verification log global view.")
    parser.add_argument("--output", type=Path, help="Optional output path. Omit to write stdout.")
    args = parser.parse_args(argv)

    output = rebuild_verification_log(Path(__file__).resolve().parents[1])
    if args.output is None:
        sys.stdout.write(output)
    else:
        args.output.write_text(output, encoding="utf-8")
    return 0


if __name__ == "__main__":
    harden_console_encoding()
    sys.exit(main())
