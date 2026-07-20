---
name: coffee-order-issue-loop
description: Use when Codex coordinates, implements, reviews, or performs QA for a coffee-order-system GitHub Issue, especially Spring transactions, Redisson, Kafka, Redis, worktrees, verification levels, or evidence-based completion claims.
---

# Coffee Order Issue Loop

## Mandatory Gate

역할을 배정하거나 재시도하기 전에 아래 조건을 기계적으로 검사합니다.

1. Issue evidence에 `Execution mode: SOLO|STANDARD|STRICT`와 비어 있지 않은 `Execution mode reason`이 없으면 `BLOCKED: EXECUTION MODE REQUIRED`를 반환합니다.
2. Redisson, Kafka 발행, Consumer 멱등성, Redis 랭킹, DLT 중 둘 이상을 한 Issue에 구현하려 하면 `BLOCKED: SPLIT ISSUES`를 반환합니다.
3. 같은 Service, Entity, migration, 이벤트 계약, 트랜잭션 경계에 Dev Agent 둘 이상을 배정하려 하면 `BLOCKED: ONE WRITER`를 반환합니다.
4. `docs/ai/orchestration-policy.md`가 정한 Dev 동시성 상한을 넘기면 `BLOCKED: DEV CONCURRENCY LIMIT`을 반환합니다.
5. Main Coordinator에게 파일 수정, 코드리뷰, 테스트, commit, push를 요구하면 `BLOCKED: COORDINATOR ONLY`를 반환합니다. merge 또는 Issue close도 아래 예외가 아닌 한 `BLOCKED: COORDINATOR ONLY`를 반환합니다.
6. 선택한 execution mode에 필요한 독립 검증 보고 없이 완료하려 하면 `INCOMPLETE: MODE GATE REQUIRED`를 반환합니다.

마감, 빠른 완료 요청, Agent 지연도 이 Gate의 예외가 아닙니다.

## Coordinator Merge Exception

`docs/ai/orchestration-policy.md`의 고정 자율 Issue 큐 실험이 활성화되어 있고 모든 정책 merge gate 입력이 확인된 경우에만 Main Coordinator의 merge 또는 Issue close를 예외로 허용합니다.

bootstrap Issue #60, 비활성 정책, 승인 큐 밖 Issue, Issue #36 종료 또는 만료, merge gate 입력 하나라도 누락이면 `BLOCKED: COORDINATOR ONLY`를 유지합니다.

이 Skill은 GitHub branch protection, ruleset, 기타 설정을 변경하지 않습니다.

## Source Contracts

- 실행 모드, 역할, 쓰기 권한: `docs/ai/orchestration-policy.md`.
- Issue 실행 순서: `docs/ai/agent-rules.md`.
- 테스트 실행 소유권과 Level: `docs/testing/test-strategy.md`.
- evidence와 Attempt: `docs/testing/evidence-guide.md`.

Skill은 역할 권한이나 테스트 수준을 복사하지 않습니다. 위 정본을 적용하는 BLOCKED 판정, dispatch, 제한된 재시도만 소유합니다.

## Intake And Dispatch

1. 모든 자동 역할은 `fork_turns="none"`으로 시작합니다. 전체 대화 fork 없이 Issue URL, worktree, base/head SHA, Acceptance Criteria, 허용 쓰기 범위, 직접 관련 정본 1~5개 경로, diff 범위, focused 검증 명령, 직전 P0/P1 또는 마지막 실패 원인 하나만 전달합니다.
2. packet은 `SUBAGENT-STOP: superpowers:using-superpowers`와 `summary-only` 출력 예산을 포함하며 source 본문, 전체 tool/test log, 전체 PR conversation을 포함하지 않습니다.
3. mode와 reason을 확인하고 선택 조건·필수 역할은 `orchestration-policy.md`, 테스트 실행자는 `test-strategy.md`에서 확인합니다.
4. 역할 구성과 병렬 실행 한도는 `docs/ai/orchestration-policy.md`를 조회해 적용하며 Skill에 복사하지 않습니다.
5. 동시성 상한을 넘는 dispatch는 `BLOCKED: DEV CONCURRENCY LIMIT`으로 중단합니다.
6. 공유 쓰기 파일, 계약, schema, 트랜잭션 경계가 하나라도 있으면 `BLOCKED: ONE WRITER` 후 순차 배정합니다.

