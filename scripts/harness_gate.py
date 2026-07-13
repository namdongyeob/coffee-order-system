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
    "metrics.md",
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
EXECUTION_MODE_DECLARATION_PATTERN = re.compile(
    r"^Execution mode:[^\r\n]*$", re.MULTILINE
)
EXECUTION_MODE_REASON_DECLARATION_PATTERN = re.compile(
    r"^Execution mode reason:[^\r\n]*$", re.MULTILINE
)
VALID_EXECUTION_MODE_PATTERN = re.compile(
    r"^Execution mode:[ \t]*(SOLO|STANDARD|STRICT)[ \t]*$"
)
VALID_EXECUTION_MODE_REASON_PATTERN = re.compile(
    r"^Execution mode reason:[ \t]*\S[^\r\n]*$"
)
METRICS_COLUMNS = (
    "실행 모드",
    "Agent 수",
    "작업 시간(분)",
    "재시도 수",
    "정체 수",
    "Review 결함 수",
    "QA 결함 수",
    "범위 밖 변경 파일 수",
    "읽은 핵심 문서 수",
)
SOLO_FORBIDDEN_PREFIXES = ("src/", "gradle/", "docker/")
SOLO_FORBIDDEN_FILES = {"build.gradle", "settings.gradle", "gradlew", "gradlew.bat"}
STRICT_ONLY_PREFIXES = ("scripts/", ".github/workflows/")
VERIFICATION_LOG_COLUMNS = (
    "날짜",
    "Issue",
    "Level",
    "결과",
    "검증 범위",
    "명령/Evidence",
    "비고",
)
VALID_VERIFICATION_LEVELS = tuple(f"Level {level}" for level in range(8))
VALID_VERIFICATION_RESULTS = ("PASS", "FAIL", "PARTIAL")
VERIFICATION_LOG_HEADER = "# 검증 로그\n\n| " + " | ".join(VERIFICATION_LOG_COLUMNS) + " |\n| " + " | ".join("---" for _ in VERIFICATION_LOG_COLUMNS) + " |\n"
STRICT_AGENT_ROLES = frozenset({"Dev", "Review", "QA", "Docs"})
QA_PRESERVING_DOCS = frozenset()
QA_PRESERVING_EVIDENCE_FILES = frozenset(
    {
        "acceptance-criteria.md",
        "attempt-log.md",
        "commands.md",
        "manual-qa.md",
        "metrics.md",
        "verification.md",
    }
)
ROLE_PACKET_REQUIRED_FIELDS = frozenset(
    {
        "issue_url",
        "worktree_path",
        "base_sha",
        "head_sha",
        "acceptance_criteria",
        "required_documents",
        "diff_scope",
    }
)
ROLE_PACKET_ALLOWED_FIELDS = ROLE_PACKET_REQUIRED_FIELDS.union({"previous_p0_p1_finding"})
ROLE_PACKET_DOCUMENT_PATH_PATTERN = re.compile(r"^(?:AGENTS\.md|docs/(?:ai|testing)/[^/]+\.md|\.codex/skills/[^/]+/SKILL\.md)$")
REPOSITORY_ROOT = Path(__file__).resolve().parents[1]


def pre_review_ready(*, dev_verified: bool, evidence_ready: bool, pr_body_preflight_passed: bool) -> bool:
    """Allow Review after only the inputs that exist before Review."""
    return dev_verified and evidence_ready and pr_body_preflight_passed


def strict_agent_role_count(roles: list[str]) -> int:
    """Count unique STRICT roles; Coordinator, CI, and retries are excluded."""
    return len(STRICT_AGENT_ROLES.intersection(roles))


def required_evidence_exists(file_names: list[str] | tuple[str, ...]) -> bool:
    """Return whether the lightweight preflight has every base evidence file."""
    return set(REQUIRED_EVIDENCE_FILES).issubset(file_names)


