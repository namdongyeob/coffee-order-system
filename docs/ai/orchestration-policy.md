# GPT-5.6 오케스트레이션 정책

## 목적

모델이 바뀌거나 새 Codex 작업을 시작해도 Issue 범위, 코드 책임, 검증 수준이 달라지지 않게 합니다. 모델과 서브에이전트는 실행 수단이며, 품질 기준의 정본은 `AGENTS.md`, GitHub Issue, 프로젝트 문서, 테스트 evidence입니다.

## 실행 모드 선택

모든 Issue evidence와 PR 본문은 아래 기계 판독 필드를 사용합니다. 형식 검사는 `scripts/harness_gate.py`가 수행합니다.

```text
Execution mode: SOLO|STANDARD|STRICT
Execution mode reason: 비어 있지 않은 선택 근거
```

| 모드 | 선택 조건 | 필수 실행 구성 |
| --- | --- | --- |
| `SOLO` | 오탈자, 링크 등 애플리케이션 동작, build, runtime을 바꾸지 않는 문서 전용 작업 | Solo Agent 한 명만 수행합니다. Main Coordinator와 Coordinator subagent를 시작하지 않으며 빠른 문서 검사만 실행합니다. |
| `STANDARD` | schema, transaction, lock, Kafka, Redis, security, cross-module 위험이 없는 제한된 CRUD, DTO, Controller, 단일 모듈 변경 | Main Coordinator, Dev Agent, 독립 Combined Verifier, CI를 사용합니다. |
| `STRICT` | DB migration 또는 schema, transaction, lock, concurrency, Kafka, Redis, Redisson, DLT, security, multi-module 또는 event contract, performance 또는 recovery, harness 또는 workflow policy 변경 | Main Coordinator, Dev Agent, 별도 Review Agent와 QA Agent, Docs Agent, CI를 모두 사용합니다. |

`SOLO`와 `STANDARD` 조건을 모두 충족한다는 근거가 없으면 `STRICT`를 선택합니다. Redisson, DB 락, 트랜잭션, Kafka 멱등성은 Sol `high`, `max` 또는 `ultra` 후보로 판단하며, 프로젝트 전체 회귀·아키텍처 점검은 `ultra` 읽기 전용으로 배정할 수 있습니다. 동일 실패가 반복되는 장기 작업에는 설치와 명령이 확인된 경우에만 LazyCodex식 반복 루프를 사용합니다.

Review Gate와 QA Gate의 판정 기준 자체를 추가·삭제·변경하는 Issue는 애플리케이션 코드 변경 여부와 무관하게 workflow policy 변경이므로 `STRICT`를 선택합니다. 단순 링크·오탈자 수정처럼 판정 의미를 바꾸지 않는 변경만 이 규칙에서 제외합니다.

## STANDARD Combined Verifier 시점

`STANDARD`의 Dev는 로컬 focused verification과 PR 필수 evidence를 기록한 뒤 Combined Verifier가 pending인 상태로 draft PR을 먼저 생성할 수 있습니다. PR 생성 뒤 저장소 밖의 별도 세션이나 담당자가 수행하는 외부 독립 리뷰를 Combined Verifier로 허용합니다. 외부 독립 리뷰를 사용할 수 없거나 지연되면 현재 오케스트레이션의 Dev와 분리된 내부 Combined Verifier를 반드시 배정합니다.

독립 Combined Verifier는 변경을 작성한 Dev와 같은 역할을 겸하지 않으며 읽기와 focused verification만 수행합니다. PASS 또는 FAIL, 실행 명령, 결과, 남은 위험을 PR 본문 또는 연결된 Issue evidence에 반영합니다. draft PR 생성은 완료가 아닙니다. 독립 Combined Verifier PASS와 CI PASS가 모두 확인되기 전에는 Main이 `READY_FOR_HUMAN`, ready 전환, 완료 또는 merge 권고를 할 수 없습니다.

## 한 작업의 오케스트레이션 소유자

하나의 Issue에는 오케스트레이션 소유자를 하나만 둡니다.

- Codex 일반 모드가 조정하면 외부 LazyCodex 루프를 중첩하지 않습니다.
- `ultra`가 서브에이전트를 조정하면 별도의 병렬 Dev 그룹을 다시 만들지 않습니다.
- LazyCodex 런타임을 사용할 때는 설치된 명령과 상태 저장 위치를 먼저 확인합니다.
- 중첩 사용이 필요하다는 근거가 없으면 가장 단순한 실행 방식 하나를 선택합니다.

이 규칙은 제품 제한이 아니라 중복 탐색, 동일 파일 충돌, 테스트 중복 실행을 막기 위한 프로젝트 운영 정책입니다.

