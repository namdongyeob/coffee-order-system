"""Repository-local quality checks required before an Issue branch is pushed."""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from urllib.parse import unquote


def harden_console_encoding() -> None:
    """cp949 등 non-UTF-8 콘솔에서 인코딩 불가 문자로 인한 UnicodeEncodeError 크래시를 막는다."""
    for stream in (sys.stdout, sys.stderr):
        if hasattr(stream, "reconfigure"):
            stream.reconfigure(errors="backslashreplace")


PROTECTED_BRANCHES = {"main", "master"}
REQUIRED_EVIDENCE_FILES = (
    "acceptance-criteria.md",
    "attempt-log.md",
    "commands.md",
    "manual-qa.md",
    "metrics.md",
)
LIGHTWEIGHT_EVIDENCE_START_ISSUE = 138
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
# Issue #57 replay로 확정한 ENFORCE 매핑만 코드화한다(M1/M2/M3). OBSERVE(M4~M7)는 구현하지 않고,
# src/test/**만 바뀐 경우(M8)는 이 패턴들이 src/main/java만 매치하므로 자연히 제외된다.
LEVEL_PATH_ENFORCE_RULES: tuple[tuple[int, re.Pattern[str]], ...] = (
    (2, re.compile(r"^src/main/java/.+/controller/[^/]+\.java$")),
    (4, re.compile(r"^src/main/java/.+/consumer/[^/]+\.java$")),
    (4, re.compile(r"^src/main/java/.+/order/event/[^/]+\.java$")),
)
# docs/testing/level-mapping-design.md(Issue #57)의 exemption code 체계. 자유 산문 사유는 인정하지 않는다.
LEVEL_EXEMPTION_CODES = frozenset(
    {
        "TEST_ONLY_IN_MATCHED_DIR",
        "NO_BEHAVIOR_CHANGE",
        "DOC_STRING_ONLY",
        "SUPERSEDED_BY_HIGHER_LEVEL",
        "HARNESS_SELF_CHANGE",
    }
)
LEVEL_EXEMPTION_PATTERN = re.compile(
    r"^Level exemption:[ \t]*(?P<level>[0-9]+)[ \t]+(?P<code>[A-Za-z_]+)[ \t]*[—-][ \t]*"
    r"(?P<path>\S+)[ \t]*[—-][ \t]*(?P<ref>\S+)[ \t]*$",
    re.MULTILINE,
)
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
ROLE_PACKET_REQUIRED_FIELDS = frozenset(
    {
        "issue_url",
        "worktree_path",
        "base_sha",
        "head_sha",
        "acceptance_criteria",
        "allowed_write_scope",
        "required_documents",
        "focused_verification",
        "diff_scope",
        "fork_turns",
        "subagent_stop",
        "output_budget",
    }
)
ROLE_PACKET_ALLOWED_FIELDS = ROLE_PACKET_REQUIRED_FIELDS.union(
    {"previous_p0_p1_finding", "last_failure_cause"}
)
ROLE_PACKET_DOCUMENT_PATH_PATTERN = re.compile(r"^(?:AGENTS\.md|docs/(?:ai|testing)/[^/]+\.md|\.codex/skills/[^/]+/SKILL\.md)$")
REPOSITORY_ROOT = Path(__file__).resolve().parents[1]


@dataclass(frozen=True)
class ChangeRecord:
    """One name-status entry from Git, including the old path for a rename."""

    status: str
    path: str
    previous_path: str | None = None


@dataclass(frozen=True)
class ChangeImpact:
    """The four independent outputs used by mode, CI, and stale gates."""

    execution_mode_floor: str
    requires_java_ci: bool
    invalidates_review_qa: bool
    invalidates_runtime_evidence: bool


