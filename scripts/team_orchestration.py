"""Reusable parallel-Dev orchestration experiment tool, separate from the harness core.

Issue #91 scope (1차): assign Dev agents to worktrees with owned paths, cap active/writer
agent slots, validate a 4-type message protocol (FINDING/NEED/BLOCKED/SCOPE_CONFLICT), and
persist runtime team-state only to a gitignored cache. GitHub Issue/PR/evidence remain the
source of truth. This module never merges branches, integrates code, or auto-fixes anything —
that stays with the existing human/Coordinator merge governance in orchestration-policy.md.
"""

from __future__ import annotations

import argparse
import json
import math
import os
import sys
import time
from dataclasses import asdict, dataclass, field, replace
from pathlib import Path


def harden_console_encoding() -> None:
    """cp949 등 non-UTF-8 콘솔에서 인코딩 불가 문자로 인한 UnicodeEncodeError 크래시를 막는다."""
    for stream in (sys.stdout, sys.stderr):
        if hasattr(stream, "reconfigure"):
            stream.reconfigure(errors="backslashreplace")

DEFAULT_MAX_ACTIVE_AGENTS = 3
DEFAULT_MAX_WRITER_AGENTS = 2
# Issue #91 1차 scope explicitly excludes a 3-writer exception; this is a hard ceiling,
# not a default that a config file can raise.
MAX_WRITER_AGENTS_CEILING = 2
MESSAGE_TYPES = frozenset({"FINDING", "NEED", "BLOCKED", "SCOPE_CONFLICT"})
TERMINAL_AGENT_STATUSES = frozenset({"STALLED", "TIMEOUT", "BLOCKED", "COMPLETED"})
DEFAULT_STATE_DIR = ".team-orchestration-state"
DEFAULT_STATE_FILE = "state.json"


def _is_finite_time(value: object) -> bool:
    return isinstance(value, (int, float)) and math.isfinite(value)


def _worktree_path_action(worktree_path: str, *, java_or_gradle_required: bool) -> str:
    """Fail closed on a missing worktree or a non-ASCII *resolved* Java path."""
    resolved = Path(worktree_path).expanduser().resolve(strict=False)
    if not resolved.is_dir():
        return "BLOCKED: WORKTREE_NOT_FOUND"
    if not java_or_gradle_required:
        return "ALLOW"
    try:
        str(resolved).encode("ascii")
    except UnicodeEncodeError:
        return "BLOCKED: NON_ASCII_WORKTREE_PATH"
    return "ALLOW"


def _retry_action(*, retry_count: int, user_approved_new_run: bool = False) -> str:
    if retry_count < 0:
        raise ValueError("retry_count must be non-negative")
    if retry_count == 0:
        return "RETRY_ONCE"
    if user_approved_new_run:
        return "START_APPROVED_NEW_RUN"
    return "BLOCKED: RETRY_LIMIT"


class TeamOrchestrationError(Exception):
    """Raised for invalid configuration or protocol violations."""


@dataclass(frozen=True)
class TeamOrchestrationConfig:
    max_active_agents: int = DEFAULT_MAX_ACTIVE_AGENTS
    max_writer_agents: int = DEFAULT_MAX_WRITER_AGENTS

    def __post_init__(self) -> None:
        if self.max_writer_agents > MAX_WRITER_AGENTS_CEILING:
            raise TeamOrchestrationError(
                f"max_writer_agents={self.max_writer_agents} exceeds the Issue #91 1차 ceiling "
                f"of {MAX_WRITER_AGENTS_CEILING} (the 3-Dev exception is out of scope)."
            )
        if self.max_writer_agents > self.max_active_agents:
            raise TeamOrchestrationError("max_writer_agents cannot exceed max_active_agents.")
        if self.max_active_agents < 1 or self.max_writer_agents < 1:
            raise TeamOrchestrationError("agent limits must be at least 1.")

    def resolved(self) -> "TeamOrchestrationConfig":
        """Auto-shrink to the local runtime's CPU budget, per Issue #91 ('runtime 한도가 더 작으면 자동 축소')."""
        cpu_budget = max(1, (os.cpu_count() or 1) - 1)
        return TeamOrchestrationConfig(
            max_active_agents=min(self.max_active_agents, cpu_budget),
            max_writer_agents=min(self.max_writer_agents, cpu_budget, MAX_WRITER_AGENTS_CEILING),
        )