def validate_role_packet(packet: dict[str, object]) -> list[str]:
    """Enforce the minimal packet schema without copied source or conversation payloads."""
    errors = [
        f"missing role packet field: {field}"
        for field in sorted(ROLE_PACKET_REQUIRED_FIELDS)
        if not packet.get(field)
    ]
    errors.extend(
        f"non-allowlisted role packet field: {field}"
        for field in packet
        if field not in ROLE_PACKET_ALLOWED_FIELDS
    )
    documents = packet.get("required_documents")
    if not isinstance(documents, list) or not 3 <= len(documents) <= 5:
        errors.append("role packet requires 3~5 canonical document paths.")
    elif any(
        not isinstance(document, str)
        or ROLE_PACKET_DOCUMENT_PATH_PATTERN.fullmatch(document) is None
        for document in documents
    ):
        errors.append("role packet requires only canonical document paths.")
    elif len(set(documents)) != len(documents):
        errors.append("role packet requires distinct canonical document paths.")
    elif any(not (REPOSITORY_ROOT / document).is_file() for document in documents):
        errors.append("role packet requires existing canonical document paths.")
    return errors


def post_qa_requirements(*, repository_changed: bool, changed_paths: list[str]) -> dict[str, bool]:
    """Require fresh Review and QA only after a repository change following QA."""
    stale = repository_changed and bool(changed_paths)
    return {
        "docs_commit_required": False,
        "full_review_required": stale,
        "qa_stale": stale,
    }


def github_state_requires_repository_commit() -> bool:
    """Keep mutable Review, QA, CI, and mergeability state in GitHub only."""
    return False


def verification_owner(verification: str, *, broad_risk: bool) -> str:
    """Separate focused Dev work, independent QA risk checks, and CI regression."""
    if verification == "full-regression":
        return "Dev" if broad_risk else "CI"
    if verification == "focused":
        return "Dev"
    if verification == "independent-risk":
        return "QA"
    raise ValueError(f"unknown verification type: {verification}")


def flaky_next_action(
    *, diff_related: bool, isolated_result: str | None, ci_passed: bool | None, blocker_state: str | None
) -> str:
    """Keep current-diff failures out of the bounded out-of-scope flaky path."""
    if diff_related:
        return "current-issue-defect"
    if blocker_state in {"production-change-required", "cause-unknown", "stabilization-failed"}:
        return "blocked-safe-stop"
    if isolated_result == "PASS" and ci_passed:
        return "continue-with-flaky-candidate"
    if isolated_result == "FAIL":
        return "create-test-only-blocker"
    return "blocked-safe-stop"


def blocked_wakeup_requires_work(*, external_state_changed: bool) -> bool:
    """Prevent repeated dispatch and verification while a blocker is unchanged."""
    return external_state_changed


def qa_remains_valid(
    qa_head: str,
    current_head: str,
    changed_paths: list[str],
    issue_number: int,
) -> bool:
    """Keep QA only when a later commit changes Issue evidence allowlist docs."""
    if qa_head == current_head:
        return True
    if not changed_paths:
        return False
    evidence_directory = f"docs/testing/evidence/issue-{issue_number}"
    allowed_paths = QA_PRESERVING_DOCS.union(
        f"{evidence_directory}/{file_name}"
        for file_name in QA_PRESERVING_EVIDENCE_FILES
    )
    return all(
        path in allowed_paths
        for path in changed_paths
    )


def autonomous_merge_ready(
    *,
    review_approved: bool,
    qa_passed: bool,
    docs_evidence_ready: bool,
    ci_passed: bool,
    review_head: str,
    current_head: str,
    mergeable_clean: bool,
) -> bool:
    """Apply the existing #60 merge gate without storing mutable snapshots."""
    return all((review_approved, qa_passed, docs_evidence_ready, ci_passed,
                review_head == current_head, mergeable_clean))


def next_issue_allowed(merged: bool, issue_closed: bool, merge_commit_exists: bool) -> bool:
    """Start the next queue item only after merge and close are observable."""
    return merged and issue_closed and merge_commit_exists


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