MODE_RANK = {"SOLO": 0, "STANDARD": 1, "STRICT": 2}
FAIL_CLOSED_IMPACT = ChangeImpact("STRICT", True, True, True)
IMPACT_BY_CATEGORY = {
    "lightweight": ChangeImpact("SOLO", False, False, False),
    "source-test": ChangeImpact("STANDARD", True, True, True),
    "migration-build-runtime": ChangeImpact("STRICT", True, True, True),
    "workflow-harness-policy": ChangeImpact("STRICT", False, True, False),
    "api-domain-architecture": ChangeImpact("STANDARD", False, True, False),
}
BUILD_FILES = {
    "build.gradle",
    "build.gradle.kts",
    "settings.gradle",
    "settings.gradle.kts",
    "gradlew",
    "gradlew.bat",
}
RUNTIME_FILES = {"Dockerfile", "docker-compose.yml", "docker-compose.yaml"}


def _impact_category(path: str, issue: int) -> str:
    normalized = path.replace("\\", "/")
    evidence_prefix = f"docs/testing/evidence/issue-{issue}/"
    if normalized.startswith(evidence_prefix):
        return "evidence-neutral"
    if normalized == "README.md":
        return "lightweight"
    if normalized.startswith("src/main/resources/db/migration/"):
        return "migration-build-runtime"
    if normalized.startswith(("src/main/", "src/test/")):
        return "source-test"
    if normalized in BUILD_FILES or normalized.startswith("gradle/"):
        return "migration-build-runtime"
    if normalized in RUNTIME_FILES or normalized.startswith("docker/"):
        return "migration-build-runtime"
    if normalized.startswith(
        (
            ".github/workflows/",
            "scripts/",
            "docs/ai/",
            "docs/testing/",
            ".codex/skills/",
        )
    ) or normalized == "AGENTS.md":
        return "workflow-harness-policy"
    if normalized.startswith(("docs/api/", "docs/domain/", "docs/architecture/", "docs/adr/")):
        return "api-domain-architecture"
    return "unknown"


def classify_change_impact(changes: list[ChangeRecord], issue: int) -> ChangeImpact:
    """Classify a diff once; unknown, mixed, rename, and delete fail closed."""
    if any(change.status[:1] in {"R", "D"} for change in changes):
        return FAIL_CLOSED_IMPACT
    if any(change.status[:1] not in {"A", "M"} for change in changes):
        return FAIL_CLOSED_IMPACT
    categories = {_impact_category(change.path, issue) for change in changes}
    if "unknown" in categories:
        return FAIL_CLOSED_IMPACT
    substantive = categories - {"evidence-neutral"}
    if not substantive:
        return IMPACT_BY_CATEGORY["lightweight"]
    if len(substantive) != 1:
        return FAIL_CLOSED_IMPACT
    return IMPACT_BY_CATEGORY[next(iter(substantive))]


def validate_declared_mode_floor(mode: str | None, impact: ChangeImpact) -> list[str]:
    """Reject a declaration lower than the mode computed from the actual diff."""
    if mode not in MODE_RANK:
        return []
    if MODE_RANK[mode] < MODE_RANK[impact.execution_mode_floor]:
        return [
            f"Execution mode {mode} is lower than computed floor "
            f"{impact.execution_mode_floor}."
        ]
    return []


def required_evidence_files(issue: int, include_verification: bool = False) -> tuple[str, ...]:
    """Keep the old six-file bootstrap through #137, then require only AC and verification."""
    files = REQUIRED_EVIDENCE_FILES if issue < LIGHTWEIGHT_EVIDENCE_START_ISSUE else ("acceptance-criteria.md",)
    return files + (("verification.md",) if include_verification else ())


def pre_review_ready(*, dev_verified: bool, evidence_ready: bool, pr_body_preflight_passed: bool) -> bool:
    """Allow Review after only the inputs that exist before Review."""
    return dev_verified and evidence_ready and pr_body_preflight_passed


def strict_agent_role_count(roles: list[str]) -> int:
    """Count unique STRICT roles; Coordinator, CI, and retries are excluded."""
    return len(STRICT_AGENT_ROLES.intersection(roles))