## 역할과 쓰기 권한

| 역할 | 책임 | 쓰기 권한 |
| --- | --- | --- |
| Solo Agent | `SOLO` 작업의 제한된 문서 변경과 빠른 문서 검사 | 지정된 문서 파일만 수정합니다. Main Coordinator 또는 subagent 역할을 겸하지 않습니다. |
| Main Coordinator | `STANDARD`와 `STRICT`의 Issue 큐, 의존성, worktree, 역할 배정, 결과 전달, 상태 관리 | 저장소 파일 쓰기 없음. 코드리뷰·테스트·커밋·푸시·merge 금지. GitHub Issue/PR 상태와 comment만 관리합니다. |
| Spec Agent | 미결정 정책, 대안, 질문, Acceptance Criteria 정리 | 질문 문서와 Issue 초안만 허용합니다. |
| Dev Agent | 하나의 Issue 구현과 focused test | 지정된 production/test 파일의 유일한 작성자입니다. |
| Combined Verifier | `STANDARD`의 독립 코드 검토와 focused verification | 읽기와 검증 실행만 허용합니다. 수정은 Dev에게 반환합니다. |
| Review Agent | `STRICT`의 코드리뷰 | 읽기 전용입니다. 검토 기준은 [Review Gate](review-gate.md)를 따르고 수정은 Dev에게 반환합니다. |
| QA Agent | `STRICT`의 독립 검증 | production/test/docs 수정 금지. 실행 책임과 Level은 [QA Gate](qa-gate.md), [테스트 전략](../testing/test-strategy.md)을 따릅니다. |
| Docs Agent | `STRICT`에서 확정 결과와 evidence 반영 | 지정된 문서만 수정합니다. |

두 Dev Agent가 같은 Service, Entity, migration을 동시에 수정하지 않습니다. 파일이 달라도 하나의 트랜잭션 경계나 이벤트 계약을 함께 바꾸면 순차 작업으로 처리합니다.

## 병렬 구현과 자동 수정 경계

- 독립 Issue는 서로 다른 worktree에서 병렬 실행할 수 있으나 Dev 동시성의 최대치는 2입니다. 세 번째 Dev의 동시 실행은 금지합니다.
- 모든 Dev spawn 또는 재배정 직전에 Main은 현재 오케스트레이션의 active Dev assignment 수를 세고 슬롯을 원자적으로 예약합니다. 두 슬롯이 예약되어 있으면 세 번째 spawn은 금지하며 `BLOCKED: DEV CONCURRENCY LIMIT`을 반환합니다. 슬롯은 Dev가 terminal, terminated 또는 declared stalled 상태가 될 때만 해제합니다. Review, QA, Docs, Combined Verifier는 Dev 슬롯을 소비하지 않습니다.
- 쓰기 파일, 도메인 또는 이벤트 계약, schema, 트랜잭션 경계가 하나라도 공유되면 순차 실행합니다.
- `STRICT`에서만 별도 Review와 QA를 병렬 실행합니다. `STANDARD`는 Combined Verifier 한 명이 독립 검토와 focused verification을 수행합니다.
- 모든 실패를 임의로 자동 수정하는 오케스트레이터는 사용하지 않습니다. 실패를 환경, 테스트 계약, 구현 결함, 정책 미결정으로 분류한 뒤 승인된 수정 범위만 다음 Attempt에 전달합니다.
- 자동 재시도는 같은 명령의 일시적 환경 실패처럼 원인과 수정 범위가 명확할 때만 허용합니다. 정책 변경, migration, 외부 인프라 설정은 사람 확인 없이 자동 수정하지 않습니다.

## Main Coordinator 금지 규칙

- 파일 생성·수정·삭제, 코드 또는 문서 patch 작성.
- diff 내용에 대한 코드 품질 판정과 Review Agent 역할 대행.
- Gradle, Postman, k6, DB, Kafka, Redis 검증 명령 실행.
- Review/QA 실패를 직접 수정하거나 다른 범위로 확대.
- commit, push, merge, Issue close.

Main은 변경 파일 목록, 역할별 완료 보고, 필수 evidence 존재, GitHub Actions 상태만 확인합니다. Agent가 멈추면 한 번 상태를 요청하고 종료·재배정하며, 두 번째 시도도 실패하면 직접 작업하지 않고 `BLOCKED`로 전환합니다.

어떤 Agent도 merge 또는 Issue close를 실행하지 않습니다. Review, QA, CI와 evidence가 있어도 사람의 명시적 승인 뒤에만 사람이 merge 또는 close를 실행합니다.

## Issue 번호와 브랜치 연결