@dataclass(frozen=True)
class AgentAssignment:
    agent_id: str
    role: str
    worktree_path: str
    owned_paths: tuple[str, ...]
    is_writer: bool = True
    java_or_gradle_required: bool | None = None
    impact: str | None = None
    phase: str = "INTAKE"
    status: str = "RUNNING"
    started_at: float = 0.0
    last_heartbeat_at: float = 0.0
    deadline_at: float | None = None


@dataclass
class RegistrationResult:
    status: str  # "REGISTERED" | "BLOCKED" | "SCOPE_CONFLICT"
    detail: str
    conflicting_agent_id: str | None = None


def _normalize_owned_path(path: str) -> str:
    normalized = path.replace("\\", "/").strip()
    for suffix in ("/**", "/*", "**", "*"):
        if normalized.endswith(suffix):
            normalized = normalized[: -len(suffix)]
    return normalized.rstrip("/")


def owned_paths_overlap(paths_a: tuple[str, ...], paths_b: tuple[str, ...]) -> tuple[str, str] | None:
    """Return the first overlapping (path_a, path_b) pair, or None. Directory-containment heuristic,
    not full glob algebra: two owned paths overlap if one is a prefix of (or equal to) the other."""
    for raw_a in paths_a:
        norm_a = _normalize_owned_path(raw_a)
        for raw_b in paths_b:
            norm_b = _normalize_owned_path(raw_b)
            if not norm_a or not norm_b:
                continue
            if norm_a == norm_b or norm_a.startswith(norm_b + "/") or norm_b.startswith(norm_a + "/"):
                return (raw_a, raw_b)
    return None