def required_evidence_exists(
    file_names: list[str] | tuple[str, ...], issue: int = 137
) -> bool:
    """Return whether the lightweight preflight has every base evidence file."""
    return set(required_evidence_files(issue)).issubset(file_names)


def validate_role_packet(packet: dict[str, object]) -> list[str]:
    """Enforce the minimal packet schema without copied source or conversation payloads."""
    errors = [
        f"missing role packet field: {field}"
        for field in sorted(ROLE_PACKET_REQUIRED_FIELDS)
        if field != "required_documents" and not packet.get(field)
    ]
    errors.extend(
        f"non-allowlisted role packet field: {field}"
        for field in packet
        if field not in ROLE_PACKET_ALLOWED_FIELDS
    )
    documents = packet.get("required_documents")
    if not isinstance(documents, list) or not 1 <= len(documents) <= 5:
        errors.append("role packet requires 1~5 canonical document paths.")
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
    if packet.get("fork_turns") != "none":
        errors.append('role packet requires fork_turns="none".')
    if packet.get("subagent_stop") != ["superpowers:using-superpowers"]:
        errors.append("role packet requires the exact SUBAGENT-STOP skill list.")
    if packet.get("output_budget") != "summary-only":
        errors.append("role packet requires summary-only output budget.")
    if packet.get("previous_p0_p1_finding") and packet.get("last_failure_cause"):
        errors.append("role packet allows only one prior P0/P1 or last failure input.")
    return errors


def post_qa_requirements(
    *, repository_changed: bool, changes: list[ChangeRecord], issue_number: int = 0
) -> dict[str, bool]:
    """Use the shared classifier for post-QA Review and QA validity."""
    impact = classify_change_impact(changes, issue_number)
    stale = repository_changed and bool(changes) and impact.invalidates_review_qa
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


def coordinator_wait_action(
    *,
    notification_available: bool,
    wait_timed_out: bool,
    stall_suspected: bool,
    diagnostic_snapshots: int,
) -> str:
    """Prefer wait/notification and allow at most one diagnostic snapshot."""
    if notification_available:
        return "consume-notification"
    if (wait_timed_out or stall_suspected) and diagnostic_snapshots == 0:
        return "diagnostic-snapshot-once"
    return "wait-for-notification"


def expensive_command_action(
    *,
    active_handle: bool,
    completed_same_input: bool,
    input_changed: bool,
    previous_failed: bool,
    flaky_isolation: bool,
    classifier_stale: bool,
    independent_qa_required: bool,
) -> str:
    """Continue a live handle, reuse identical PASS, or run for an allowlisted reason."""
    if active_handle:
        return "continue-active-handle"
    rerun_allowed = any(
        (
            input_changed,
            previous_failed,
            flaky_isolation,
            classifier_stale,
            independent_qa_required,
        )
    )
    if completed_same_input and not rerun_allowed:
        return "reuse-completed-result"
    return "run"


def qa_remains_valid(
    qa_head: str,
    current_head: str,
    changes: list[ChangeRecord],
    issue_number: int,
) -> bool:
    """Keep QA when the shared classifier says the later delta is non-semantic evidence."""
    if qa_head == current_head:
        return True
    if not changes:
        return False
    impact = classify_change_impact(changes, issue_number)
    return not impact.invalidates_review_qa


def runtime_evidence_remains_valid(
    *,
    evidence_source_tree_sha: str,
    current_source_tree_sha: str,
    impact: ChangeImpact,
) -> bool:
    """Tie Level 3~7 evidence to source-tree SHA, not an evidence-only commit SHA."""
    return (
        evidence_source_tree_sha == current_source_tree_sha
        and not impact.invalidates_runtime_evidence
    )


