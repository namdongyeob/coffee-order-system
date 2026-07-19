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
| `STRICT` | DB migration 또는 schema, transaction, lock, concurrency, Kafka, Redis, Redisson, DLT, security, multi-module 또는 event contract, performance 또는 recovery, harness 또는 workflow policy 변경 | Main Coordinator, Dev Agent, 별도 Review Agent와 QA Agent, CI를 사용합니다. Dev가 PR 전 evidence와 preflight를 완성하며 metadata 불일치가 있을 때만 Docs Agent를 조건부로 호출합니다. |

Dev가 PR 전 evidence를 완성한 STRICT 흐름에서는 Docs Agent를 기본 dispatch하지 않습니다. metadata 불일치가 있을 때만 조건부로 호출합니다.

위 표는 수동 merge 기본값입니다. 조건부 auto-merge PR은 mode와 무관하게 Main이 서로 다른 Writer·Review·QA를 dispatch하며 mode는 검증 깊이만 줄입니다.

`SOLO`와 `STANDARD` 조건을 모두 충족한다는 근거가 없으면 `STRICT`를 선택합니다. Redisson, DB 락, 트랜잭션, Kafka 멱등성은 고정밀 추론 등급 후보로 판단하며, 프로젝트 전체 회귀·아키텍처 점검은 읽기 전용 대규모 점검 등급으로 배정할 수 있습니다. 역할별 실제 모델 매핑은 [모델·도구 매핑](model-tooling-map.md)을 참조합니다. 동일 실패가 반복되는 장기 작업에는 설치와 명령이 확인된 경우에만 반복 실행 루프를 사용합니다.

Review Gate와 QA Gate의 판정 기준 자체를 추가·삭제·변경하는 Issue는 애플리케이션 코드 변경 여부와 무관하게 workflow policy 변경이므로 `STRICT`를 선택합니다. 단순 링크·오탈자 수정처럼 판정 의미를 바꾸지 않는 변경만 이 규칙에서 제외합니다.

실행 모드는 파일 위치가 아니라 판정 의미 변경 여부로 정합니다. Gate 판정 기준, 검증 소유권, 역할 권한, merge 조건, stale 규칙, 안전 불변조건을 바꾸면 `STRICT`입니다. 의미를 보존하는 문서 이동·분리·링크 정리는 `STANDARD` 후보, 단일 오탈자·링크 수정처럼 판정 의미가 바뀌지 않는 제한 작업은 `SOLO` 후보입니다. 모드가 낮아져도 transaction, lock, event contract, security와 Level 3~6 검증 기준은 낮추지 않으며 `STRICT-lite` 등 새 실행 등급은 추가하지 않습니다.

## STANDARD Combined Verifier 시점

`STANDARD`의 Dev는 로컬 focused verification과 PR 필수 evidence를 기록한 뒤 Combined Verifier가 pending인 상태로 draft PR을 먼저 생성할 수 있습니다. PR 생성 뒤 저장소 밖의 별도 세션이나 담당자가 수행하는 외부 독립 리뷰를 Combined Verifier로 허용합니다. 외부 독립 리뷰를 사용할 수 없거나 지연되면 현재 오케스트레이션의 Dev와 분리된 내부 Combined Verifier를 반드시 배정합니다.

독립 Combined Verifier는 변경을 작성한 Dev와 같은 역할을 겸하지 않으며 읽기와 focused verification만 수행합니다. PASS 또는 FAIL, 실행 명령, 결과, 남은 위험을 PR 본문 또는 연결된 Issue evidence에 반영합니다. draft PR 생성은 완료가 아닙니다. 독립 Combined Verifier PASS와 CI PASS가 모두 확인되기 전에는 Main이 `READY_FOR_HUMAN`, ready 전환, 완료 또는 merge 권고를 할 수 없습니다.

## 한 작업의 오케스트레이션 소유자