def validate_execution_mode_fields(markdown: str, path: Path | None = None) -> list[str]:
    """Require exactly one valid execution mode and one non-empty reason."""
    location = f"{path}: " if path else ""
    errors: list[str] = []

    mode_declarations = EXECUTION_MODE_DECLARATION_PATTERN.findall(markdown)
    if len(mode_declarations) > 1:
        errors.append(f"{location}duplicate Execution mode declarations are not allowed.")
    elif len(mode_declarations) == 0 or VALID_EXECUTION_MODE_PATTERN.fullmatch(
        mode_declarations[0]
    ) is None:
        errors.append(f"{location}Execution mode: SOLO|STANDARD|STRICT 선언이 없습니다.")

    reason_declarations = EXECUTION_MODE_REASON_DECLARATION_PATTERN.findall(markdown)
    if len(reason_declarations) > 1:
        errors.append(f"{location}duplicate Execution mode reason declarations are not allowed.")
    elif len(reason_declarations) == 0 or VALID_EXECUTION_MODE_REASON_PATTERN.fullmatch(
        reason_declarations[0]
    ) is None:
        errors.append(f"{location}Execution mode reason이 비어 있습니다.")

    return errors


def extract_execution_mode(markdown: str) -> str | None:
    """Return the sole valid execution mode, or None when it is absent or invalid."""
    declarations = EXECUTION_MODE_DECLARATION_PATTERN.findall(markdown)
    if len(declarations) == 1:
        match = VALID_EXECUTION_MODE_PATTERN.fullmatch(declarations[0])
        return match.group(1) if match else None
    if declarations:
        return None

    metrics_header = "| " + " | ".join(METRICS_COLUMNS) + " |"
    lines = markdown.splitlines()
    try:
        header_index = next(index for index, line in enumerate(lines) if line.strip() == metrics_header)
    except StopIteration:
        return None
    if header_index + 2 >= len(lines):
        return None
    cells = _split_markdown_table_row(lines[header_index + 2])
    return cells[0] if len(cells) == len(METRICS_COLUMNS) and cells[0] in {"SOLO", "STANDARD", "STRICT"} else None


def validate_execution_mode_consistency(
    acceptance_markdown: str,
    metrics_markdown: str,
    pr_body_markdown: str | None = None,
) -> list[str]:
    """Require acceptance, metrics, and optional PR body to declare one mode."""
    acceptance_mode = extract_execution_mode(acceptance_markdown)
    metrics_mode = extract_execution_mode(metrics_markdown)
    errors: list[str] = []
    if acceptance_mode is not None and metrics_mode is not None and acceptance_mode != metrics_mode:
        errors.append("Execution mode mismatch: acceptance-criteria.md and metrics.md must match.")
    if pr_body_markdown is not None:
        pr_mode = extract_execution_mode(pr_body_markdown)
        if acceptance_mode is not None and pr_mode is not None and acceptance_mode != pr_mode:
            errors.append("Execution mode mismatch: acceptance-criteria.md and pull request body must match.")
    return errors


def validate_pr_body(markdown: str, path: Path | None = None) -> list[str]:
    """Validate machine-readable execution fields in a pull request body."""
    return validate_execution_mode_fields(markdown, path)


def validate_acceptance_criteria(markdown: str, path: Path | None = None) -> list[str]:
    """Require machine-readable execution and Level 5/6 decisions."""
    location = f"{path}: " if path else ""
    errors = validate_execution_mode_fields(markdown, path)
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


def _single_metadata_value(markdown: str, field: str) -> str | None:
    matches = re.findall(rf"^{re.escape(field)}:[ \t]*(\S[^\r\n]*)$", markdown, re.MULTILINE)
    return matches[0].strip() if len(matches) == 1 else None


