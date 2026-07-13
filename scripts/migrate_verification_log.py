"""기존 전역 검증 로그를 원문 행 보존 Issue별 정본으로 한 번 이관한다."""

from __future__ import annotations

import argparse
import re
import sys
from collections import defaultdict
from pathlib import Path

try:
    from scripts.harness_gate import VERIFICATION_LOG_COLUMNS, VERIFICATION_LOG_HEADER
except ModuleNotFoundError:
    from harness_gate import VERIFICATION_LOG_COLUMNS, VERIFICATION_LOG_HEADER


ISSUE_PATTERN = re.compile(r"\bIssue\s*#\s*(\d+)\b", re.IGNORECASE)


def migrate(repository_root: Path, source: Path) -> list[Path]:
    """Copy each original table row unchanged into its Issue or legacy source."""
    lines = source.read_text(encoding="utf-8").splitlines()
    header = "| " + " | ".join(VERIFICATION_LOG_COLUMNS) + " |"
    try:
        start = next(index for index, line in enumerate(lines) if line.strip() == header) + 2
    except StopIteration as error:
        raise ValueError(f"verification table header is missing: {source}") from error

    grouped: dict[str, list[str]] = defaultdict(list)
    for line in lines[start:]:
        if not line.strip() or not line.lstrip().startswith("|"):
            continue
        cells = line.split("|", 3)
        issue_match = ISSUE_PATTERN.search(cells[2] if len(cells) > 2 else "")
        key = f"issue-{issue_match.group(1)}" if issue_match else "legacy"
        grouped[key].append(line)

    created: list[Path] = []
    evidence_root = repository_root / "docs" / "testing" / "evidence"
    for key, rows in sorted(grouped.items()):
        target = evidence_root / key / "verification.md"
        if target.exists():
            raise FileExistsError(f"migration target already exists: {target}")
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(VERIFICATION_LOG_HEADER + "\n".join(rows) + "\n", encoding="utf-8")
        created.append(target)
    return created


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Migrate the global verification log once.")
    parser.add_argument("--source", type=Path, default=Path("docs/testing/verification-log.md"))
    parser.add_argument("--delete-source", action="store_true")
    args = parser.parse_args(argv)
    repository_root = Path(__file__).resolve().parents[1]
    source = args.source if args.source.is_absolute() else repository_root / args.source
    created = migrate(repository_root, source)
    if args.delete_source:
        source.unlink()
    print(f"Migrated {len(created)} verification sources.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