하나의 Issue에는 오케스트레이션 소유자를 하나만 둡니다. 외부 반복 루프나 상위 오케스트레이터를 중첩하지 않으며, 중첩이 필요하다는 근거가 없으면 가장 단순한 실행 방식 하나를 선택합니다. 이 규칙은 중복 탐색, 동일 파일 충돌, 테스트 중복 실행을 막기 위한 프로젝트 운영 정책입니다.

## 역할과 쓰기 권한

| 역할 | 책임 | 쓰기 권한 |
| --- | --- | --- |
| Solo Agent | `SOLO` 작업의 제한된 문서 변경과 빠른 문서 검사 | 지정된 문서 파일만 수정합니다. Main Coordinator 또는 subagent 역할을 겸하지 않습니다. |
| Main Coordinator | `STANDARD`와 `STRICT`의 Issue 큐, 의존성, worktree, 역할 배정, 결과 전달, 상태 관리 | 저장소 파일 쓰기 없음. 코드리뷰·테스트·커밋·푸시는 금지합니다. GitHub Issue/PR 상태와 comment만 관리하며, merge·close는 아래 고정 자율 Issue 큐 실험의 모든 조건을 충족한 경우에만 예외입니다. |
| Spec Agent | 미결정 정책, 대안, 질문, Acceptance Criteria 정리 | 질문 문서와 Issue 초안만 허용합니다. |
| Dev Agent | 하나의 Issue 구현과 focused test | 지정된 production/test 파일의 유일한 작성자입니다. |
| Combined Verifier | `STANDARD`의 독립 코드 검토와 focused verification | 읽기와 검증 실행만 허용합니다. 수정은 Dev에게 반환합니다. |
| Review Agent | `STRICT`의 코드리뷰 | 읽기 전용입니다. 검토 기준은 [Review Gate](review-gate.md)를 따르고 수정은 Dev에게 반환합니다. |
| QA Agent | `STRICT`의 독립 검증 | production/test/docs 수정 금지. 실행 책임과 Level은 [QA Gate](qa-gate.md), [테스트 전략](../testing/test-strategy.md)을 따릅니다. |
| Docs Agent | Dev가 PR 전 evidence를 완성한 뒤 metadata 불일치가 확인된 `STRICT` 작업의 정본 동기화 | 지정된 문서만 수정합니다. 기본 dispatch하지 않습니다. |

두 Dev Agent가 같은 Service, Entity, migration을 동시에 수정하지 않습니다. 파일이 달라도 하나의 트랜잭션 경계나 이벤트 계약을 함께 바꾸면 순차 작업으로 처리합니다.

## 병렬 구현과 자동 수정 경계

- 독립 Issue는 서로 다른 worktree에서 병렬 실행할 수 있으나 Dev 동시성의 최대치는 2입니다. 세 번째 Dev의 동시 실행은 금지합니다.
- 모든 Dev spawn 또는 재배정 직전에 Main은 현재 오케스트레이션의 active Dev assignment 수를 세고 슬롯을 원자적으로 예약합니다. 두 슬롯이 예약되어 있으면 세 번째 spawn은 금지하며 `BLOCKED: DEV CONCURRENCY LIMIT`을 반환합니다. 슬롯은 Dev가 terminal, terminated 또는 declared stalled 상태가 될 때만 해제합니다. Review, QA, Docs, Combined Verifier는 Dev 슬롯을 소비하지 않습니다.
- 쓰기 파일, 도메인 또는 이벤트 계약, schema, 트랜잭션 경계가 하나라도 공유되면 순차 실행합니다.
- `STRICT`에서만 별도 Review와 QA를 병렬 실행합니다. `STANDARD`는 Combined Verifier 한 명이 독립 검토와 focused verification을 수행합니다.
- 모든 실패를 임의로 자동 수정하는 오케스트레이터는 사용하지 않습니다. 실패를 환경, 테스트 계약, 구현 결함, 정책 미결정으로 분류한 뒤 승인된 수정 범위만 다음 Attempt에 전달합니다.
- 자동 재시도는 같은 명령의 일시적 환경 실패처럼 원인과 수정 범위가 명확할 때만 허용합니다. 정책 변경, migration, 외부 인프라 설정은 사람 확인 없이 자동 수정하지 않습니다.

