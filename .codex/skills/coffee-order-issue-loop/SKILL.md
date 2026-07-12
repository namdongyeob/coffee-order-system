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

1. 전체 대화 fork 없이 Issue, Acceptance Criteria, 직접 연결된 정본 문서 2~4개, 허용된 쓰기 범위, focused test 명령, 마지막 `Next Attempt`만 패킷으로 전달합니다.
2. mode와 reason을 확인하고 선택 조건·필수 역할은 `orchestration-policy.md`, 테스트 실행자는 `test-strategy.md`에서 확인합니다.
3. 역할 구성과 병렬 실행 한도는 `docs/ai/orchestration-policy.md`를 조회해 적용하며 Skill에 복사하지 않습니다.
4. 동시성 상한을 넘는 dispatch는 `BLOCKED: DEV CONCURRENCY LIMIT`으로 중단합니다.
5. 공유 쓰기 파일, 계약, schema, 트랜잭션 경계가 하나라도 있으면 `BLOCKED: ONE WRITER` 후 순차 배정합니다.

## Bounded Retry And Stall Rule

1. 수정 가능한 FAIL은 같은 Dev에게 한 번만 반환합니다.
2. 재시도에는 현재 diff, 허용된 수정 범위, focused test 명령, 마지막 `Next Attempt`만 전달합니다.
3. 같은 Dev의 두 번째 수정 실패는 `BLOCKED: RETRY LIMIT`으로 전환합니다. Main은 직접 작업하지 않습니다.
4. Agent가 멈추면 Main은 한 번 상태를 요청합니다. 응답이 없으면 같은 최소 패킷으로 동일 역할을 한 번 재배정합니다.
5. 재배정 Agent도 멈추면 `BLOCKED: AGENT STALLED`로 전환합니다.

### Metadata-only Recovery Dispatch

기계적으로 검증 가능한 metadata 불일치는 `docs/ai/orchestration-policy.md`의 고정 allowlist, 정본, 별도 횟수 제한과 BLOCKED 판정을 조회합니다. Main Coordinator는 파일을 직접 수정하지 않고 원래 Dev 또는 Docs Agent에게 제한된 recovery를 배정합니다. 코드·정책 결함, allowlist 밖 변경, 불명확한 계산 근거에는 이 경로를 사용하지 않으며 상세 파일 목록이나 횟수·산식은 이 Skill에 중복하지 않습니다.

Dev 검증 뒤에는 같은 정책의 Gate 상태 머신에서 현재 상태를 계산합니다. `PRE_REVIEW_READY`에 도달하면 Reviewer·QA를 배정하며, 아직 생성되지 않은 미래 역할 링크나 결과를 요구하지 않습니다. 기계적 metadata 불일치는 정책의 recovery를 적용하고, 안전 정지 사유는 정책의 고정 분류만 사용합니다.

Main Coordinator는 정책의 read-only GitHub snapshot adapter와 harness CLI 출력으로 현재 Gate와 다음 작업을 결정합니다. head가 바뀌면 이전 Review·QA 판정을 재사용하지 않고 CLI가 요구하는 현재 head 역할부터 다시 배정합니다.

## Completion Gate

선택한 mode의 완료 입력은 `docs/ai/orchestration-policy.md`의 실행 모드 표와 Gate 상태 머신을 그대로 따릅니다. 현재 Gate에 필요한 입력이 없으면 해당 입력을 만드는 역할을 배정하고, 미래 Gate 입력 부재는 실패로 판정하지 않습니다. `MERGE_READY` 입력이 하나라도 없으면 merge·close를 허용하지 않습니다.

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