@dataclass
class TeamState:
    """Gitignored runtime cache of active agent assignments and the 4-type message log.

    GitHub Issue/PR/evidence remain the source of truth (per Issue #91); this state exists
    only to coordinate a single local orchestration run and is safe to delete at any time.
    """

    config: TeamOrchestrationConfig
    created_at: float
    assignments: dict[str, AgentAssignment] = field(default_factory=dict)
    messages: list[dict[str, str]] = field(default_factory=list)
    retries: int = 0
    retry_ledger: dict[str, int] = field(default_factory=dict)
    legacy_retry_ledger_unresolved: bool = False
    diagnostic_snapshots: set[str] = field(default_factory=set)
    reset_history: list[dict[str, object]] = field(default_factory=list)
    stalls: int = 0
    out_of_scope_changes: int = 0
    review_qa_defects: int = 0
    human_interventions: int = 0

    @classmethod
    def new(cls, config: TeamOrchestrationConfig | None = None) -> "TeamState":
        return cls(config=(config or TeamOrchestrationConfig()).resolved(), created_at=time.time())

    def active_count(self) -> int:
        return sum(1 for assignment in self.assignments.values() if assignment.status == "RUNNING")

    def writer_count(self) -> int:
        return sum(
            1
            for assignment in self.assignments.values()
            if assignment.is_writer and assignment.status == "RUNNING"
        )

    def register_agent(self, assignment: AgentAssignment) -> RegistrationResult:
        if assignment.agent_id in self.assignments:
            return RegistrationResult("BLOCKED", f"agent_id '{assignment.agent_id}' is already registered.")

        if not assignment.impact or not assignment.impact.strip():
            return RegistrationResult("BLOCKED", "BLOCKED: IMPACT_UNDECLARED")
        if assignment.deadline_at is None:
            return RegistrationResult("BLOCKED", "BLOCKED: DEADLINE_UNDECLARED")
        if not all(
            _is_finite_time(value)
            for value in (assignment.started_at, assignment.last_heartbeat_at, assignment.deadline_at)
        ):
            return RegistrationResult("BLOCKED", "BLOCKED: INVALID_TIME")
        if assignment.java_or_gradle_required is None:
            return RegistrationResult("BLOCKED", "BLOCKED: JAVA_REQUIREMENT_UNDECLARED")

        worktree_action = _worktree_path_action(
            assignment.worktree_path,
            java_or_gradle_required=assignment.java_or_gradle_required,
        )
        if worktree_action != "ALLOW":
            return RegistrationResult("BLOCKED", worktree_action)

        for existing in self.assignments.values():
            if existing.status != "RUNNING":
                continue
            overlap = owned_paths_overlap(assignment.owned_paths, existing.owned_paths)
            if overlap is not None:
                return RegistrationResult(
                    "SCOPE_CONFLICT",
                    f"owned path '{overlap[0]}' overlaps with agent '{existing.agent_id}' owned path '{overlap[1]}'.",
                    conflicting_agent_id=existing.agent_id,
                )

        if self.active_count() >= self.config.max_active_agents:
            return RegistrationResult(
                "BLOCKED",
                f"max_active_agents={self.config.max_active_agents} slot limit reached.",
            )
        if assignment.is_writer and self.writer_count() >= self.config.max_writer_agents:
            return RegistrationResult(
                "BLOCKED",
                f"max_writer_agents={self.config.max_writer_agents} slot limit reached "
                "(Issue #91 1차 excludes a 3-writer exception).",
            )

        now = time.time()
        if assignment.deadline_at <= (assignment.started_at or now):
            return RegistrationResult("BLOCKED", "BLOCKED: INVALID_DEADLINE")
        self.assignments[assignment.agent_id] = replace(
            assignment,
            phase=assignment.phase or "INTAKE",
            status="RUNNING",
            started_at=assignment.started_at or now,
            last_heartbeat_at=assignment.last_heartbeat_at or now,
        )
        return RegistrationResult("REGISTERED", f"agent '{assignment.agent_id}' registered.")

    def heartbeat(self, agent_id: str, *, now: float | None = None, phase: str | None = None) -> bool:
        assignment = self.assignments.get(agent_id)
        if assignment is None or assignment.status != "RUNNING":
            return False
        heartbeat_at = time.time() if now is None else now
        if not _is_finite_time(heartbeat_at):
            raise TeamOrchestrationError("BLOCKED: INVALID_TIME")
        self.assignments[agent_id] = replace(
            assignment,
            phase=phase or assignment.phase,
            status="RUNNING",
            last_heartbeat_at=heartbeat_at,
        )
        return True

    def retry_decision(self, *, issue: int, failure_key: str) -> str:
        """Consume one retry per stable Issue/failure key in the persisted state ledger."""
        if issue < 1 or not failure_key.strip():
            raise TeamOrchestrationError("issue and non-empty failure_key are required for retry.")
        if self.legacy_retry_ledger_unresolved:
            return "BLOCKED: LEGACY_RETRY_LEDGER_REQUIRED"
        ledger_key = f"{issue}:{failure_key.strip()}"
        action = _retry_action(retry_count=self.retry_ledger.get(ledger_key, 0))
        if action == "RETRY_ONCE":
            self.retries += 1
            self.retry_ledger[ledger_key] = 1
        return action

    def lifecycle_status(self, agent_id: str, *, now: float, heartbeat_timeout: float) -> str:
        assignment = self.assignments[agent_id]
        if not all(
            _is_finite_time(value)
            for value in (now, heartbeat_timeout, assignment.last_heartbeat_at)
        ) or heartbeat_timeout < 0 or (
            assignment.deadline_at is not None and not _is_finite_time(assignment.deadline_at)
        ):
            raise TeamOrchestrationError("BLOCKED: INVALID_TIME")
        if assignment.status != "RUNNING":
            return assignment.status
        if assignment.deadline_at is not None and now >= assignment.deadline_at:
            self.assignments[agent_id] = replace(assignment, status="TIMEOUT")
            return "TIMEOUT"
        if now - assignment.last_heartbeat_at > heartbeat_timeout:
            self.assignments[agent_id] = replace(assignment, status="STALLED")
            self.stalls += 1
            return "STALLED"
        return "RUNNING"

    def lifecycle_action(self, agent_id: str, *, now: float, heartbeat_timeout: float) -> str:
        """Return WAIT while live, or the persisted terminal/stalled action otherwise."""
        status = self.lifecycle_status(agent_id, now=now, heartbeat_timeout=heartbeat_timeout)
        return "WAIT" if status == "RUNNING" else status

    def mark_terminal(self, agent_id: str, *, status: str) -> bool:
        if status not in TERMINAL_AGENT_STATUSES:
            raise ValueError(f"status must be terminal: {sorted(TERMINAL_AGENT_STATUSES)}")
        assignment = self.assignments[agent_id]
        if assignment.status == "RUNNING":
            self.assignments[agent_id] = replace(assignment, status=status)
            return True
        return False

    def complete_agent(self, agent_id: str) -> bool:
        assignment = self.assignments.get(agent_id)
        if assignment is None or assignment.status != "RUNNING":
            return False
        self.assignments[agent_id] = replace(assignment, status="COMPLETED")
        return True

    def consume_diagnostic_snapshot(self, agent_id: str) -> str:
        if agent_id not in self.assignments:
            return "BLOCKED: AGENT_NOT_FOUND"
        if agent_id in self.diagnostic_snapshots:
            return "BLOCKED: SNAPSHOT_LIMIT"
        self.diagnostic_snapshots.add(agent_id)
        return "SNAPSHOT_RECORDED"

    def release_agent(self, agent_id: str) -> bool:
        released = self.assignments.pop(agent_id, None) is not None
        if released:
            self.diagnostic_snapshots.discard(agent_id)
        return released

    def reset_for_new_run(self, approval_ref: str, *, now: float | None = None) -> "TeamState":
        if not approval_ref.strip():
            raise TeamOrchestrationError("BLOCKED: APPROVAL_REFERENCE_REQUIRED")
        reset_at = time.time() if now is None else now
        if not _is_finite_time(reset_at):
            raise TeamOrchestrationError("BLOCKED: INVALID_TIME")
        return TeamState(
            config=self.config,
            created_at=reset_at,
            reset_history=[*self.reset_history, {"approval_ref": approval_ref.strip(), "reset_at": reset_at}],
        )

    def send_message(self, agent_id: str, message_type: str, body: str) -> None:
        if message_type not in MESSAGE_TYPES:
            raise TeamOrchestrationError(
                f"message type '{message_type}' is not one of the allowed 4 types: "
                f"{', '.join(sorted(MESSAGE_TYPES))}."
            )
        if agent_id not in self.assignments:
            raise TeamOrchestrationError(f"agent_id '{agent_id}' is not a registered agent.")
        self.messages.append(
            {"agent_id": agent_id, "type": message_type, "body": body, "at": str(time.time())}
        )
        if message_type == "SCOPE_CONFLICT":
            self.out_of_scope_changes += 1

    def metrics_snapshot(self) -> dict[str, object]:
        """Issue #91's required measurement fields. merge_conflicts is always 0 by design: this
        tool has no merge/integration code path, so it cannot produce a merge conflict."""
        return {
            "wall_clock_seconds": round(time.time() - self.created_at, 3),
            "agent_count": self.active_count(),
            "token_measurable": False,
            "retries": self.retries,
            "retry_ledger": self.retry_ledger,
            "diagnostic_snapshots": sorted(self.diagnostic_snapshots),
            "stalls": self.stalls,
            "out_of_scope_changes": self.out_of_scope_changes,
            "review_qa_defects": self.review_qa_defects,
            "merge_conflicts": 0,
            "human_interventions": self.human_interventions,
        }

    def to_dict(self) -> dict[str, object]:
        return {
            "config": asdict(self.config),
            "created_at": self.created_at,
            "assignments": {
                agent_id: asdict(assignment) for agent_id, assignment in self.assignments.items()
            },
            "messages": self.messages,
            "retries": self.retries,
            "retry_ledger": self.retry_ledger,
            "legacy_retry_ledger_unresolved": self.legacy_retry_ledger_unresolved,
            "diagnostic_snapshots": sorted(self.diagnostic_snapshots),
            "reset_history": self.reset_history,
            "stalls": self.stalls,
            "out_of_scope_changes": self.out_of_scope_changes,
            "review_qa_defects": self.review_qa_defects,
            "human_interventions": self.human_interventions,
        }

    @classmethod
    def from_dict(cls, data: dict[str, object]) -> "TeamState":
        assignments = {
            agent_id: AgentAssignment(**{**value, "owned_paths": tuple(value["owned_paths"])})
            for agent_id, value in data.get("assignments", {}).items()
        }
        retry_ledger_present = "retry_ledger" in data
        retries = data.get("retries", 0)
        return cls(
            config=TeamOrchestrationConfig(**data["config"]),
            created_at=data["created_at"],
            assignments=assignments,
            messages=list(data.get("messages", [])),
            retries=retries,
            retry_ledger=dict(data.get("retry_ledger", {})),
            legacy_retry_ledger_unresolved=bool(
                data.get("legacy_retry_ledger_unresolved", not retry_ledger_present and retries > 0)
            ),
            diagnostic_snapshots=set(data.get("diagnostic_snapshots", [])),
            reset_history=list(data.get("reset_history", [])),
            stalls=data.get("stalls", 0),
            out_of_scope_changes=data.get("out_of_scope_changes", 0),
            review_qa_defects=data.get("review_qa_defects", 0),
            human_interventions=data.get("human_interventions", 0),
        )