### 최소 packet과 evidence 정본

모든 자동 역할은 `fork_turns="none"`으로 시작합니다. packet은 Issue URL, worktree, base/head SHA, Acceptance Criteria, 허용 쓰기 범위, 직접 관련 정본 1~5개 경로, diff·focused 검증, 직전 P0/P1 또는 마지막 실패 하나, `SUBAGENT-STOP`, 요약 출력 예산만 담습니다. source·전체 대화·전체 log는 복제하지 않으며 상세 allowlist와 진행 순서의 정본은 [Issue 개발 흐름](agent-rules.md)입니다.

evidence 정본 위치와 PR 전 preflight는 [Evidence Guide](../testing/evidence-guide.md)를 따릅니다. mode floor, Java CI, Review·QA stale, runtime evidence stale은 `base...HEAD` name-status를 읽는 단일 fail-closed 분류기로 계산하며 unknown·mixed·rename·delete와 선언 mode 하향은 무거운 경로로 막습니다.

### 검증 소유권과 범위 밖 flaky

- Dev focused 실행, QA 독립 검증, GitHub Actions `quality-gates`의 전체 Level 1 회귀 판정 등 테스트 실행 소유권의 단일 정본은 [테스트 전략](../testing/test-strategy.md)입니다. 이 문서는 그 규칙을 복제하지 않고 참조만 합니다.
- 범위 밖 flaky의 격리·후보 기록·격리 FAIL 처리와 안전 정지 절차의 정본은 [Issue 개발 흐름](agent-rules.md)입니다. current diff와 관련될 수 있는 실패는 flaky로 분류하지 않고 현재 Issue 결함으로 처리합니다.
- 코드·정책·보안·데이터 P0/P1은 원래 Dev에게 한 번 반환하고 두 번째 P0/P1은 안전 정지합니다. 완료 기준을 위반하지 않는 P2는 비차단 권고 또는 후속 Issue로 남깁니다.

## Main Coordinator 금지 규칙

- 파일 생성·수정·삭제, 코드 또는 문서 patch 작성.
- diff 내용에 대한 코드 품질 판정과 Review Agent 역할 대행.
- Gradle, Postman, k6, DB, Kafka, Redis 검증 명령 실행.
- Review/QA 실패를 직접 수정하거나 다른 범위로 확대.
- commit, push, 그리고 고정 자율 Issue 큐 실험 밖에서의 merge, Issue close.

Main은 변경 파일 목록, 역할별 완료 보고, 필수 evidence 존재, GitHub Actions 상태만 확인합니다. 진행은 wait/notification으로 기다리고 timeout·stall 때만 진단 snapshot을 한 번 허용합니다. 장기 명령 handle은 이어받고 상태 변화 없는 shell polling은 반복하지 않습니다. Agent가 멈추면 한 번 상태를 요청하고 재배정하며 두 번째 실패는 `BLOCKED`입니다.

고정 자율 Issue 큐 실험 밖에서는 어떤 Agent도 merge 또는 Issue close를 실행하지 않습니다. Review, QA, CI와 evidence가 있어도 사람의 명시적 승인 뒤에만 사람이 merge 또는 close를 실행합니다. 고정 자율 Issue 큐 실험에서는 아래 열거된 모든 조건을 충족한 Main Coordinator만 merge 또는 Issue close를 실행할 수 있습니다.

## Issue 번호와 브랜치 연결

작업 브랜치는 실제로 열린 GitHub Issue 번호를 사용한 `issue-N` 형식이어야 합니다. 접두어는 도구별로 달라도 되므로 `codex/issue-29-harness-baseline`처럼 `issue-N`을 포함하면 됩니다. 여기서 `N`은 `docs/testing/evidence/issue-N/`의 번호, evidence의 `Issue: #N`, Issue URL의 끝 번호와 모두 같아야 합니다. 추정 번호, 로컬 순번, PR 번호를 대신 쓰지 않습니다.