def attempt_reconciliation_state(markdown: str) -> tuple[dict[str, str] | None, list[str]]:
    """Read the machine-readable terminal state that gates Issue completion evidence."""
    errors: list[str] = []
    disposition = _single_metadata_value(markdown, "Current disposition")
    attempt = _single_metadata_value(markdown, "Current Attempt")
    head = _single_metadata_value(markdown, "Current head")
    if disposition not in {"PASS", "BLOCKED"}:
        errors.append("attempt-log.md Current disposition must be PASS or BLOCKED.")
    if attempt is None or re.fullmatch(r"[1-9]\d*", attempt) is None:
        errors.append("attempt-log.md Current Attempt must be a positive integer.")
    if head is None or re.fullmatch(r"[0-9a-fA-F]{7,40}", head) is None:
        errors.append("attempt-log.md Current head must be a Git commit SHA.")
    attempts = [int(value) for value in re.findall(r"^## Attempt ([1-9]\d*)\s*$", markdown, re.MULTILINE)]
    if attempt is not None and attempts and int(attempt) != max(attempts):
        errors.append("attempt-log.md Current Attempt must match the latest Attempt heading.")
    if errors:
        return None, errors
    return {"disposition": disposition, "attempt": attempt, "head": head.lower()}, errors


def verification_reconciliation_state(markdown: str) -> tuple[dict[str, str] | None, list[str]]:
    """Read the verification metadata paired with the current Attempt state."""
    errors: list[str] = []
    attempt = _single_metadata_value(markdown, "Attempt")
    head = _single_metadata_value(markdown, "Head")
    if attempt is None or re.fullmatch(r"[1-9]\d*", attempt) is None:
        errors.append("verification.md Attempt must be a positive integer.")
    if head is None or re.fullmatch(r"[0-9a-fA-F]{7,40}", head) is None:
        errors.append("verification.md Head must be a Git commit SHA.")
    if errors:
        return None, errors
    return {"attempt": attempt, "head": head.lower()}, errors


def _split_markdown_table_row(line: str) -> list[str]:
    """Split a Markdown table row while preserving pipes inside inline code."""
    stripped = line.strip()
    if not (stripped.startswith("|") and stripped.endswith("|")):
        return []

    cells: list[str] = []
    cell: list[str] = []
    code_delimiter_length: int | None = None
    escaped = False
    content = stripped[1:-1]
    index = 0
    while index < len(content):
        char = content[index]
        if char == "`" and (code_delimiter_length is not None or not escaped):
            end = index
            while end < len(content) and content[end] == "`":
                end += 1
            delimiter_length = end - index
            if code_delimiter_length is None:
                code_delimiter_length = delimiter_length
            elif delimiter_length == code_delimiter_length:
                code_delimiter_length = None
            cell.append(content[index:end])
            escaped = False
            index = end
            continue
        if char == "|" and code_delimiter_length is None and not escaped:
            cells.append("".join(cell).strip())
            cell = []
        else:
            cell.append(char)
        escaped = char == "\\" and not escaped if code_delimiter_length is None else False
        index += 1
    cells.append("".join(cell).strip())
    return cells


def validate_metrics(markdown: str, path: Path | None = None) -> list[str]:
    """Validate the fixed Issue metrics table with exactly one data row."""
    location = f"{path}: " if path else ""
    header = "| " + " | ".join(METRICS_COLUMNS) + " |"
    separator = "| " + " | ".join("---" for _ in METRICS_COLUMNS) + " |"
    lines = markdown.splitlines()
    errors: list[str] = []
    try:
        header_index = next(index for index, line in enumerate(lines) if line.strip() == header)
    except StopIteration:
        return [f"{location}metrics.md requires the exact nine-column template header."]

    if header_index + 1 >= len(lines) or lines[header_index + 1].strip() != separator:
        errors.append(f"{location}metrics.md requires the exact nine-column table separator.")
        return errors

    data_rows = [
        _split_markdown_table_row(line)
        for line in lines[header_index + 2 :]
        if line.lstrip().startswith("|")
    ]
    if len(data_rows) != 1:
        errors.append(f"{location}metrics.md requires exactly one data row.")
    if not data_rows:
        return errors

    row = data_rows[0]
    if len(row) != len(METRICS_COLUMNS):
        return [f"{location}metrics.md data row requires exactly nine columns."]
    if row[0] not in {"SOLO", "STANDARD", "STRICT"}:
        errors.append(f"{location}metrics.md 실행 모드는 SOLO, STANDARD, STRICT 중 하나여야 합니다.")
    for index, column in enumerate(METRICS_COLUMNS[1:], start=1):
        value = row[index]
        if column == "작업 시간(분)":
            if value != "미측정" and re.fullmatch(r"0|[1-9]\d*", value) is None:
                errors.append(f"{location}metrics.md {column}은 0 이상의 정수 또는 미측정이어야 합니다.")
        elif re.fullmatch(r"0|[1-9]\d*", value) is None:
            errors.append(f"{location}metrics.md {column}은 0 이상의 정수여야 합니다.")
    return errors