def state_file_path(repository_root: Path) -> Path:
    return repository_root / DEFAULT_STATE_DIR / DEFAULT_STATE_FILE


def load_state(repository_root: Path) -> TeamState:
    path = state_file_path(repository_root)
    if not path.is_file():
        return TeamState.new()
    return TeamState.from_dict(json.loads(path.read_text(encoding="utf-8")))


def save_state(repository_root: Path, state: TeamState) -> None:
    path = state_file_path(repository_root)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(state.to_dict(), ensure_ascii=False, indent=2), encoding="utf-8")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Issue #91 parallel Dev orchestration experiment tool.")
    parser.add_argument(
        "--repository-root",
        type=Path,
        help="Repository root whose gitignored state file is consumed (defaults to this repository).",
    )
    subparsers = parser.add_subparsers(dest="command", required=True)

    register_parser = subparsers.add_parser("register", help="Register a Dev agent assignment.")
    register_parser.add_argument("--agent-id", required=True)
    register_parser.add_argument("--role", default="Dev")
    register_parser.add_argument("--worktree", required=True)
    register_parser.add_argument("--owned-path", action="append", required=True, dest="owned_paths")
    register_parser.add_argument("--impact", required=True, help="Declared task impact before spawning.")
    register_parser.add_argument(
        "--deadline-seconds", required=True, type=float, help="Positive deadline duration for this assignment."
    )
    register_parser.add_argument("--reader", action="store_true", help="Register as a non-writer (does not consume a writer slot).")
    java_group = register_parser.add_mutually_exclusive_group(required=True)
    java_group.add_argument("--java-required", action="store_true", help="Require Java/Gradle; enforce ASCII resolved worktree path.")
    java_group.add_argument("--no-java-required", action="store_true", help="Declare this assignment does not require Java/Gradle.")

    release_parser = subparsers.add_parser("release", help="Release an agent's slot.")
    release_parser.add_argument("--agent-id", required=True)

    message_parser = subparsers.add_parser("message", help="Send a FINDING/NEED/BLOCKED/SCOPE_CONFLICT message.")
    message_parser.add_argument("--agent-id", required=True)
    message_parser.add_argument("--type", required=True, choices=sorted(MESSAGE_TYPES))
    message_parser.add_argument("--body", required=True)

    heartbeat_parser = subparsers.add_parser("heartbeat", help="Persist a live Agent heartbeat or phase transition.")
    heartbeat_parser.add_argument("--agent-id", required=True)
    heartbeat_parser.add_argument("--phase", help="Optional current phase.")

    lifecycle_parser = subparsers.add_parser("lifecycle", aliases=["observe"], help="Persist and print WAIT, STALLED, or TIMEOUT once.")
    lifecycle_parser.add_argument("--agent-id", required=True)
    lifecycle_parser.add_argument("--heartbeat-timeout-seconds", required=True, type=float)
    lifecycle_parser.add_argument("--now", type=float, help="Optional deterministic Unix timestamp for fixture use.")

    retry_parser = subparsers.add_parser("retry", help="Consume the one scoped retry for an Issue failure.")
    retry_parser.add_argument("--issue", required=True, type=int)
    retry_parser.add_argument("--failure-key", required=True)

    snapshot_parser = subparsers.add_parser("snapshot", help="Consume the one diagnostic snapshot allowance for an Agent.")
    snapshot_parser.add_argument("--agent-id", required=True)

    complete_parser = subparsers.add_parser("complete", help="Persist completion and release active/writer slot accounting.")
    complete_parser.add_argument("--agent-id", required=True)

    block_parser = subparsers.add_parser("block", help="Persist an explicit BLOCKED terminal assignment.")
    block_parser.add_argument("--agent-id", required=True)

    subparsers.add_parser("status", help="Print current assignments and metrics.")
    reset_parser = subparsers.add_parser("reset", aliases=["new-run"], help="Start audited empty runtime state after explicit approval.")
    reset_parser.add_argument("--approval-ref", required=True, help="Non-empty approval reference authorizing the reset/new run.")

    args = parser.parse_args(argv)
    repository_root = (args.repository_root or Path(__file__).resolve().parents[1]).resolve()

    try:
        if args.command in {"reset", "new-run"}:
            if not args.approval_ref.strip():
                raise TeamOrchestrationError("BLOCKED: APPROVAL_REFERENCE_REQUIRED")
            state = load_state(repository_root).reset_for_new_run(args.approval_ref)
            save_state(repository_root, state)
            print("team-orchestration state reset.")
            return 0

        state = load_state(repository_root)

        if args.command == "register":
            if not _is_finite_time(args.deadline_seconds) or args.deadline_seconds <= 0:
                raise TeamOrchestrationError("BLOCKED: INVALID_DEADLINE")
            assignment = AgentAssignment(
                agent_id=args.agent_id,
                role=args.role,
                worktree_path=args.worktree,
                owned_paths=tuple(args.owned_paths),
                is_writer=not args.reader,
                java_or_gradle_required=args.java_required,
                impact=args.impact,
                deadline_at=time.time() + args.deadline_seconds,
            )
            result = state.register_agent(assignment)
            save_state(repository_root, state)
            print(f"{result.status}: {result.detail}")
            return 0 if result.status == "REGISTERED" else 1

        if args.command == "release":
            released = state.release_agent(args.agent_id)
            save_state(repository_root, state)
            print("RELEASED" if released else "NOT_FOUND")
            return 0 if released else 1

        if args.command == "message":
            state.send_message(args.agent_id, args.type, args.body)
            save_state(repository_root, state)
            print("MESSAGE_RECORDED")
            return 0

        if args.command == "heartbeat":
            updated = state.heartbeat(args.agent_id, phase=args.phase)
            save_state(repository_root, state)
            print("HEARTBEAT_RECORDED" if updated else "BLOCKED: AGENT_NOT_RUNNING")
            return 0 if updated else 1

        if args.command in {"lifecycle", "observe"}:
            action = state.lifecycle_action(
                args.agent_id,
                now=time.time() if args.now is None else args.now,
                heartbeat_timeout=args.heartbeat_timeout_seconds,
            )
            save_state(repository_root, state)
            print(action)
            return 0

        if args.command == "retry":
            action = state.retry_decision(issue=args.issue, failure_key=args.failure_key)
            save_state(repository_root, state)
            print(action)
            return 0 if action == "RETRY_ONCE" else 1

        if args.command == "snapshot":
            action = state.consume_diagnostic_snapshot(args.agent_id)
            save_state(repository_root, state)
            print(action)
            return 0 if action == "SNAPSHOT_RECORDED" else 1

        if args.command == "complete":
            completed = state.complete_agent(args.agent_id)
            save_state(repository_root, state)
            print("COMPLETED" if completed else "BLOCKED: AGENT_NOT_RUNNING")
            return 0 if completed else 1

        if args.command == "block":
            blocked = state.mark_terminal(args.agent_id, status="BLOCKED")
            save_state(repository_root, state)
            print("BLOCKED" if blocked else "BLOCKED: AGENT_NOT_RUNNING")
            return 0 if blocked else 1

        if args.command == "status":
            print(json.dumps({"assignments": state.to_dict()["assignments"], "metrics": state.metrics_snapshot()}, ensure_ascii=False, indent=2))
            return 0
    except (TeamOrchestrationError, KeyError, ValueError) as error:
        print(f"ERROR: {error}")
        return 1

    return 1


if __name__ == "__main__":
    harden_console_encoding()
    raise SystemExit(main())
