# Context Router

작업 시작 시 `docs/`를 재귀적으로 읽지 않습니다. 아래 역할·도메인 hot path의 필수 문서 3~5개만 읽고, 조건이 충족될 때만 추가 문서를 읽습니다. 규칙의 단일 정본은 [규칙 정본 지도](rule-source-map.md)입니다.

## 공통 진입

- 시작. [프로젝트 진입점](../../AGENTS.md)에서 Router를 선택하고 대상 Issue를 확인합니다. 이 단계는 아래 hot path의 필수 문서 수에 더하지 않습니다.
- 조건부. Issue 실행 흐름, 실행 모드와 역할, 규칙 정본 지도는 해당 판단이 필요할 때만 [Issue 실행 흐름](agent-rules.md), [실행 모드와 역할](orchestration-policy.md), [규칙 정본 지도](rule-source-map.md)에서 확인합니다.
- 제외. 이 목록에 없다는 이유만으로 `docs/` 하위 문서를 전부 읽지 않습니다.
- 추가 탐색. Issue의 용어·파일·정책이 hot path와 맞지 않거나 서로 충돌할 때에만 [규칙 정본 지도](rule-source-map.md)에서 해당 정본을 찾아 한 단계 확장합니다.

## 개발 hot path

### 주문과 포인트

- 필수. [요구사항](../product/requirements.md), [범위](../product/scope.md), [주문 정책](../domain/order-policy.md), [포인트 정책](../domain/point-policy.md), [API 명세](../api/api-spec.md)를 읽습니다.
- 조건부. 트랜잭션·재고·잠금이 바뀌면 [동시성 전략](../architecture/concurrency-strategy.md)을 추가합니다.
- 제외. Kafka, Redis 랭킹, 복구 ADR은 관련 계약을 바꾸지 않으면 읽지 않습니다.
- 추가 탐색. 주문·포인트 정책과 API 명세의 계약이 충돌하거나 새 상태 전이가 필요할 때 [도메인 규칙](../domain/domain-rules.md)을 추가합니다.

### Kafka와 복구

- 필수. [Kafka 이벤트 흐름](../architecture/kafka-event-flow.md), [Kafka 선택 ADR](../adr/ADR-003-kafka-vs-rabbitmq-vs-db.md), [재생·복구 ADR](../adr/ADR-005-kafka-replay-recovery.md), [복구 전략](../architecture/recovery-strategy.md)을 읽습니다.
- 조건부. 주문 이벤트 payload가 바뀌면 [주문 정책](../domain/order-policy.md)을 추가합니다.
- 제외. Redis 랭킹과 HTTP API 세부 명세는 직접 계약이 바뀌지 않으면 읽지 않습니다.
- 추가 탐색. 재처리, DLT, 멱등성의 정책이 불명확할 때 [도메인 규칙](../domain/domain-rules.md)을 추가합니다.

### Redis 랭킹

- 필수. [Redis 랭킹 설계](../architecture/redis-ranking.md), [Redis ZSET ADR](../adr/ADR-004-redis-zset-ranking.md), [인기 메뉴 정책](../domain/popular-menu-policy.md), [도메인 규칙](../domain/domain-rules.md)을 읽습니다.
- 조건부. 동시 갱신 또는 원자성이 바뀌면 [동시성 전략](../architecture/concurrency-strategy.md)을 추가합니다.
- 제외. Kafka 재생·복구 문서는 이벤트 복구 계약을 바꾸지 않으면 읽지 않습니다.
- 추가 탐색. 랭킹 조회 API가 바뀔 때 [API 명세](../api/api-spec.md)를 추가합니다.

### 동시성

- 필수. [동시성 전략](../architecture/concurrency-strategy.md), [Redisson·DB 잠금 ADR](../adr/ADR-002-redisson-and-db-pessimistic-lock.md), [주문 정책](../domain/order-policy.md), [도메인 규칙](../domain/domain-rules.md)을 읽습니다.
- 조건부. 성능 목표나 k6 계획이 바뀌면 [k6 우선순위 ADR](../adr/ADR-007-k6-test-priority.md)을 추가합니다.
- 제외. Redis 랭킹 문서는 랭킹 상태를 함께 변경하지 않으면 읽지 않습니다.
- 추가 탐색. 이벤트 발행 순서가 잠금 경계와 연결될 때 [Kafka 이벤트 흐름](../architecture/kafka-event-flow.md)을 추가합니다.

## 검토·운영 hot path

### Review

- 필수. [Review Gate](review-gate.md), [구현 가드레일](implementation-guardrails.md), [테스트 전략](../testing/test-strategy.md), [반복 실수](agent-mistakes.md), [계층 설계 정책](../architecture/layered-design-policy.md)을 읽습니다.
- 조건부. 변경 영역의 개발 hot path와 [오케스트레이션 정책](orchestration-policy.md)을 추가합니다.
- 제외. 변경과 무관한 도메인·ADR 전체는 읽지 않습니다.
- 추가 탐색. 설계 원칙과 Issue 요구사항이 충돌할 때 [요구사항](../product/requirements.md)을 추가합니다.

### QA

- 필수. [QA Gate](qa-gate.md), [테스트 전략](../testing/test-strategy.md), [evidence 안내](../testing/evidence-guide.md), [실행 모드와 역할](orchestration-policy.md)을 읽습니다.
- 조건부. 실제 변경 영역의 개발 hot path와 기존 [검증 로그](../testing/verification-log.md)를 추가합니다.
- 제외. 구현과 무관한 ADR·도메인 문서는 읽지 않습니다.
- 추가 탐색. 필수 검증 Level의 근거가 불명확할 때 [요구사항](../product/requirements.md)을 추가합니다.

### Docs와 evidence

- 필수. [evidence 안내](../testing/evidence-guide.md), [규칙 정본 지도](rule-source-map.md), [완료 전 검사 목록](issue-completion-checklist.md), [실행 모드와 역할](orchestration-policy.md)을 읽습니다.
- 조건부. 대상 Issue의 evidence, [검증 로그](../testing/verification-log.md), 변경된 hot path 정본을 추가합니다.
- 제외. 구현 코드와 전체 도메인 문서는 evidence에 필요한 근거가 없으면 읽지 않습니다.
- 추가 탐색. evidence와 실제 명령·결과가 일치하지 않을 때 해당 명령의 소유 문서와 변경 diff만 추가합니다.

### 하네스와 스크립트

- 필수. [실행 모드와 역할](orchestration-policy.md), [Issue 실행 흐름](agent-rules.md), [테스트 전략](../testing/test-strategy.md), [evidence 안내](../testing/evidence-guide.md)를 읽습니다.
- 조건부. 변경하는 명령의 사용법이나 계약이 바뀌면 해당 스크립트와 직접 연결된 테스트·README만 추가합니다.
- 제외. 애플리케이션 코드, build·infra 설정, 도메인·ADR 문서는 하네스 계약이 직접 참조하지 않으면 읽지 않습니다.
- 추가 탐색. 필수 문서 사이에 실행 주체·Gate·evidence 계약 충돌이 있을 때만 [규칙 정본 지도](rule-source-map.md)에서 충돌한 정본을 찾아 한 단계 확장합니다.

## 하네스 계약

이 문서에 선언한 저장소 상대 Markdown 링크는 [harness gate](../../scripts/harness_gate.py)가 `--check-links` 또는 `--links-only` 실행 때 존재 여부를 검사합니다. 경로를 추가·변경할 때는 성공과 의도된 누락 실패를 [harness 단위 테스트](../../scripts/tests/test_harness_gate.py)로 함께 검증합니다.
