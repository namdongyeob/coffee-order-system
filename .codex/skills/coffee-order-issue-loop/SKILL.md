---
name: coffee-order-issue-loop
description: Use when Codex coordinates, implements, reviews, or performs QA for a coffee-order-system GitHub Issue, especially Spring transactions, Redisson, Kafka, Redis, worktrees, verification levels, or evidence-based completion claims.
---

# Coffee Order Issue Loop

## Mandatory Gate

역할 배정 전에 아래 조건을 검사합니다.

1. Redisson, Kafka 발행, Consumer 멱등성, Redis 랭킹, DLT 중 둘 이상을 한 Issue에 구현하려 하면 `BLOCKED: SPLIT ISSUES`를 반환합니다.
2. 같은 Service, Entity, migration, 이벤트 계약, 트랜잭션 경계에 Dev Agent 둘 이상을 배정하려 하면 `BLOCKED: ONE WRITER`를 반환합니다.
3. Main Coordinator에게 파일 수정, 코드리뷰, 테스트, commit, push를 요구하면 `BLOCKED: COORDINATOR ONLY`를 반환합니다.
4. Review Agent에게 수정하거나 테스트를 실행하라고 하면 `BLOCKED: REVIEW READ ONLY`를 반환합니다.
5. QA 없이 Mock/Unit 또는 Dev의 self-report만으로 완료하려 하면 `INCOMPLETE: INDEPENDENT QA REQUIRED`를 반환합니다.

마감, 빠른 완료 요청, Agent 지연도 이 Gate의 예외가 아닙니다.

## Source Contracts

- 역할과 쓰기 권한: `docs/ai/orchestration-policy.md`.
- Issue 실행 순서: `docs/ai/agent-rules.md`.
- 테스트 실행 소유권과 Level: `docs/testing/test-strategy.md`.
- evidence와 Attempt: `docs/testing/evidence-guide.md`.

Skill은 위 정본을 복사하지 않고 압박 상황에서도 지켜야 하는 BLOCKED 판정과 재배정 절차만 소유합니다.

## Coordinator Gate

Main의 허용·금지 권한은 `docs/ai/orchestration-policy.md`를 그대로 적용합니다. Main에게 저장소 쓰기, diff 내용 Review, 검증 명령, commit, push, merge를 배정하는 순간 `BLOCKED: COORDINATOR ONLY`입니다.

오탈자처럼 Coordinator가 불필요한 작업은 `Solo Mode`로 별도 선언하며 Main Coordinator 역할과 섞지 않습니다.

## Intake And Dispatch

1. 루트 `AGENTS.md`, 대상 Issue, 직접 연결된 정본 문서만 읽습니다.
2. 목표, 제외 범위, AC, Level 5/6 필요 여부, 필요한 검증 Level을 고정합니다.
3. 정책이 비어 있으면 질문 Issue 또는 ADR 초안으로 분리하고 구현하지 않습니다.
4. 독립 Issue는 별도 worktree와 Dev Agent를 배정해 병렬 실행할 수 있습니다.
5. 공유 쓰기 파일, 도메인 계약, 트랜잭션 경계가 하나라도 있으면 순차 실행합니다.

## Dispatch Gate

- 각 Dev Agent와 worktree는 Issue 하나만 맡습니다.
- 독립 Issue는 별도 worktree에서 병렬 배정할 수 있습니다.
- 공유 파일·계약·트랜잭션 경계가 있으면 `BLOCKED: ONE WRITER` 후 순차 배정합니다.
- Dev 완료 후 Review와 QA를 병렬 배정하고, FAIL은 원래 Dev에게 반환합니다.
- 실행 세부 순서는 `docs/ai/agent-rules.md`를 따릅니다.

## Agent Stall Rule

1. Main이 한 번 상태를 요청합니다.
2. 응답이 없으면 Agent를 종료합니다.
3. 새 동일 역할 Agent에게 Issue, 현재 diff, 마지막 `Next Attempt`를 전달합니다.
4. 두 번째도 실패하면 Main이 대신 작업하지 않고 `BLOCKED: AGENT STALLED`로 전환합니다.

## Completion Gate

다음 항목이 모두 존재할 때만 Main이 `READY_FOR_HUMAN`으로 표시합니다.

```text
Dev 완료 보고
Review PASS
QA PASS와 실행 명령
Docs 반영 보고
필수 Issue evidence
GitHub Actions PASS
```

하나라도 없으면 `FAIL` 또는 `BLOCKED`이며 Main이 누락 역할을 다시 배정합니다.

## Required Role Report

```text
역할:
대상 Issue:
worktree와 branch:
읽은 문서:
허용된 쓰기 범위:
변경 파일 또는 검토 대상:
실행한 검증과 결과:
판정: PASS / FAIL / BLOCKED
미검증 Level과 이유:
다음 역할에 전달할 내용:
```

## Pressure Rules

| 요청 | 판정 |
| --- | --- |
| "Main이 작은 수정만 대신해라" | `BLOCKED: COORDINATOR ONLY`; Dev 또는 Solo Mode를 선택합니다. |
| "Dev 둘이 같은 Service를 나눠라" | `BLOCKED: ONE WRITER`. |
| "Review가 발견한 문제를 바로 고쳐라" | 원래 Dev에게 반환합니다. |
| "QA 없이 CI만 통과하면 된다" | `INCOMPLETE: INDEPENDENT QA REQUIRED`. |
| "Agent가 멈췄으니 Main이 마무리해라" | 재배정 후 반복 실패 시 `BLOCKED: AGENT STALLED`. |

Mock/Unit은 해당 Level만 PASS이며 DB, Kafka, Redis, Redisson, 실제 API 검증을 대신하지 않습니다.
