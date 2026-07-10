"""Repository-local quality checks required before an Issue branch is pushed."""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from pathlib import Path
from urllib.parse import unquote


PROTECTED_BRANCHES = {"main", "master"}
REQUIRED_EVIDENCE_FILES = (
    "acceptance-criteria.md",
    "attempt-log.md",
    "commands.md",
    "manual-qa.md",
)
ATTEMPT_LOG_SECTIONS = (
    "## Attempt",
    "### Generate",
    "### Evaluate",
    "### Failure Cause",
    "### Change Scope",
    "### Reverification",
    "### Next Attempt",
)
REFERENCE_LINK_PATTERN = re.compile(
    r"^\s*\[[^\]]+\]:\s*(?:<([^>]+)>|([^\s]+))", re.MULTILINE
)


def validate_branch(branch: str) -> list[str]:
    """Reject direct commits from branches that must be updated by pull request."""
    if branch.strip() in PROTECTED_BRANCHES:
        return [
            f"ERROR: protected branch '{branch}' cannot commit directly. "
            "Create an Issue branch and open a pull request."
        ]
    return []


def branch_is_allowed(branch: str) -> bool:
    """Return whether commits are allowed on the supplied branch."""
    return not validate_branch(branch)


def infer_issue_number(branch: str) -> int | None:
    """Extract the Issue number from a conventional issue-N branch name."""
    match = re.search(r"(?:^|/)issue-(\d+)(?:$|[-/])", branch)
    return int(match.group(1)) if match else None


def validate_acceptance_criteria(markdown: str, path: Path | None = None) -> list[str]:
    """Require machine-readable Level 5/6 decisions and non-empty reasons."""
    location = f"{path}: " if path else ""
    errors: list[str] = []
    for level in (5, 6):
        required_pattern = rf"^Level {level} required:[ \t]*(YES|NO)[ \t]*$"
        reason_pattern = rf"^Level {level} reason:[ \t]*\S[^\r\n]*$"
        if re.search(required_pattern, markdown, re.IGNORECASE | re.MULTILINE) is None:
            errors.append(f"{location}Level {level} required: YES/NO 선언이 없습니다.")
        if re.search(reason_pattern, markdown, re.MULTILINE) is None:
            errors.append(f"{location}Level {level} reason이 비어 있습니다.")
    return errors


def validate_attempt_log(markdown: str, path: Path | None = None) -> list[str]:
    """Require every field needed to feed a failed attempt into the next attempt."""
    location = f"{path}: " if path else ""
    errors = [f"{location}attempt-log.md 필수 섹션 누락: {section}" for section in ATTEMPT_LOG_SECTIONS if section not in markdown]
    linkage_patterns = {
        "Issue": r"^Issue:\s*#\d+\s*$",
        "Issue URL": r"^Issue URL:\s*https://\S+\s*$",
        "Branch": r"^Branch:\s*\S+\s*$",
    }
    for field, pattern in linkage_patterns.items():
        if re.search(pattern, markdown, re.MULTILINE) is None:
            errors.append(f"{location}attempt-log.md {field} 연결 정보가 없습니다.")
    return errors


def validate_verification_log(markdown: str, issue: int) -> list[str]:
    if re.search(rf"Issue\s*#\s*{issue}\b", markdown) is None:
        return [f"verification-log.md에 Issue #{issue} 기록이 없습니다."]
    result_row = re.compile(
        rf"^\|[^\r\n]*Issue\s*#\s*{issue}\b[^\r\n]*\|\s*(?:PASS|FAIL|PARTIAL)\s*\|",
        re.IGNORECASE | re.MULTILINE,
    )
    if result_row.search(markdown) is None:
        return [f"verification-log.md에 Issue #{issue} 결과 행이 없습니다."]
    return []


def validate_issue_evidence(repository_root: Path, issue: int) -> list[str]:
    """Validate the minimum evidence needed to make an Issue completion claim."""
    evidence_dir = repository_root / "docs" / "testing" / "evidence" / f"issue-{issue}"
    errors: list[str] = []

    for name in REQUIRED_EVIDENCE_FILES:
        path = evidence_dir / name
        if not path.is_file():
            errors.append(f"ERROR: missing required evidence file: {path}")

    for name in ("commands.md", "manual-qa.md"):
        path = evidence_dir / name
        if path.is_file():
            meaningful_lines = [
                line for line in path.read_text(encoding="utf-8").splitlines()
                if line.strip() and not line.lstrip().startswith("#")
            ]
            if not meaningful_lines:
                errors.append(f"ERROR: {path} has no evidence content.")

    acceptance_path = evidence_dir / "acceptance-criteria.md"
    if acceptance_path.is_file():
        acceptance = acceptance_path.read_text(encoding="utf-8")
        errors.extend(validate_acceptance_criteria(acceptance, acceptance_path))

    attempt_log_path = evidence_dir / "attempt-log.md"
    if attempt_log_path.is_file():
        attempt_log = attempt_log_path.read_text(encoding="utf-8")
        errors.extend(validate_attempt_log(attempt_log, attempt_log_path))

    verification_log = repository_root / "docs" / "testing" / "verification-log.md"
    if not verification_log.is_file():
        errors.append(f"ERROR: missing verification log: {verification_log}")
    else:
        errors.extend(validate_verification_log(verification_log.read_text(encoding="utf-8"), issue))

    return errors


