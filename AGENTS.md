# 커피 주문 시스템 에이전트 규칙

## 문서 정본

- 구현 기준은 저장소의 문서입니다.
- 과제 요구사항과 MVP 범위는 `docs/product/requirements.md`를 봅니다.
- 구현 순서와 Issue 초안은 `docs/product/github-issues.md`를 봅니다.
- 도메인 불변 규칙과 정책은 `docs/domain/domain-rules.md`를 봅니다.
- API 계약은 `docs/api/api-spec.md`를 봅니다.
- 3계층 설계와 스파게티 코드 방지 정책은 `docs/architecture/layered-design-policy.md`를 봅니다.
- 코드 위치 기준은 `docs/architecture/source-map.md`를 봅니다.
- 강의 개념과 구현 연결은 `docs/architecture/lecture-mapping.md`를 봅니다.
- 의존성 차이는 `docs/onboarding/dependency-check.md`를 봅니다.
- 모든 Issue의 완료 전 체크리스트는 `docs/ai/issue-completion-checklist.md`를 봅니다.
- 서브에이전트와 병렬 작업 기준은 `docs/ai/subagent-workflow.md`와 `docs/ai/lazycodex-runbook.md`를 봅니다.
- 완료 주장 전에는 `docs/testing/test-strategy.md`와 `docs/testing/verification-log.md`를 확인합니다.

## 작업 순서

1. 대상 Issue와 연결된 문서를 먼저 읽습니다.
2. 한 번에 하나의 Issue 범위만 작업합니다.
3. PR 본문에 실제로 읽은 문서 목록을 남깁니다.
4. 정책이 불명확하면 구현하지 않고 `docs/product/questions-for-tutor.md` 또는 질문 Issue 후보로 분리합니다.
5. 의존성 추가가 필요하면 먼저 `build.gradle` 변경 이유와 검증 방법을 남깁니다.
6. 기본 구조는 Controller-Service-Repository 3계층을 우선합니다.
7. Facade, Generic Manager, 공통 프레임워크성 구조, 광범위한 리팩터링은 명시된 Issue가 없으면 만들지 않습니다.
8. 동작, 계약, 검증 기준이 바뀌면 문서도 함께 갱신합니다.
9. PR을 열기 전 `docs/ai/issue-completion-checklist.md`를 확인합니다.
10. 완료 주장은 검증 근거를 남긴 뒤에만 합니다.

## 아키텍처 경계

- 메뉴, 포인트, 주문, 랭킹, 이벤트 처리는 책임별로 분리합니다.
- Controller, Service, Repository의 책임을 섞지 않습니다.
- 주문 생성과 포인트 차감의 정합성은 DB 트랜잭션과 비관적 락으로 보장합니다.
- Redisson은 `userId` 기준 주문/결제 진입 제어에 사용합니다.
- Kafka는 `OrderCompletedEvent` 발행에 사용합니다.
- Redis ZSET은 원천 데이터가 아니라 인기 메뉴 조회용 파생 데이터입니다.
- DLT 이동은 구현합니다. DLT 재처리는 별도 Issue가 없으면 수동 또는 스크립트 방식으로만 다룹니다.

## 검증 레벨

- Level 1 Unit: 서비스와 도메인 단위 테스트입니다.
- Level 2 Controller: MockMvc/API 요청, 응답, 예외 포맷 테스트입니다.
- Level 3 DB Integration: 실제 DB 또는 Testcontainers DB 기반 JPA, 트랜잭션, 락 검증입니다.
- Level 4 Infra Integration: Kafka, Redis, Redisson, DLT 통합 검증입니다.
- Level 5 Local Run: 로컬 서버 기동 후 실제 API 호출 검증입니다.
- Level 6 Postman/curl/http: 실제 요청 산출물 기반 검증입니다.
- Level 7 k6: Load, Stress, Spike 성능 관찰입니다.

## 완료 주장 규칙

완료 보고는 `docs/ai/done-claim-template.md` 형식을 따릅니다. Mock 테스트만 통과한 경우 로컬 실행, 인프라 통합, 실제 API 검증이 끝났다고 말하지 않습니다.

## 반복 학습 규칙

반복 실수, 헷갈린 정책, 재현 가능한 실패 원인을 발견하면 `docs/ai/agent-mistakes.md`에 한 줄로 남깁니다. 단순한 개인 감상이나 아직 검증되지 않은 추측은 기록하지 않습니다.