def metrics_row(markdown: str) -> list[str] | None:
    header = "| " + " | ".join(METRICS_COLUMNS) + " |"
    lines = markdown.splitlines()
    try:
        header_index = next(index for index, line in enumerate(lines) if line.strip() == header)
    except StopIteration:
        return None
    return _split_markdown_table_row(lines[header_index + 2]) if header_index + 2 < len(lines) else None


def validate_evidence_reconciliation(
    acceptance: str, attempt_log: str, metrics: str, verification: str, issue: int
) -> list[str]:
    """Fail closed when current Issue evidence sources describe different completion states."""
    attempt_state, errors = attempt_reconciliation_state(attempt_log)
    verification_state, verification_errors = verification_reconciliation_state(verification)
    errors.extend(verification_errors)
    row = metrics_row(metrics)
    if attempt_state is None or verification_state is None or row is None:
        return errors

    if attempt_state["attempt"] != verification_state["attempt"]:
        errors.append("evidence reconciliation: Current Attempt and verification.md Attempt must match.")
    if attempt_state["head"] != verification_state["head"]:
        errors.append("evidence reconciliation: Current head and verification.md Head must match.")
    if int(row[3]) != int(attempt_state["attempt"]) - 1:
        errors.append("evidence reconciliation: metrics retry count must equal Current Attempt minus one.")

    rows, row_errors = _verification_rows(verification)
    errors.extend(row_errors)
    issue_pattern = re.compile(rf"\bIssue\s*#\s*{issue}\b", re.IGNORECASE)
    has_pass = any(issue_pattern.search(row["Issue"]) and row["결과"] == "PASS" for row in rows)
    unchecked = re.findall(r"^- \[ \] .+", acceptance, re.MULTILINE)
    checked = re.findall(r"^- \[[xX]\] .+", acceptance, re.MULTILINE)
    if attempt_state["disposition"] == "BLOCKED" and has_pass:
        errors.append("evidence reconciliation: BLOCKED Current disposition cannot include PASS verification.")
    if attempt_state["disposition"] == "PASS" and not has_pass:
        errors.append("evidence reconciliation: PASS Current disposition requires PASS verification.")
    if attempt_state["disposition"] == "PASS" and (unchecked or not checked):
        errors.append("evidence reconciliation: PASS Current disposition requires every acceptance check to be checked.")
    if attempt_state["disposition"] == "BLOCKED" and checked and not unchecked:
        errors.append("evidence reconciliation: BLOCKED Current disposition requires an unchecked acceptance check.")
    return errors


def verification_file_path(issue: int) -> Path:
    """Return the Issue-local verification source path relative to the repository root."""
    return Path("docs") / "testing" / "evidence" / f"issue-{issue}" / "verification.md"


def verification_source_files(repository_root: Path) -> list[Path]:
    """Return every committed verification source in deterministic path order."""
    evidence_root = repository_root / "docs" / "testing" / "evidence"
    return sorted(evidence_root.glob("**/verification.md"), key=lambda path: path.as_posix())


