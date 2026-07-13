# 문서 lifecycle 분류

`docs/` 전체 문서의 실제 사용 경로를 감사하고 `active / conditional / archive / obsolete 후보`로 분류합니다(Issue #36). 판정은 `docs/ai/context-router.md`(AI hot path), `docs/ai/rule-source-map.md`(정본 지도), `README.md`(사람 온보딩 색인) 세 경로의 실제 링크 여부를 근거로 하며, 정적 미참조만으로 obsolete를 단정하지 않습니다.

이 분류는 스냅샷입니다. `docs/` 구조를 바꾸는 후속 Issue(특히 #58 이후 예정된 하네스 재정비)가 반영되면 다시 감사해야 합니다.

## active — 기본 또는 역할별 hot path에서 직접 사용

`docs/ai/context-router.md`의 각 hot path "필수" 목록에 직접 이름이 올라 있거나, 프로젝트 진입점 자체인 문서입니다.

| 문서 | 참조자 |
| --- | --- |
| `AGENTS.md` | 프로젝트 진입점(정본). |
| `CLAUDE.md` | Claude 진입점, `AGENTS.md` 참조. |
| `.codex/skills/coffee-order-issue-loop/SKILL.md` | Main Coordinator 필수 진입, `rule-source-map.md` 정본. |
| `docs/ai/context-router.md` | Router 자체. `rule-source-map.md`, 대부분의 evidence 파일에서 참조. |
| `docs/ai/orchestration-policy.md` | Review/QA/Docs/하네스 hot path 필수, `rule-source-map.md` 정본. |
| `docs/ai/agent-rules.md` | 하네스 hot path 필수, `rule-source-map.md` 정본, Issue/PR 템플릿. |
| `docs/ai/rule-source-map.md` | Docs·하네스 hot path 필수, Router가 직접 링크. |
| `docs/ai/review-gate.md` | Review hot path 필수, `rule-source-map.md` 정본. |
| `docs/ai/qa-gate.md` | QA hot path 필수, `rule-source-map.md` 정본. |
| `docs/ai/implementation-guardrails.md` | Review hot path 필수. |
| `docs/ai/agent-mistakes.md` | Review hot path 필수, `rule-source-map.md` 정본. |
| `docs/ai/issue-completion-checklist.md` | Docs hot path 필수, `rule-source-map.md` 정본, PR 템플릿. |
| `docs/testing/test-strategy.md` | Review·QA·하네스 hot path 필수, `rule-source-map.md` 정본. |
| `docs/testing/evidence-guide.md` | Docs·QA·하네스 hot path 필수, `rule-source-map.md` 정본, Issue/PR 템플릿. |
| `docs/product/requirements.md` | 주문·포인트 hot path 필수, README 색인. |
| `docs/product/scope.md` | 주문·포인트 hot path 필수. |
| `docs/domain/order-policy.md` | 주문·포인트·Kafka·동시성 hot path 필수. |
| `docs/domain/point-policy.md` | 주문·포인트 hot path 필수. |
| `docs/api/api-spec.md` | 주문·포인트 hot path 필수, README 색인. |
| `docs/domain/domain-rules.md` | Kafka·Redis 랭킹·동시성 hot path 필수, README 색인. |
| `docs/architecture/kafka-event-flow.md` | Kafka hot path 필수. |
| `docs/adr/ADR-003-kafka-vs-rabbitmq-vs-db.md` | Kafka hot path 필수. |
| `docs/adr/ADR-005-kafka-replay-recovery.md` | Kafka hot path 필수. |
| `docs/architecture/recovery-strategy.md` | Kafka hot path 필수. |
| `docs/architecture/redis-ranking.md` | Redis 랭킹 hot path 필수. |
| `docs/adr/ADR-004-redis-zset-ranking.md` | Redis 랭킹 hot path 필수. |
| `docs/domain/popular-menu-policy.md` | Redis 랭킹 hot path 필수. |
| `docs/architecture/concurrency-strategy.md` | 주문·포인트(조건부)·Redis 랭킹(조건부)·동시성(필수) hot path. |
| `docs/adr/ADR-002-redisson-and-db-pessimistic-lock.md` | 동시성 hot path 필수. |
| `docs/architecture/layered-design-policy.md` | Review hot path 필수. |
| `docs/testing/evidence/attempt-log-template.md` | `evidence-guide.md`가 지정한 복사 템플릿. |
| `docs/testing/evidence/issue-metrics-template.md` | `evidence-guide.md`가 지정한 복사 템플릿. |

## conditional — 특정 도메인·장애·검증 조건에서만 사용

Router의 "조건부"·"추가 탐색"·"조건부 참조 문서" 목록에 있거나, README에는 링크돼 있지만 AI hot path 필수 목록 밖인 문서, 또는 특정 작업(로컬 실행, k6, Postman, onboarding)에서만 필요한 운영 문서입니다.

| 문서 | 사용 조건 | 비고 |
| --- | --- | --- |
| `docs/ai/model-tooling-map.md` | 서브에이전트 모델 override·실행 환경 CLI 문제 다룰 때 | Router 조건부 참조 문서로 명시. |
| `docs/ai/harness-metrics-and-transfer.md` | 지표 집계·팀 이전 준비 | Router 조건부 참조 문서로 명시. |
| `docs/ai/autonomous-queue-runbook.md` | 이 저장소 한정 자율 큐 실험 운영 | Router 조건부 참조 문서로 명시. |
| `docs/adr/ADR-007-k6-test-priority.md` | 성능 목표·k6 계획 변경 시 | 동시성 hot path 조건부. |
| `docs/domain/domain-rules.md`(재확인) | 계약 충돌·새 상태 전이 필요 시 | 위 active 표와 별도로 "추가 탐색" 조건도 있음. |
| `docs/adr/ADR-001-layered-architecture.md` | 계층 경계 설계 근거 확인 시 | **Router·정본 지도 어디서도 참조되지 않음(도달성 문제).** `layered-design-policy.md`가 같은 주제를 다루지만 이 ADR을 문자열로 링크하지 않음. |
| `docs/adr/README.md` | 새 ADR 작성·Superseded 관계 표기 시 | **Router·정본 지도 어디서도 참조되지 않음(도달성 문제).** `docs/adr/ADR-001`, `ADR-006`을 개별 색인하지도 않는 순수 운영 규칙 문서(상태 표기, Superseded 절차, 실제/계획된 검증 구분). 과거 evidence(issue-28)에서만 언급됨. |
| `docs/adr/ADR-006-querydsl-and-indexing.md` | QueryDSL·인덱스 설계 근거 확인 시 | **Router·정본 지도 어디서도 참조되지 않음(도달성 문제).** `docs/db/indexing-explain.md`와 내용이 겹칠 수 있어 통합 검토 여지. |
| `docs/db/erd.md` | DB 스키마 참고 | **AI Router 밖.** README.md:36에서 링크(사람 온보딩 색인). |
| `docs/db/indexing-explain.md` | 인덱스 설계 확인 | evidence/issue-16에서만 참조. Router 밖. |
| `docs/architecture/overview.md` | 아키텍처 전체 조망 | **AI Router 밖.** README.md에서만 링크. |
| `docs/architecture/source-map.md` | 코드 위치 참고 | `layered-design-policy.md`에서만 참조. Router 밖. |
| `docs/architecture/lecture-mapping.md` | 강의 개념-구현 매핑 참고 | **AI Router 밖.** README.md:34에서 링크(사람 온보딩 색인). |
| `docs/operations/local-runbook.md` | 로컬 실행·관찰 | evidence 파일에서만 참조. Router 밖이지만 실제 운영 절차로 유효. |
| `docs/operations/kafka-redis-runbook.md` | Redis 랭킹 maintenance rebuild | evidence 파일에서만 참조. Router 밖이지만 실제 운영 절차로 유효. |
| `docs/onboarding/project-setup.md` | 신규 환경 첫 확인 | evidence/issue-54에서만 참조. |
| `docs/onboarding/dependency-check.md` | 의존성 보강 확인 | README에서 링크. |
| `docs/onboarding/codex-safety.md` | `.codex/config.toml` 권한 확인 | `rule-source-map.md`가 정본으로 지정. |
| `docs/testing/k6-plan.md` | k6 Load/Stress/Spike 스크립트 작성·실행 | **참조 0건.** `test-strategy.md`의 "k6 우선순위" 절이 내용상 이 문서를 전제로 하지만 링크가 없음. 링크 보강 권고. |
| `docs/testing/postman-guide.md` | Level 6 수동 API 검증 | **참조 0건.** `evidence-guide.md`의 "작업별 Evidence > API" 행과 맞닿아 있지만 링크가 없음. 링크 보강 권고. |
| `docs/ai/lazycodex-runbook.md` | `$ulw-plan` 등 실제 LazyCodex 명령을 쓰는 예외 상황 | `rule-source-map.md`가 정본으로 지정하지만 `context-router.md`의 "조건부 참조 문서" 목록에는 빠져 있음(Router·정본 지도 불일치). |
| `docs/product/github-issues.md` | 새 GitHub Issue 초안 작성 시 복사용 템플릿 | README에서 링크. |

## archive — 과거 결정·계획·evidence 보존용

| 문서 | 보존 근거 |
| --- | --- |
| `docs/testing/evidence/issue-{number}/**`(약 200개 파일) | `docs/testing/evidence-guide.md`의 Issue별 evidence 보존 정책. 개별 분류하지 않고 이 항목으로 일괄 보존합니다. 소급 수정·삭제 금지. |
| `docs/testing/evidence/legacy/verification.md` | 하네스 의무화 이전 원문 행 보존(Legacy). |
| `docs/testing/evidence/orchestration-skill/baseline.md` | 문서 본문에 "최초 설계 당시 관찰 기록이며 현재 계약은 `orchestration-policy.md`를 따른다"고 명시된 자체 선언 archive. Issue #88에서 상단에 "Status: Archive" 배지를 추가했습니다. |
| `docs/testing/evidence/orchestration-skill/validation.md` | `baseline.md`와 같은 실험 계열의 관찰 기록. |
| `docs/ai/archive/5-6-orchestration-implementation-plan.md` | 폴더명 자체가 `archive/`이며 초기 오케스트레이션 설계 계획의 역사 기록. |
| `docs/performance/k6-results.md` | Issue #13 실측 k6 결과. Issue #88에서 `docs/testing/test-strategy.md`의 "k6 우선순위" 절에 링크를 추가해 discoverability를 보강했습니다. |

## obsolete 후보 — 현재 정본과 중복되거나 어떤 경로에서도 사용되지 않아 별도 삭제 검토가 필요

이 표의 항목은 이 Issue에서 삭제하지 않습니다. 실제 삭제·통합 여부는 Issue #88(후속)에서 검토합니다.

| 문서 | 근거 |
| --- | --- |
| ~~`docs/product/questions-for-tutor.md`~~ | Issue #88에서 삭제 완료. `git log --follow`로 확인한 결과 최초 커밋 이후 한 번도 수정되지 않았고 다른 커밋 메시지에서도 참조되지 않아, 실사용 흔적 없음을 확인했습니다. |
| ~~`docs/testing/verification-matrix.md`~~ | Issue #88에서 삭제 완료. `git log --follow`로 확인한 결과 최초 커밋 이후 한 번(`verification-log.md` 경로 변경에 따른 기계적 문자열 치환)만 수정됐고, 실제 내용 갱신이나 다른 문서에서의 참조는 없었습니다. |
| `docs/troubleshooting/troubleshooting-log.md` | README·hot path·정본 지도 어디서도 참조되지 않음. 헤더만 있고 실제 기록 행이 0건인 빈 템플릿 — 실제로 쓰인 적이 없습니다. |
| `docs/ai/subagent-workflow.md` | `rule-source-map.md`가 "역할과 쓰기 권한" 정본을 `orchestration-policy.md`로 지정했고, 그 문서의 "최소 packet과 evidence 정본" 절이 이 문서가 정의하는 역할별 입출력 packet 내용을 이미 포함합니다. 이 문서를 직접 참조하는 곳은 `rule-source-map.md`의 참조 목록 한 줄뿐이라 사실상 중복 정본입니다. |

## Router·정본 지도 도달성 문제 요약

- `ADR-001-layered-architecture.md`, `ADR-006-querydsl-and-indexing.md`, `docs/adr/README.md`: 정본 문서 어디서도 파일 경로가 링크되지 않습니다. 실제 결정 근거·운영 규칙으로는 유효해 보이나 Router의 조건부 목록에 없습니다.
- `docs/testing/k6-plan.md`, `docs/testing/postman-guide.md`: 관련 정본(`test-strategy.md`, `evidence-guide.md`)이 내용상 이 문서들을 전제로 하지만 링크가 없어 실제로는 찾기 어렵습니다.
- `docs/ai/lazycodex-runbook.md`: `rule-source-map.md`는 정본으로 지정했지만 `context-router.md`의 "조건부 참조 문서" 목록에는 없어 두 정본 사이에 불일치가 있습니다.
- `docs/db/erd.md`, `docs/architecture/lecture-mapping.md`, `docs/architecture/overview.md`: AI Router 밖이지만 README.md(사람 온보딩 색인)에서 링크되므로 obsolete가 아닙니다. 정적 미참조만으로 판정하면 안 되는 사례로 이 감사 과정에서 실제로 확인했습니다.

## 후속 조치 후보

- Issue #88(완료): `questions-for-tutor.md`·`verification-matrix.md` 삭제, `k6-results.md`·`orchestration-skill/baseline.md` discoverability 보강.
- 이 감사에서 새로 발견한 추가 후속 후보(별도 Issue로 제안, 이 PR에서 자동 생성하지 않음):
  - `docs/testing/k6-plan.md`, `docs/testing/postman-guide.md`를 `test-strategy.md`/`evidence-guide.md`에서 링크.
  - `ADR-001`, `ADR-006`, `docs/adr/README.md`를 `context-router.md`의 적절한 hot path 조건부 목록에 추가하거나, 왜 빠졌는지 확인.
  - `docs/ai/subagent-workflow.md`와 `docs/ai/orchestration-policy.md`의 packet 정의 중복 정리.
  - `docs/ai/lazycodex-runbook.md`를 `context-router.md`의 "조건부 참조 문서" 목록에 추가하거나 `rule-source-map.md`에서 제거.
  - `docs/troubleshooting/troubleshooting-log.md`를 실제로 쓸지, 빈 템플릿을 obsolete로 정리할지 결정.