## 하네스 변경 동결과 예외

하네스와 workflow policy는 기능 개발 중 편의나 도구 실험을 위해 바꾸지 않습니다. 변경은 다음 중 하나의 관찰 가능한 근거가 있을 때에만 별도 Issue에서 허용합니다.

- 재현 가능한 실제 하네스 실패와 그 재현 명령.
- Issue별 운영 지표가 보이는 반복 비용 또는 품질 저하.
- 보안 문제 또는 CI·도구 호환성 문제.
- 명시적인 프로젝트 요구사항 변경.

단순 선호, 새 도구 평가, 예방적 추상화는 기능 Issue와 분리합니다. 허용된 하네스 변경은 변경 전 실패 또는 결함 재현, 변경 후 focused harness test, 영향받는 repository gate 결과를 evidence에 남깁니다.

## 모델·도구와 실행 환경

역할별 모델 후보 매핑과 실행 환경 확인 절차는 핵심 실행 계약에 두지 않고 [모델·도구 매핑](model-tooling-map.md)에서만 관리합니다. 모델을 낮췄다는 이유로 테스트와 리뷰를 줄이지 않으며, 강한 모델을 사용했다는 이유로 evidence를 생략하지 않습니다.

## Issue 품질 루프

실행 순서의 정본은 `docs/ai/agent-rules.md`입니다. 이 문서는 각 단계에 배치할 역할과 쓰기 권한만 결정합니다.

Main의 완료 조건은 코드 품질을 직접 판정하는 것이 아니라 이 문서의 실행 모드 표에 정의된 필수 보고와 CI 상태가 존재하는지 확인하는 것입니다. `SOLO`는 Main을 사용하지 않습니다.

## 지표와 최종 프로젝트 이전

metrics는 완료 gate가 아니며 pilot·회고의 on-demand 집계만 [하네스 지표와 이전](harness-metrics-and-transfer.md)에서 관리합니다.

## merge 거버넌스

- 최종 merge gate의 `PASS`를 증명하는 유일한 machine ground truth는 최종 source SHA의 고정 GitHub Actions `quality-gates` conclusion입니다. PR `edited`는 별도 `metadata-gates` 이름·concurrency로 source run을 취소·대체하지 않고, docs/evidence-only는 Python gate만 실행하며 source/test/build/runtime은 단일 Gradle `test` invocation을 유지합니다. 로컬·Agent evidence는 CI를 대체하지 않습니다.
- 기본 merge 거버넌스는 사람 도메인 오너의 최종 merge 승인입니다. AI는 Review·QA·CI로 결함을 찾고 사람이 최종 merge를 승인하며, 이 기본값을 최종 팀 프로젝트로 그대로 이전합니다. 팀 저장소 전환 시 활성화할 branch rule의 구체 항목(approval 수, stale dismissal, unresolved conversation 차단)은 [팀 저장소 merge governance baseline](team-merge-governance-baseline.md)을 따릅니다.
- 이 저장소 한정 자율 Issue 큐 실험에서만 서로 다른 Writer·Review·QA, 같은 source-tree SHA의 Review `APPROVED`·QA `PASS`, 그 source SHA의 required CI PASS와 merge 안전 조건을 모두 충족할 때 Main의 조건부 auto-merge·close를 허용합니다. 누락·FAIL·BLOCKED·stale은 mode와 무관하게 차단합니다. #137 자체는 기존 evidence 6종·preflight·fresh Review·QA·최신 CI를 소급 완화하지 않으며 새 경량 기본값은 #137 merge 뒤 Issue부터 적용합니다.

## 개인·팀 역할 구성

개인 작업과 팀 작업의 역할 구성을 구분합니다. AI는 1차 결함 탐지와 독립 검증을 담당하고, 사람 팀원은 도메인 의도, 요구사항 일치, 설명 가능성과 최종 merge 승인을 담당합니다. 실제 팀원 이름, GitHub 계정, CODEOWNERS는 팀 확정 뒤 별도로 설정합니다.