def autonomous_merge_ready(
    *,
    writer_id: str,
    review_id: str,
    qa_id: str,
    review_verdict: str,
    qa_verdict: str,
    docs_evidence_ready: bool,
    ci_passed: bool,
    review_head: str,
    qa_head: str,
    source_tree_head: str,
    review_qa_stale: bool,
    ci_head: str,
    mergeable_clean: bool,
) -> bool:
    """Require distinct Writer, Review, and QA final verdicts at one source-tree SHA."""
    identities = (writer_id, review_id, qa_id)
    return all(
        (
            all(identities),
            len(set(identities)) == 3,
            review_verdict == "APPROVED",
            qa_verdict == "PASS",
            not review_qa_stale,
            docs_evidence_ready,
            ci_passed,
            review_head == source_tree_head,
            qa_head == source_tree_head,
            ci_head == source_tree_head,
            mergeable_clean,
        )
    )


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
    metrics_markdown: str | None = None,
    pr_body_markdown: str | None = None,
) -> list[str]:
    """Require every evidence source that exists to declare the same mode."""
    acceptance_mode = extract_execution_mode(acceptance_markdown)
    errors: list[str] = []
    if metrics_markdown is not None:
        metrics_mode = extract_execution_mode(metrics_markdown)
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


def current_attempt_failure_cause(markdown: str, attempt: str) -> str | None:
    """Return only the machine-selected current Attempt's Failure Cause body."""
    attempt_match = re.search(
        rf"^## Attempt {re.escape(attempt)}[ \t]*\r?\n(.*?)(?=^## Attempt [1-9]\d*[ \t]*$|\Z)",
        markdown,
        re.MULTILINE | re.DOTALL,
    )
    if attempt_match is None:
        return None
    failure_match = re.search(
        r"^### Failure Cause[ \t]*\r?\n(.*?)(?=^### |\Z)",
        attempt_match.group(1),
        re.MULTILINE | re.DOTALL,
    )
    return failure_match.group(1).strip() if failure_match else None


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
    acceptance: str,
    attempt_log: str,
    metrics: str | None,
    verification: str,
    issue: int,
    verification_rows: list[dict[str, str]] | None = None,
) -> list[str]:
    """Fail closed when current Issue evidence sources describe different completion states."""
    attempt_state, errors = attempt_reconciliation_state(attempt_log)
    verification_state, verification_errors = verification_reconciliation_state(verification)
    errors.extend(verification_errors)
    row = metrics_row(metrics) if metrics is not None else None
    if attempt_state is None or verification_state is None:
        return errors

    if attempt_state["attempt"] != verification_state["attempt"]:
        errors.append("evidence reconciliation: Current Attempt and verification.md Attempt must match.")
    if attempt_state["head"] != verification_state["head"]:
        errors.append("evidence reconciliation: Current head and verification.md Head must match.")
    if row is not None and int(row[3]) != int(attempt_state["attempt"]) - 1:
        errors.append("evidence reconciliation: metrics retry count must equal Current Attempt minus one.")

    if verification_rows is None:
        rows, row_errors = _verification_rows(verification)
        errors.extend(row_errors)
    else:
        rows = verification_rows
    issue_pattern = re.compile(rf"\bIssue\s*#\s*{issue}\b", re.IGNORECASE)
    has_pass = any(issue_pattern.search(row["Issue"]) and row["결과"] == "PASS" for row in rows)
    unchecked = re.findall(r"^- \[ \] .+", acceptance, re.MULTILINE)
    checked = re.findall(r"^- \[[xX]\] .+", acceptance, re.MULTILINE)
    failure_cause = current_attempt_failure_cause(attempt_log, attempt_state["attempt"])
    normalized_failure_cause = re.sub(r"^[\s*-]+|[\s.]+$", "", failure_cause or "").casefold()
    if attempt_state["disposition"] == "BLOCKED" and normalized_failure_cause in {
        "",
        "none",
        "n/a",
        "unknown",
        "없음",
        "미정",
    }:
        errors.append("attempt-log.md BLOCKED Current disposition requires an exact latest Failure Cause blocker.")
    if attempt_state["disposition"] == "BLOCKED" and has_pass:
        errors.append("evidence reconciliation: BLOCKED Current disposition cannot include PASS verification.")
    if attempt_state["disposition"] == "PASS" and not has_pass:
        errors.append("evidence reconciliation: PASS Current disposition requires PASS verification.")
    if attempt_state["disposition"] == "PASS" and (unchecked or not checked):
        errors.append("evidence reconciliation: PASS Current disposition requires every acceptance check to be checked.")
    if attempt_state["disposition"] == "BLOCKED" and checked and not unchecked:
        errors.append("evidence reconciliation: BLOCKED Current disposition requires an unchecked acceptance check.")
    return errors