## Bounded Retry And Stall Rule

1. 수정 가능한 FAIL은 같은 Dev에게 한 번만 반환합니다.
2. 재시도에는 현재 diff, 허용된 수정 범위, focused test 명령, 마지막 `Next Attempt`만 전달합니다.
3. 같은 Dev의 두 번째 수정 실패는 `BLOCKED: RETRY LIMIT`으로 전환합니다. Main은 직접 작업하지 않습니다.
4. Agent가 멈추면 Main은 한 번 상태를 요청합니다. 응답이 없으면 같은 최소 패킷으로 동일 역할을 한 번 재배정합니다.
5. 재배정 Agent도 멈추면 `BLOCKED: AGENT STALLED`로 전환합니다.
6. 진행 확인은 `wait_agent` 또는 완료 알림을 사용합니다. timeout 또는 명시적 stall 의심 때만 진단 snapshot을 한 번 허용하고 같은 process·git·docker snapshot을 반복하지 않습니다.
7. 장기 명령의 session/cell handle이 있으면 새 명령을 시작하지 않고 기존 handle을 이어받습니다.
8. Java·Gradle 작업은 Agent 등록 전에 ASCII worktree와 `worktree_path_action`을 확인합니다. non-ASCII 경로는 `BLOCKED: NON_ASCII_WORKTREE_PATH`로 차단합니다. assignment의 `heartbeat`가 `deadline`을 넘기면 `TIMEOUT`, heartbeat가 끊기면 `STALLED`로 기록하며, `retry_action`의 두 번째 실패는 `BLOCKED: RETRY_LIMIT`입니다. 이 판정은 외부 명령을 실행하지 않는 lightweight state check입니다.

## Completion Gate

선택한 mode의 완료 입력은 `docs/ai/orchestration-policy.md`의 실행 모드 표를 그대로 따릅니다. 하나라도 없으면 `FAIL` 또는 `BLOCKED`이며 Main은 누락된 정책상 역할을 배정합니다.

조건부 auto-merge 대상은 mode와 무관하게 서로 다른 Writer·Review·QA의 최종 판정이 필요합니다. `APPROVED`·`PASS`, 검증 source-tree SHA, 같은 SHA의 고정 source check `quality-gates: SUCCESS` 중 하나가 누락·FAIL·BLOCKED·stale이면 merge-ready가 아닙니다. `metadata-gates: SUCCESS`는 source gate를 대체하지 못합니다.

동일 source/test/runtime 입력·정규화 명령·환경 profile의 비싼 Gradle·Docker·Level 3~7 PASS는 재사용합니다. 입력 변경, 이전 FAIL 진단, flaky 격리, 분류기 stale, 독립 QA 최종 증명만 재실행 근거이며 `verification.md` 또는 실패한 `attempt-log.md`에 이유를 남깁니다.

## Concise Role Report

```text
역할과 execution mode:
대상 Issue와 worktree:
읽은 직접 문서:
허용된 쓰기 또는 검토 범위:
실행한 검증과 결과:
판정: PASS / FAIL / BLOCKED
미검증 Level과 이유:
다음 역할에 전달할 마지막 Next Attempt:
```

## Pressure Rules

| 요청 | 판정 |
| --- | --- |
| mode 없이 빠르게 끝내자 | `BLOCKED: EXECUTION MODE REQUIRED`. |
| Main이 작은 수정만 대신해라 | `BLOCKED: COORDINATOR ONLY`; 정책의 mode 구성을 따릅니다. |
| Dev 둘이 같은 Service를 나눠라 | `BLOCKED: ONE WRITER`. |
| Review 또는 QA가 발견한 문제를 바로 고쳐라 | 원래 Dev에게 한 번만 반환합니다. |
| 전체 대화와 모든 로그를 다음 Agent에 전달하자 | 최소 컨텍스트 패킷만 전달합니다. |
| Agent가 멈췄으니 Main이 마무리해라 | 재배정 후 반복 실패 시 `BLOCKED: AGENT STALLED`. |