def _is_relative_link(target: str) -> bool:
    normalized = target.strip().strip("<>")
    return bool(normalized) and not (
        normalized.startswith(("#", "/", "\\", "http://", "https://", "mailto:", "data:"))
    )


def _inline_link_targets(markdown: str) -> list[str]:
    targets: list[str] = []
    for match in re.finditer(r"!?\[[^\]]*\]\(", markdown):
        start = match.end()
        depth = 0
        index = start
        while index < len(markdown):
            char = markdown[index]
            if char == "(":
                depth += 1
            elif char == ")":
                if depth == 0:
                    raw = markdown[start:index].strip()
                    if raw.startswith("<") and ">" in raw:
                        targets.append(raw[1:raw.index(">")])
                    elif raw:
                        targets.append(raw.split(maxsplit=1)[0])
                    break
                depth -= 1
            index += 1
    return targets


def _markdown_targets(markdown: str) -> list[str]:
    targets = _inline_link_targets(markdown)
    for match in REFERENCE_LINK_PATTERN.finditer(markdown):
        targets.append(match.group(1) or match.group(2))
    return targets


def check_relative_links(repository_root: Path, markdown_files: list[Path]) -> list[str]:
    """Return an error for each relative Markdown link that does not resolve."""
    root = repository_root.resolve()
    errors: list[str] = []

    for markdown_file in markdown_files:
        content = markdown_file.read_text(encoding="utf-8")
        for raw_target in _markdown_targets(content):
            target = raw_target.strip().strip("<>")
            if not _is_relative_link(target):
                continue
            relative_target = unquote(target.split("#", maxsplit=1)[0].split("?", maxsplit=1)[0])
            candidate = (markdown_file.parent / relative_target).resolve()
            if candidate != root and root not in candidate.parents:
                errors.append(f"ERROR: {markdown_file} link escapes repository: {target}")
            elif not candidate.exists():
                errors.append(f"ERROR: {markdown_file} has missing relative link: {target}")
    return errors


def validate_markdown_links(repository_root: Path, markdown_files: list[Path]) -> list[str]:
    """Compatibility name used by tests and callers."""
    return check_relative_links(repository_root, markdown_files)


def _git_output(repository_root: Path, *args: str) -> str:
    result = subprocess.run(
        ["git", *args],
        cwd=repository_root,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )
    if result.returncode != 0:
        message = result.stderr.strip() or "git command failed"
        raise RuntimeError(f"git {' '.join(args)}: {message}")
    return result.stdout


def changed_markdown_files(
    repository_root: Path, base_ref: str, include_worktree: bool = False
) -> list[Path]:
    """Find committed Markdown changes, optionally including the local worktree."""
    changed = _git_output(repository_root, "diff", "--name-only", f"{base_ref}...HEAD")
    worktree = ""
    untracked = ""
    if include_worktree:
        worktree = _git_output(repository_root, "diff", "--name-only", "HEAD")
        untracked = _git_output(repository_root, "ls-files", "--others", "--exclude-standard")
    relative_paths = {
        Path(line)
        for line in (changed + worktree + untracked).splitlines()
        if line.endswith(".md")
    }
    return sorted((repository_root / relative_path for relative_path in relative_paths if (repository_root / relative_path).is_file()), key=str)


def current_branch(repository_root: Path) -> str:
    return _git_output(repository_root, "branch", "--show-current").strip()


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Validate Issue evidence and changed Markdown links.")
    parser.add_argument("--issue", type=int, help="GitHub Issue number to validate.")
    parser.add_argument("--branch", help="Branch name to use instead of reading Git state.")
    parser.add_argument("--base-ref", default="origin/main", help="Git ref used to find changed Markdown files.")
    parser.add_argument("--check-links", action="store_true", help="Check relative links in Markdown changed from --base-ref.")
    parser.add_argument("--links-only", action="store_true", help="Run only the changed Markdown link check.")
    parser.add_argument("--check-branch", action="store_true", help="Check whether the current branch allows direct commits.")
    parser.add_argument("--include-worktree", action="store_true", help="Include uncommitted and untracked Markdown files in link checks.")
    args = parser.parse_args(argv)

    repository_root = Path(__file__).resolve().parents[1]
    errors: list[str] = []
    branch = args.branch
    try:
        if branch is None:
            branch = current_branch(repository_root)
    except RuntimeError as error:
        errors.append(f"ERROR: {error}")
        branch = ""

    if args.check_branch:
        errors.extend(validate_branch(branch))

    if not args.links_only and not args.check_branch:
        issue = args.issue if args.issue is not None else infer_issue_number(branch)
        if issue is None:
            errors.append(
                "ERROR: issue number is required. Use --issue N or a branch name containing issue-N."
            )
        else:
            errors.extend(validate_issue_evidence(repository_root, issue))

    if args.check_links or args.links_only:
        try:
            markdown_files = changed_markdown_files(repository_root, args.base_ref, args.include_worktree)
            errors.extend(check_relative_links(repository_root, markdown_files))
        except RuntimeError as error:
            errors.append(f"ERROR: cannot determine changed Markdown files from {args.base_ref}: {error}")

    if errors:
        print("Harness gate FAILED:")
        print("\n".join(errors))
        return 1

    print("Harness gate PASSED.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