def validate_lightweight_evidence_reconciliation(
    acceptance: str,
    verification: str,
    issue: int,
    verification_rows: list[dict[str, str]] | None = None,
) -> list[str]:
    """Gate post-bootstrap completion using only checked AC and final PASS verification."""
    rows, errors = (
        _verification_rows(verification)
        if verification_rows is None
        else (verification_rows, [])
    )
    issue_pattern = re.compile(rf"\bIssue\s*#\s*{issue}\b", re.IGNORECASE)
    has_pass = any(issue_pattern.search(row["Issue"]) and row["결과"] == "PASS" for row in rows)
    unchecked = re.findall(r"^- \[ \] .+", acceptance, re.MULTILINE)
    checked = re.findall(r"^- \[[xX]\] .+", acceptance, re.MULTILINE)
    if not has_pass:
        errors.append("evidence reconciliation: completion requires PASS verification.")
    if unchecked or not checked:
        errors.append("evidence reconciliation: completion requires every acceptance check to be checked.")
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
    markdown: str,
    issue: int,
    required_levels: tuple[int, ...] = (),
    required_result: str = "PASS",
    parsed_rows: list[dict[str, str]] | None = None,
) -> list[str]:
    """Validate every log row and required terminal evidence for one Issue."""
    rows, errors = _verification_rows(markdown) if parsed_rows is None else (parsed_rows, [])
    issue_pattern = re.compile(rf"\bIssue\s*#\s*{issue}\b", re.IGNORECASE)
    issue_rows = [row for row in rows if issue_pattern.search(row["Issue"])]

    if not issue_rows:
        if issue_pattern.search(markdown) is None:
            errors.append(f"verification-log.md에 Issue #{issue} 기록이 없습니다.")
        else:
            errors.append(f"verification-log.md에 Issue #{issue} 결과 행이 없습니다.")

    for level in required_levels:
        expected_level = f"Level {level}"
        has_required_result = any(
            row["Level"] == expected_level and row["결과"] == required_result
            for row in issue_rows
        )
        if not has_required_result:
            errors.append(
                f"verification-log.md: Issue #{issue} required {expected_level} {required_result} is missing; "
                f"add a row with the same Issue, Level '{expected_level}', and result '{required_result}'."
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


def load_issue_evidence(repository_root: Path, issue: int) -> dict[str, str]:
    """Read each known Issue evidence file at most once per gate invocation."""
    evidence_dir = repository_root / "docs" / "testing" / "evidence" / f"issue-{issue}"
    names = set(REQUIRED_EVIDENCE_FILES).union({"verification.md"})
    return {
        name: (evidence_dir / name).read_text(encoding="utf-8")
        for name in names
        if (evidence_dir / name).is_file()
    }


def validate_issue_evidence(
    repository_root: Path,
    issue: int,
    changed_paths_for_level: list[str] | None = None,
    evidence_files: dict[str, str] | None = None,
) -> list[str]:
    """Validate the minimum evidence needed to make an Issue completion claim."""
    evidence_dir = repository_root / "docs" / "testing" / "evidence" / f"issue-{issue}"
    errors: list[str] = []
    evidence = evidence_files if evidence_files is not None else load_issue_evidence(repository_root, issue)

    for name in required_evidence_files(issue):
        path = evidence_dir / name
        if name not in evidence:
            errors.append(f"ERROR: missing required evidence file: {path}")

    for name in ("commands.md", "manual-qa.md"):
        path = evidence_dir / name
        if name in evidence:
            meaningful_lines = [
                line for line in evidence[name].splitlines()
                if line.strip() and not line.lstrip().startswith("#")
            ]
            if not meaningful_lines:
                errors.append(f"ERROR: {path} has no evidence content.")

    acceptance = ""
    acceptance_path = evidence_dir / "acceptance-criteria.md"
    if "acceptance-criteria.md" in evidence:
        acceptance = evidence["acceptance-criteria.md"]
        errors.extend(validate_acceptance_criteria(acceptance, acceptance_path))
        errors.extend(validate_level_exemptions(acceptance))

    metrics = ""
    metrics_path = evidence_dir / "metrics.md"
    if "metrics.md" in evidence:
        metrics = evidence["metrics.md"]
        errors.extend(validate_metrics(metrics, metrics_path))
    if acceptance and metrics:
        errors.extend(validate_execution_mode_consistency(acceptance, metrics))

    attempt_log_path = evidence_dir / "attempt-log.md"
    if "attempt-log.md" in evidence:
        attempt_log = evidence["attempt-log.md"]
        errors.extend(validate_attempt_log(attempt_log, attempt_log_path))
    else:
        attempt_log = ""

    verification_log = repository_root / verification_file_path(issue)
    if "verification.md" not in evidence:
        errors.append(f"ERROR: missing Issue verification source: {verification_log}")
    else:
        verification = evidence["verification.md"]
        verification_rows, verification_parse_errors = _verification_rows(verification)
        errors.extend(verification_parse_errors)
        required_levels = set(required_verification_levels(acceptance))
        if changed_paths_for_level is not None:
            required_levels.update(required_path_levels_needing_pass(changed_paths_for_level, acceptance))
        required_result = (
            "PARTIAL"
            if _single_metadata_value(attempt_log, "Current disposition") == "BLOCKED"
            else "PASS"
        )
        errors.extend(
            validate_verification_log(
                verification,
                issue,
                tuple(sorted(required_levels)),
                required_result,
                verification_rows,
            )
        )
        if issue < LIGHTWEIGHT_EVIDENCE_START_ISSUE and acceptance and metrics and attempt_log:
            errors.extend(
                validate_evidence_reconciliation(
                    acceptance,
                    attempt_log,
                    metrics,
                    verification,
                    issue,
                    verification_rows,
                )
            )
        elif issue >= LIGHTWEIGHT_EVIDENCE_START_ISSUE and acceptance and attempt_log:
            errors.extend(
                validate_evidence_reconciliation(
                    acceptance,
                    attempt_log,
                    metrics or None,
                    verification,
                    issue,
                    verification_rows,
                )
            )
        elif issue >= LIGHTWEIGHT_EVIDENCE_START_ISSUE and acceptance:
            if metrics:
                row = metrics_row(metrics)
                if row is not None and int(row[3]) != 0:
                    errors.append(
                        "evidence reconciliation: metrics retry count requires attempt-log.md."
                    )
            errors.extend(
                validate_lightweight_evidence_reconciliation(
                    acceptance, verification, issue, verification_rows
                )
            )

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
    records = changed_path_records(repository_root, base_ref, include_worktree)
    relative_paths = {Path(record.path) for record in records if record.path.endswith(".md")}
    return sorted((repository_root / relative_path for relative_path in relative_paths if (repository_root / relative_path).is_file()), key=str)


def changed_markdown_files_from_records(
    repository_root: Path, records: list[ChangeRecord]
) -> list[Path]:
    """Derive link-check inputs from the already-read name-status records."""
    relative_paths = {Path(record.path) for record in records if record.path.endswith(".md")}
    return sorted(
        (
            repository_root / relative_path
            for relative_path in relative_paths
            if (repository_root / relative_path).is_file()
        ),
        key=str,
    )


def _parse_name_status(output: str) -> list[ChangeRecord]:
    records: list[ChangeRecord] = []
    for line in output.splitlines():
        fields = line.split("\t")
        if len(fields) == 2:
            records.append(ChangeRecord(fields[0], fields[1]))
        elif len(fields) == 3 and fields[0].startswith(("R", "C")):
            records.append(ChangeRecord(fields[0], fields[2], fields[1]))
        elif line:
            records.append(ChangeRecord("?", line))
    return records


def changed_path_records(
    repository_root: Path, base_ref: str, include_worktree: bool = False
) -> list[ChangeRecord]:
    """Read Git name-status once so rename/delete remain visible to every gate."""
    records = _parse_name_status(
        _git_output(repository_root, "diff", "--name-status", "--find-renames", f"{base_ref}...HEAD")
    )
    if include_worktree:
        records.extend(
            _parse_name_status(
                _git_output(repository_root, "diff", "--name-status", "--find-renames", "HEAD")
            )
        )
        records.extend(
            ChangeRecord("A", path)
            for path in _git_output(
                repository_root, "ls-files", "--others", "--exclude-standard"
            ).splitlines()
            if path
        )
    unique = {(record.status, record.path, record.previous_path): record for record in records}
    return sorted(unique.values(), key=lambda record: (record.path, record.status))


def changed_paths(repository_root: Path, base_ref: str) -> list[str]:
    """Return changed repository-relative paths between the base ref and HEAD."""
    return [record.path for record in changed_path_records(repository_root, base_ref)]


def validate_execution_head_delta(
    execution_head: str, is_ancestor: bool, changed_since_execution_head: list[str], issue: int
) -> list[str]:
    """Allow only current-Issue evidence commits after the verified execution head."""
    if not is_ancestor:
        return ["evidence reconciliation: execution head must be an ancestor of current Git HEAD."]
    evidence_prefix = f"docs/testing/evidence/issue-{issue}/"
    disallowed = sorted(
        path for path in set(changed_since_execution_head) if not path.startswith(evidence_prefix)
    )
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
    """Compatibility wrapper around the single fail-closed classifier."""
    impact = classify_change_impact([ChangeRecord("M", path) for path in paths], issue=0)
    return validate_declared_mode_floor(mode, impact)


def required_path_levels(paths: list[str]) -> dict[int, list[str]]:
    """Return {Level: [matched paths]} for Issue #57 ENFORCE path->Level mappings (M1/M2/M3)."""
    normalized_paths = [path.replace("\\", "/") for path in paths]
    matched: dict[int, list[str]] = {}
    for level, pattern in LEVEL_PATH_ENFORCE_RULES:
        for path in normalized_paths:
            if pattern.match(path):
                matched.setdefault(level, []).append(path)
    return matched


def parse_level_exemptions(acceptance_markdown: str) -> list[dict[str, str | int]]:
    """Parse fixed-format `Level exemption: <level> <code> — <path> — <ref>` lines only."""
    exemptions: list[dict[str, str | int]] = []
    for match in LEVEL_EXEMPTION_PATTERN.finditer(acceptance_markdown):
        exemptions.append(
            {
                "level": int(match.group("level")),
                "code": match.group("code"),
                "path": match.group("path").replace("\\", "/"),
                "ref": match.group("ref"),
            }
        )
    return exemptions


def validate_level_exemptions(acceptance_markdown: str) -> list[str]:
    """Reject any declared Level exemption whose code is not on the fixed list (no free-prose reasons)."""
    errors: list[str] = []
    for exemption in parse_level_exemptions(acceptance_markdown):
        if exemption["code"] not in LEVEL_EXEMPTION_CODES:
            errors.append(
                f"Level exemption code '{exemption['code']}' is not one of the fixed codes "
                f"defined in docs/testing/level-mapping-design.md: {', '.join(sorted(LEVEL_EXEMPTION_CODES))}."
            )
    return errors


def required_path_levels_needing_pass(paths: list[str], acceptance_markdown: str) -> tuple[int, ...]:
    """Return the subset of ENFORCE Levels (M1/M2/M3) that still need a verification.md PASS row.

    A Level is dropped from the result only when every path that matched an ENFORCE mapping for
    that Level has its own valid, fixed-code exemption recorded in acceptance-criteria.md.
    """
    matched = required_path_levels(paths)
    if not matched:
        return ()
    exemptions = parse_level_exemptions(acceptance_markdown)
    levels: list[int] = []
    for level, matched_paths in matched.items():
        fully_exempted = all(
            any(
                exemption["level"] == level
                and exemption["path"] == path
                and exemption["code"] in LEVEL_EXEMPTION_CODES
                for exemption in exemptions
            )
            for path in matched_paths
        )
        if not fully_exempted:
            levels.append(level)
    return tuple(sorted(set(levels)))


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
        "--impact-only",
        action="store_true",
        help="Print GitHub-output-compatible impact classification and skip evidence validation.",
    )
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
    records: list[ChangeRecord] | None = None

    if args.impact_only:
        if issue is None:
            print("Harness gate FAILED:\nERROR: issue number is required for impact classification.")
            return 1
        try:
            records = changed_path_records(
                repository_root, args.base_ref, args.include_worktree
            )
        except RuntimeError as error:
            print(f"Harness gate FAILED:\nERROR: cannot determine changed paths from {args.base_ref}: {error}")
            return 1
        impact = classify_change_impact(records, issue)
        print(f"execution_mode_floor={impact.execution_mode_floor}")
        print(f"requires_java_ci={str(impact.requires_java_ci).lower()}")
        print(f"invalidates_review_qa={str(impact.invalidates_review_qa).lower()}")
        print(
            "invalidates_runtime_evidence="
            f"{str(impact.invalidates_runtime_evidence).lower()}"
        )
        return 0

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
            issue_changed_paths: list[str] | None = None
            try:
                records = changed_path_records(
                    repository_root, args.base_ref, args.include_worktree
                )
                issue_changed_paths = [record.path for record in records]
            except RuntimeError as error:
                errors.append(f"ERROR: cannot determine changed paths from {args.base_ref}: {error}")
            evidence_files = load_issue_evidence(repository_root, issue)
            errors.extend(
                validate_issue_evidence(
                    repository_root, issue, issue_changed_paths, evidence_files
                )
            )
            evidence_dir = repository_root / "docs" / "testing" / "evidence" / f"issue-{issue}"
            acceptance_path = evidence_dir / "acceptance-criteria.md"
            metrics_path = evidence_dir / "metrics.md"
            attempt_path = evidence_dir / "attempt-log.md"
            if "attempt-log.md" in evidence_files:
                attempt_state, attempt_errors = attempt_reconciliation_state(
                    evidence_files["attempt-log.md"]
                )
                errors.extend(attempt_errors)
                if attempt_state is not None:
                    errors.extend(
                        validate_execution_head(repository_root, attempt_state["head"], issue)
                    )
            if pr_body is not None and "acceptance-criteria.md" in evidence_files:
                errors.extend(
                    validate_execution_mode_consistency(
                        evidence_files["acceptance-criteria.md"],
                        evidence_files.get("metrics.md"),
                        pr_body,
                    )
                )
            if "acceptance-criteria.md" in evidence_files and issue_changed_paths is not None:
                impact = classify_change_impact(records or [], issue)
                errors.extend(
                    validate_declared_mode_floor(
                        extract_execution_mode(evidence_files["acceptance-criteria.md"]),
                        impact,
                    )
                )

    if args.check_links or args.links_only:
        try:
            if records is None:
                records = changed_path_records(
                    repository_root, args.base_ref, args.include_worktree
                )
            markdown_files = changed_markdown_files_from_records(repository_root, records)
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
    harden_console_encoding()
    sys.exit(main())