def rebuild_verification_log(repository_root: Path) -> str:
    """Render the uncommitted global view from Issue-local and legacy sources."""
    rows: list[str] = []
    for source in verification_source_files(repository_root):
        lines = source.read_text(encoding="utf-8").splitlines()
        header = "| " + " | ".join(VERIFICATION_LOG_COLUMNS) + " |"
        try:
            start = next(index for index, line in enumerate(lines) if line.strip() == header) + 2
        except StopIteration:
            continue
        rows.extend(
            line for line in lines[start:]
            if line.strip() and line.lstrip().startswith("|")
        )
    return VERIFICATION_LOG_HEADER + "\n".join(rows) + ("\n" if rows else "")


def _verification_rows(markdown: str) -> tuple[list[dict[str, str]], list[str]]:
    lines = markdown.splitlines()
    expected_header = "| " + " | ".join(VERIFICATION_LOG_COLUMNS) + " |"
    expected_separator = "| " + " | ".join("---" for _ in VERIFICATION_LOG_COLUMNS) + " |"
    errors: list[str] = []
    rows: list[dict[str, str]] = []

    try:
        header_index = next(
            index for index, line in enumerate(lines) if line.strip() == expected_header
        )
    except StopIteration:
        return [], [
            "verification-log.md: expected header "
            "'날짜|Issue|Level|결과|검증 범위|명령/Evidence|비고'."
        ]

    separator_index = header_index + 1
    if separator_index >= len(lines) or lines[separator_index].strip() != expected_separator:
        errors.append(
            f"verification-log.md line {separator_index + 1}: expected seven-column table separator."
        )

    for index in range(separator_index + 1, len(lines)):
        line = lines[index]
        if not line.strip():
            continue
        if not line.lstrip().startswith("|"):
            continue

        cells = _split_markdown_table_row(line)
        if len(cells) != len(VERIFICATION_LOG_COLUMNS):
            errors.append(
                f"verification-log.md line {index + 1}: expected 7 columns "
                "(날짜|Issue|Level|결과|검증 범위|명령/Evidence|비고), "
                f"found {len(cells)}."
            )
            continue

        row = dict(zip(VERIFICATION_LOG_COLUMNS, cells))
        for column in VERIFICATION_LOG_COLUMNS:
            if not row[column]:
                errors.append(
                    f"verification-log.md line {index + 1}: column '{column}' must not be empty."
                )
        if re.fullmatch(r"\d{4}-\d{2}-\d{2}", row["날짜"]) is None:
            errors.append(
                f"verification-log.md line {index + 1}: invalid 날짜 '{row['날짜']}'; "
                "expected YYYY-MM-DD."
            )
        if row["Level"] not in VALID_VERIFICATION_LEVELS:
            errors.append(
                f"verification-log.md line {index + 1}: invalid Level '{row['Level']}'; "
                "allowed values are Level 0..Level 7."
            )
        if row["결과"] not in VALID_VERIFICATION_RESULTS:
            errors.append(
                f"verification-log.md line {index + 1}: invalid 결과 '{row['결과']}'; "
                "allowed values are PASS, FAIL, PARTIAL."
            )
        rows.append(row)

    return rows, errors


def validate_verification_log(
    markdown: str, issue: int, required_levels: tuple[int, ...] = ()
) -> list[str]:
    """Validate every log row and required PASS evidence for one Issue."""
    rows, errors = _verification_rows(markdown)
    issue_pattern = re.compile(rf"\bIssue\s*#\s*{issue}\b", re.IGNORECASE)
    issue_rows = [row for row in rows if issue_pattern.search(row["Issue"])]

    if not issue_rows:
        if issue_pattern.search(markdown) is None:
            errors.append(f"verification-log.md에 Issue #{issue} 기록이 없습니다.")
        else:
            errors.append(f"verification-log.md에 Issue #{issue} 결과 행이 없습니다.")

    for level in required_levels:
        expected_level = f"Level {level}"
        has_required_pass = any(
            row["Level"] == expected_level and row["결과"] == "PASS"
            for row in issue_rows
        )
        if not has_required_pass:
            errors.append(
                f"verification-log.md: Issue #{issue} required {expected_level} PASS is missing; "
                f"add a row with the same Issue, Level '{expected_level}', and result 'PASS'."
            )
    return errors


