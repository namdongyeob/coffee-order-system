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
import os
import sys
import time
from dataclasses import asdict, dataclass, field
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
DEFAULT_STATE_DIR = ".team-orchestration-state"
DEFAULT_STATE_FILE = "state.json"


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
    stalls: int = 0
    out_of_scope_changes: int = 0
    review_qa_defects: int = 0
    human_interventions: int = 0

    @classmethod
    def new(cls, config: TeamOrchestrationConfig | None = None) -> "TeamState":
        return cls(config=(config or TeamOrchestrationConfig()).resolved(), created_at=time.time())

    def active_count(self) -> int:
        return len(self.assignments)

    def writer_count(self) -> int:
        return sum(1 for assignment in self.assignments.values() if assignment.is_writer)

    def register_agent(self, assignment: AgentAssignment) -> RegistrationResult:
        if assignment.agent_id in self.assignments:
            return RegistrationResult("BLOCKED", f"agent_id '{assignment.agent_id}' is already registered.")

        for existing in self.assignments.values():
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

        self.assignments[assignment.agent_id] = assignment
        return RegistrationResult("REGISTERED", f"agent '{assignment.agent_id}' registered.")

    def release_agent(self, agent_id: str) -> bool:
        return self.assignments.pop(agent_id, None) is not None

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
        return cls(
            config=TeamOrchestrationConfig(**data["config"]),
            created_at=data["created_at"],
            assignments=assignments,
            messages=list(data.get("messages", [])),
            retries=data.get("retries", 0),
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
    subparsers = parser.add_subparsers(dest="command", required=True)

    register_parser = subparsers.add_parser("register", help="Register a Dev agent assignment.")
    register_parser.add_argument("--agent-id", required=True)
    register_parser.add_argument("--role", default="Dev")
    register_parser.add_argument("--worktree", required=True)
    register_parser.add_argument("--owned-path", action="append", required=True, dest="owned_paths")
    register_parser.add_argument("--reader", action="store_true", help="Register as a non-writer (does not consume a writer slot).")

    release_parser = subparsers.add_parser("release", help="Release an agent's slot.")
    release_parser.add_argument("--agent-id", required=True)

    message_parser = subparsers.add_parser("message", help="Send a FINDING/NEED/BLOCKED/SCOPE_CONFLICT message.")
    message_parser.add_argument("--agent-id", required=True)
    message_parser.add_argument("--type", required=True, choices=sorted(MESSAGE_TYPES))
    message_parser.add_argument("--body", required=True)

    subparsers.add_parser("status", help="Print current assignments and metrics.")
    subparsers.add_parser("reset", help="Delete runtime team-state.")

    args = parser.parse_args(argv)
    repository_root = Path(__file__).resolve().parents[1]

    try:
        if args.command == "reset":
            path = state_file_path(repository_root)
            if path.is_file():
                path.unlink()
            print("team-orchestration state reset.")
            return 0

        state = load_state(repository_root)

        if args.command == "register":
            assignment = AgentAssignment(
                agent_id=args.agent_id,
                role=args.role,
                worktree_path=args.worktree,
                owned_paths=tuple(args.owned_paths),
                is_writer=not args.reader,
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

        if args.command == "status":
            print(json.dumps({"assignments": state.to_dict()["assignments"], "metrics": state.metrics_snapshot()}, ensure_ascii=False, indent=2))
            return 0
    except TeamOrchestrationError as error:
        print(f"ERROR: {error}")
        return 1

    return 1


if __name__ == "__main__":
    harden_console_encoding()
    raise SystemExit(main())