작업 브랜치는 실제로 열린 GitHub Issue 번호를 사용한 `issue-N` 형식이어야 합니다. 접두어는 도구별로 달라도 되므로 `codex/issue-29-harness-baseline`처럼 `issue-N`을 포함하면 됩니다. 여기서 `N`은 `docs/testing/evidence/issue-N/`의 번호, evidence의 `Issue: #N`, Issue URL의 끝 번호와 모두 같아야 합니다. 추정 번호, 로컬 순번, PR 번호를 대신 쓰지 않습니다.

## 하네스 변경 동결과 예외

하네스와 workflow policy는 기능 개발 중 편의나 도구 실험을 위해 바꾸지 않습니다. 변경은 다음 중 하나의 관찰 가능한 근거가 있을 때에만 별도 Issue에서 허용합니다.

- 재현 가능한 실제 하네스 실패와 그 재현 명령.
- Issue별 운영 지표가 보이는 반복 비용 또는 품질 저하.
- 보안 문제 또는 CI·도구 호환성 문제.
- 명시적인 프로젝트 요구사항 변경.

단순 선호, 새 도구 평가, 예방적 추상화는 기능 Issue와 분리합니다. 허용된 하네스 변경은 변경 전 실패 또는 결함 재현, 변경 후 focused harness test, 영향받는 repository gate 결과를 evidence에 남깁니다.

## 모델 사용 기준

현재 Codex가 서브에이전트별 모델 override를 제공하는 경우 다음을 기본 후보로 사용합니다. 기능이 노출되지 않은 환경에서는 부모 모델을 상속하고 역할 규칙만 유지합니다.

| 역할 | 후보 | 기준 |
| --- | --- | --- |
| Main Coordinator | Sol `medium` 또는 `high` | 의존성과 작업 큐를 관리하고 결과를 전달합니다. |
| 일반 Spring 구현 | Terra `high` | 비용과 구현 품질의 균형을 맞춥니다. |
| 파일 탐색, 문서 위치 조사 | Luna `medium` | 빠른 읽기 작업에 사용합니다. |
| 복잡한 동시성·멱등성 검토 | Sol `high`, `max` 또는 `ultra` | 경쟁 조건과 실패 순서를 분석합니다. |

모델을 낮췄다는 이유로 테스트와 리뷰를 줄이지 않으며, 강한 모델을 사용했다는 이유로 evidence를 생략하지 않습니다.

## 실행 환경 확인

Codex 데스크톱 앱이 사용하는 CLI와 PowerShell `PATH`의 CLI 버전이 다를 수 있습니다. 모델 또는 Skill이 동작하지 않을 때는 먼저 두 실행 파일의 `--version`을 비교하고, 오래된 CLI의 모델 거부를 프로젝트 설계 실패로 판단하지 않습니다. 검증 evidence에는 사용한 CLI 버전, 모델, reasoning effort를 함께 기록합니다.

## Issue 품질 루프

실행 순서의 정본은 `docs/ai/agent-rules.md`입니다. 이 문서는 각 단계에 배치할 역할과 쓰기 권한만 결정합니다.

Main의 완료 조건은 코드 품질을 직접 판정하는 것이 아니라 이 문서의 실행 모드 표에 정의된 필수 보고와 CI 상태가 존재하는지 확인하는 것입니다. `SOLO`는 Main을 사용하지 않습니다.

## 품질 개선 지표

최종 프로젝트로 가져갈 때 에이전트 수가 아니라 아래 결과를 비교합니다.

- Issue 범위 밖 변경 파일 수.
- Review에서 발견된 요구사항·회귀·과한 추상화 건수.
- Mock 통과 후 실제 API 검증에서 추가로 발견된 결함 수.
- 실패를 재현할 수 있는 테스트와 evidence의 비율.
- PR 문서와 실제 코드·측정 수치의 불일치 건수.
- 같은 원인으로 재발한 `agent-mistakes.md` 항목 수.

이 수치는 [Issue metrics template](../testing/evidence/issue-metrics-template.md)의 고정 형식으로 `docs/testing/evidence/issue-N/metrics.md`에 남깁니다. 값이 없으면 추정하지 않고 `0`, `없음`, `미측정` 중 해당하는 값을 쓰며 근거를 함께 적습니다. 이 기록은 최종 프로젝트에서 현재 방식과 개선된 방식을 비교하는 기준입니다.

## 최종 프로젝트 이전 범위

그대로 이전할 항목은 전역 작업 규칙, 단일 작성자, 읽기 전용 Review/QA, 검증 레벨, evidence, 사람의 merge 승인입니다. 커피 주문 도메인의 Redisson 키, Kafka topic, Redis ZSET 정책은 이전하지 않고 최종 프로젝트의 도메인 문서와 ADR로 다시 결정합니다.