def required_verification_levels(acceptance_markdown: str) -> tuple[int, ...]:
    """Return Level 5/6 declarations that explicitly require PASS evidence."""
    return tuple(
        level
        for level in (5, 6)
        if re.search(
            rf"^Level {level} required:[ \t]*YES[ \t]*$",
            acceptance_markdown,
            re.IGNORECASE | re.MULTILINE,
        )
        is not None
    )


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

    acceptance = ""
    acceptance_path = evidence_dir / "acceptance-criteria.md"
    if acceptance_path.is_file():
        acceptance = acceptance_path.read_text(encoding="utf-8")
        errors.extend(validate_acceptance_criteria(acceptance, acceptance_path))

    metrics = ""
    metrics_path = evidence_dir / "metrics.md"
    if metrics_path.is_file():
        metrics = metrics_path.read_text(encoding="utf-8")
        errors.extend(validate_metrics(metrics, metrics_path))
    if acceptance and metrics:
        errors.extend(validate_execution_mode_consistency(acceptance, metrics))

    attempt_log_path = evidence_dir / "attempt-log.md"
    if attempt_log_path.is_file():
        attempt_log = attempt_log_path.read_text(encoding="utf-8")
        errors.extend(validate_attempt_log(attempt_log, attempt_log_path))
    else:
        attempt_log = ""

    verification_log = repository_root / verification_file_path(issue)
    if not verification_log.is_file():
        errors.append(f"ERROR: missing Issue verification source: {verification_log}")
    else:
        verification = verification_log.read_text(encoding="utf-8")
        errors.extend(validate_verification_log(verification, issue, required_verification_levels(acceptance)))
        if acceptance and metrics and attempt_log:
            errors.extend(validate_evidence_reconciliation(acceptance, attempt_log, metrics, verification, issue))

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


def validate_context_router_paths(repository_root: Path) -> list[str]:
    """Validate every repository-relative Markdown link declared by the Context Router."""
    router = repository_root / "docs" / "ai" / "context-router.md"
    if not router.is_file():
        return [f"ERROR: missing Context Router: {router}"]
    return check_relative_links(repository_root, [router])


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


def changed_paths(repository_root: Path, base_ref: str) -> list[str]:
    """Return changed repository-relative paths between the base ref and HEAD."""
    return [
        path
        for path in _git_output(repository_root, "diff", "--name-only", f"{base_ref}...HEAD").splitlines()
        if path
    ]


def issue_evidence_allowlist(issue: int) -> set[str]:
    """Return the only post-verification files allowed to advance an execution head."""
    directory = f"docs/testing/evidence/issue-{issue}"
    return {
        f"{directory}/acceptance-criteria.md",
        f"{directory}/attempt-log.md",
        f"{directory}/commands.md",
        f"{directory}/manual-qa.md",
        f"{directory}/metrics.md",
        f"{directory}/verification.md",
    }


def validate_execution_head_delta(
    execution_head: str, is_ancestor: bool, changed_since_execution_head: list[str], issue: int
) -> list[str]:
    """Allow only current-Issue evidence commits after the verified execution head."""
    if not is_ancestor:
        return ["evidence reconciliation: execution head must be an ancestor of current Git HEAD."]
    disallowed = sorted(set(changed_since_execution_head) - issue_evidence_allowlist(issue))
    if not disallowed:
        return []
    return [
        "evidence reconciliation: changes after execution head must be limited to "
        f"Issue #{issue} evidence files: {', '.join(disallowed)}"
    ]


