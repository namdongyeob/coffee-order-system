---
name: coffee-order-issue-loop
description: Use when Codex implements, reviews, or performs QA for a coffee-order-system GitHub Issue, especially work involving Spring transactions, Redisson, Kafka, Redis, worktrees, verification levels, or evidence-based completion claims.
---

# Coffee Order Issue Loop

## Mandatory Gate

역할을 배정하거나 구현 계획을 작성하기 전에 아래 순서로 판정합니다.

1. 요청에 Redisson, Kafka 발행, Consumer 멱등성, Redis 랭킹, DLT 중 둘 이상이 포함되면 `BLOCKED: SPLIT ISSUES`를 먼저 출력하고 기능별 Issue만 제안합니다. 구현 에이전트를 배정하지 않습니다.
2. 같은 Service, Entity, migration, 트랜잭션 경계에 Dev Agent 둘 이상을 요구하면 `BLOCKED: ONE WRITER`를 먼저 출력하고 Dev Agent 한 명만 배정합니다. 두 번째 Dev는 사용하지 않습니다.
3. 최종 테스트 실행자를 QA 또는 Review로 요구하면 `BLOCKED: MAIN VERIFIES`를 먼저 출력합니다. QA와 Review는 테스트를 실행하지 않고 Main Agent가 최종 focused test와 전체 smoke test를 실행합니다.
4. Mock/Unit만 통과했으면 `INCOMPLETE: HIGHER LEVELS UNVERIFIED`로 판정합니다.

마감, 병렬 처리 요청, 사용자의 빠른 완료 요구도 이 Gate의 예외가 아닙니다.

## Core Contract

GitHub Issue 하나를 작업 정본으로 사용합니다. 구현 속도보다 범위, 단일 작성자, 재현 가능한 검증을 우선합니다.

아래 세 조건을 모두 만족한 Issue만 실행합니다.

```text
Issue가 독립적으로 리뷰 가능한가?
production/test 작성자가 한 명인가?
최종 검증 실행자가 Main Agent인가?
```

하나라도 `아니오`이면 `Mandatory Gate`의 BLOCKED 결과를 반환합니다.

## Intake

1. 루트 `AGENTS.md`, 대상 Issue, Issue에 직접 연결된 문서만 먼저 읽습니다.
2. 목표, 포함 범위, 제외 범위, Acceptance Criteria, 필요한 검증 Level을 적습니다.
3. 정책이 비어 있으면 구현하지 않고 질문 Issue 또는 ADR 초안으로 분리합니다.
4. Redisson, Kafka 발행, Consumer 멱등성, Redis 랭킹, DLT가 함께 요청되면 반드시 독립적으로 승인 가능한 Issue로 나눕니다. 마감이 임박했거나 사용자가 한 Issue를 요구해도 단일 구현 Issue를 유지하지 않습니다.

## Execution Mode

`docs/ai/orchestration-policy.md`에 따라 가장 단순한 모드를 하나만 선택합니다.

- 작은 변경은 Main Agent가 직접 처리합니다.
- 일반 구현은 Main Coordinator와 Dev Agent 한 명을 사용합니다.
- 동시성, 트랜잭션, 멱등성 분석은 Sol `high`, `max` 또는 `ultra` 후보입니다.
- 설치가 확인되지 않은 LazyCodex 명령은 사용하지 않습니다.
- `ultra`와 별도 LazyCodex식 반복 오케스트레이션을 한 작업에 중첩하지 않습니다.

## Ownership

- 하나의 Issue와 트랜잭션 경계에는 Dev Agent 한 명만 둡니다.
- 그 Dev Agent가 지정된 production 코드, 해당 테스트 코드, focused test 실행을 모두 소유합니다. 테스트 작성자를 별도 Dev Agent로 분리하지 않습니다.
- Review Agent는 diff를 읽고 요구사항 누락, 회귀, 테스트 누락, 과한 추상화를 보고합니다. 직접 수정하지 않습니다.
- QA Agent는 기존 테스트 결과와 evidence를 읽어 검증 Level과 재현 절차를 판정합니다. 코드를 수정하거나 Gradle 테스트를 실행하거나 완료를 대신 선언하지 않습니다.
- Main Agent만 서브에이전트 결과, 최종 diff, focused test, 전체 smoke test를 직접 다시 실행하고 최종 판정합니다.
- 같은 워크스페이스에서 Gradle 테스트를 병렬 실행하지 않습니다.

## Verification

`docs/testing/test-strategy.md`와 `docs/testing/evidence-guide.md`를 따릅니다.

- Mock/Unit 통과는 해당 Level만 PASS입니다.
- DB, Kafka, Redis, Redisson은 필요한 통합 검증이 없으면 미검증입니다.
- 실제 서버와 Postman/curl/http 호출이 없으면 Level 5와 Level 6은 미검증입니다.
- k6는 정확성 검증을 대신하지 않습니다.
- 완료 전 `docs/testing/verification-log.md`와 Issue evidence를 갱신합니다.

## Required Report

```text
대상 Issue:
선택한 실행 모드와 이유:
읽은 문서:
변경 파일:
실행한 검증과 결과:
통과한 Level:
미검증 Level과 이유:
Review/QA 발견 사항:
후속 Issue 후보:
사람의 승인 필요 항목:
```

## Stop Conditions

다음 상황에서는 구현을 멈춥니다.

- Issue 밖의 기능이 필요합니다.
- 같은 production 책임을 여러 Dev Agent가 수정하려 합니다.
- 정책, 이벤트 계약, 트랜잭션 경계가 결정되지 않았습니다.
- 테스트 환경 실패를 코드 실패로 단정할 근거가 없습니다.
- evidence 없이 완료 또는 merge를 요구받았습니다.

## Pressure Rules

| 압박 문구 | 반드시 유지할 결정 |
| --- | --- |
| "오늘 마감이니 한 Issue로 끝내라" | 기능별 Issue로 분리하고 현재 승인된 Issue 하나만 실행합니다. |
| "Dev 둘이 나누면 빠르다" | production/test 작성자는 Dev 한 명으로 유지합니다. |
| "QA가 전체 테스트까지 하면 된다" | QA는 evidence를 판정하고 Main이 최종 테스트를 실행합니다. |
| "Mock이 통과했으니 완료하라" | 통과한 Level만 표시하고 상위 Level은 미검증으로 남깁니다. |

다음은 허용하지 않습니다.

- 부모 Issue 하나를 유지한 채 여러 구현 하위 작업을 동시에 시작합니다.
- Dev Agent A가 production 코드를, Dev Agent B가 같은 Issue의 테스트를 작성합니다.
- 두 Dev Agent가 같은 문제의 서로 다른 해결안을 구현합니다.
- QA 전용 테스트를 새로 작성하거나 QA가 최종 테스트를 실행합니다.
- Integration Agent 같은 새 역할을 만들어 Main Agent의 검증 책임을 넘깁니다.