def validate_execution_head(repository_root: Path, execution_head: str, issue: int) -> list[str]:
    """Compare evidence execution head with current Git history without accepting stale code changes."""
    ancestor = subprocess.run(
        ["git", "merge-base", "--is-ancestor", execution_head, "HEAD"],
        cwd=repository_root,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )
    if ancestor.returncode == 1:
        return validate_execution_head_delta(execution_head, False, [], issue)
    if ancestor.returncode != 0:
        detail = ancestor.stderr.strip() or "git merge-base failed"
        return [f"evidence reconciliation: cannot validate execution head: {detail}"]
    try:
        changed = _git_output(repository_root, "diff", "--name-only", f"{execution_head}..HEAD")
    except RuntimeError as error:
        return [f"evidence reconciliation: cannot read execution head delta: {error}"]
    return validate_execution_head_delta(execution_head, True, changed.splitlines(), issue)


def validate_changed_path_mode(paths: list[str], mode: str | None) -> list[str]:
    """Reject execution modes that do not cover changed production or gate paths."""
    if mode is None:
        return []
    normalized_paths = [path.replace("\\", "/") for path in paths]
    errors: list[str] = []
    if mode == "SOLO" and any(
        path.startswith(SOLO_FORBIDDEN_PREFIXES) or path in SOLO_FORBIDDEN_FILES
        for path in normalized_paths
    ):
        errors.append("Execution mode SOLO is not allowed when production, build, or Docker paths change.")
    if mode != "STRICT" and any(
        path.startswith(STRICT_ONLY_PREFIXES) for path in normalized_paths
    ):
        errors.append("Execution mode STRICT is required when scripts/ or .github/workflows/ paths change.")
    return errors


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
    parser.add_argument(
        "--pr-body-file",
        type=Path,
        help="Read and validate Execution mode fields from a pull request body file.",
    )
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
    issue = args.issue if args.issue is not None else infer_issue_number(branch)
    pr_body = None

    if args.pr_body_file is not None:
        try:
            pr_body = args.pr_body_file.read_text(encoding="utf-8")
            errors.extend(validate_pr_body(pr_body, args.pr_body_file))
        except OSError as error:
            errors.append(f"ERROR: cannot read pull request body file {args.pr_body_file}: {error}")

    if not args.links_only and not args.check_branch:
        if issue is None:
            errors.append(
                "ERROR: issue number is required. Use --issue N or a branch name containing issue-N."
            )
        else:
            errors.extend(validate_issue_evidence(repository_root, issue))
            evidence_dir = repository_root / "docs" / "testing" / "evidence" / f"issue-{issue}"
            acceptance_path = evidence_dir / "acceptance-criteria.md"
            metrics_path = evidence_dir / "metrics.md"
            attempt_path = evidence_dir / "attempt-log.md"
            if attempt_path.is_file():
                attempt_state, attempt_errors = attempt_reconciliation_state(
                    attempt_path.read_text(encoding="utf-8")
                )
                errors.extend(attempt_errors)
                if attempt_state is not None:
                    errors.extend(
                        validate_execution_head(repository_root, attempt_state["head"], issue)
                    )
            if pr_body is not None:
                if acceptance_path.is_file() and metrics_path.is_file():
                    errors.extend(
                        validate_execution_mode_consistency(
                            acceptance_path.read_text(encoding="utf-8"),
                            metrics_path.read_text(encoding="utf-8"),
                            pr_body,
                        )
                    )
            if acceptance_path.is_file():
                try:
                    errors.extend(
                        validate_changed_path_mode(
                            changed_paths(repository_root, args.base_ref),
                            extract_execution_mode(acceptance_path.read_text(encoding="utf-8")),
                        )
                    )
                except RuntimeError as error:
                    errors.append(f"ERROR: cannot determine changed paths from {args.base_ref}: {error}")

    if args.check_links or args.links_only:
        try:
            markdown_files = changed_markdown_files(repository_root, args.base_ref, args.include_worktree)
            errors.extend(check_relative_links(repository_root, markdown_files))
            errors.extend(validate_context_router_paths(repository_root))
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
