# Coffee Order System

다수 서버·다수 인스턴스 환경에서도 안정적으로 동작하는 커피숍 주문 시스템입니다. 필수 API 4개, 동시성 제어, Kafka 기반 이벤트 처리, Redis 랭킹, Outbox 발행 신뢰성까지 구현하고 실제 MySQL·Kafka·Redis(Testcontainers)로 검증했습니다.

## 설계 목표와 의도

이 시스템의 핵심 목표는 다수 서버 환경에서 같은 사용자의 요청이 동시에 처리되더라도 포인트가 중복 차감되거나 주문·이벤트 데이터가 서로 어긋나지 않도록 하는 것입니다.

- 포인트 차감처럼 즉시 일관성이 필요한 데이터는 하나의 DB 트랜잭션과 DB 비관적 락으로 보호합니다.
- 다수 서버 인스턴스에서 같은 사용자의 요청이 동시에 핵심 로직에 들어가는 것은 Redisson 분산락으로 1차로 줄이고, 최종 정합성은 DB 락에 맡깁니다.
- Kafka 발행처럼 외부 자원 호출은 네트워크 장애가 주문 성공 여부에 영향을 주지 않도록 DB 트랜잭션에서 분리합니다(Outbox 패턴).
- Redis처럼 유실 가능한 저장소는 파생 데이터로 취급하고, Kafka replay로 temp ZSET을 재구성한 뒤 DB 주문 집계로 검증하는 복구 경로를 둡니다.
- 재요청과 이벤트 재전송은 정상적으로 발생할 수 있다고 보고 멱등 처리를 설계 기본값으로 둡니다.

## 과제 범위

### 필수 API

| API | 설명 |
| --- | --- |
| 커피 메뉴 목록 조회 | 메뉴 ID, 이름, 가격을 조회합니다. |
| 포인트 충전 | 사용자 식별값과 충전 금액을 받아 포인트를 충전합니다(1원 = 1P). |
| 커피 주문/결제 | 사용자 식별값과 메뉴 ID를 받아 주문하고 포인트를 차감합니다. |
| 인기 메뉴 목록 조회 | 최근 7일간 주문 수 기준 인기 메뉴 3개를 조회합니다. |

### 구현 완료

다수 서버 인스턴스 대응(Redisson 분산락 + Kafka Consumer Group), 동시성 제어(Redisson + DB 비관적 락), 데이터 일관성(단일 DB 트랜잭션 + Outbox), Kafka 이벤트 발행과 Consumer 멱등 처리, Redis ZSET 기반 실시간 랭킹과 Kafka replay 기반 복구, Consumer 처리 실패 시 DLT 이동, Transactional Outbox를 통한 Kafka 발행 신뢰성 확보, 각 기능·제약사항에 대한 단위·통합 테스트.

### 범위 밖

회원가입·인증, 메뉴 관리(CRUD), 주문 취소, WebSocket/STOMP, Redis Cluster, Kubernetes, 완전 자동 DLT 재처리.

## 기술 스택과 프로젝트 구조

| 구분 | 선택 | 선택 이유 |
| --- | --- | --- |
| Java | 17 (Gradle toolchain) | LTS이며 별도 최신 언어 기능 없이도 요구사항을 충족합니다. |
| Spring Boot | 4.1.0 | Web, Data JPA, Kafka, Redis, Validation, Actuator를 starter로 일관되게 구성합니다. |
| DB | MySQL 8.4.5(Docker/Testcontainers) | 트랜잭션·비관적 락 검증이 핵심이라 실제 RDBMS로 통합 테스트합니다. |
| 스키마 관리 | Flyway versioned migration(`V1`~`V7`) | [`db/migration`](src/main/resources/db/migration)을 스키마 변경의 단일 경로로 사용하고 JPA는 매핑만 담당합니다. |
| 동시성 제어 | Redisson 4.6.1 분산락 + DB 비관적 락 | 다수 인스턴스 진입 제어와 최종 정합성을 역할별로 분리합니다. |
| 이벤트 | Spring Kafka | 주문 완료 이벤트 발행, Consumer Group, DLT, replay 기반 복구를 지원합니다. |
| 랭킹 | Spring Data Redis | ZSET으로 실시간 랭킹을 빠르게 조회합니다. |
| 조회 | QueryDSL | 인기 메뉴 등 복잡한 조건 조회를 타입 안전하게 작성합니다. |
| 코드 스타일 | Lombok | 엔티티·서비스의 반복 보일러플레이트를 줄입니다. |

### 패키지 구조

```text
com.example.coffeeordersystem
├── CoffeeOrderSystemApplication
├── common          (ApiException, ErrorCode, GlobalExceptionHandler)
├── event            (Kafka Consumer 멱등 처리 이력 ProcessedEvent)
├── menu             (메뉴 조회 API)
│   ├── controller / domain / dto / repository / service
├── order            (주문/결제, Kafka 발행, Outbox)
│   ├── controller / domain / dto / event / repository / service
├── point            (포인트 충전)
│   ├── controller / domain / dto / repository / service
├── ranking          (Redis 인기 메뉴 랭킹)
│   ├── consumer / rebuild / retention / service
└── recovery         (DLT 재발행)
```

기능별로 패키지를 먼저 나누고, 그 안에서 Controller-Service-Repository 3계층으로 구분합니다. 계층 경계 규칙은 [계층 설계 정책](docs/architecture/layered-design-policy.md)을 따릅니다.

## 핵심 정책

### 포인트

- 1원 = 1P이며 1회 충전 최대 1,000,000P, 잔액은 음수가 될 수 없습니다.
- 충전 요청 시 포인트 지갑이 없으면 새로 생성하고, 주문 시 지갑이 없으면 실패(404)합니다.
- 동시 충전은 DB 비관적 락(`SELECT ... FOR UPDATE`)으로 보호합니다.

### 주문/결제

- 사전 충전된 포인트로만 결제하며(외부 PG 연동 없음), 결제 금액은 DB의 메뉴 가격 기준으로 계산합니다.
- 주문 흐름: `lock:order:user:{userId}` Redisson 락 획득(`waitTime` 2초, `leaseTime` 5초) → DB 트랜잭션 시작 → `UserPoint` 비관적 락 조회 → 잔액 검증·차감 → 주문 저장 → `OutboxEvent` 저장 → 트랜잭션 커밋 → 락 해제.
- 락 획득 실패는 `409 ORDER_LOCK_NOT_ACQUIRED`를 반환합니다.

### 이벤트와 Kafka 발행 신뢰성(Outbox)

- 주문 성공 시 `OrderCompletedEvent`(eventId, orderId, userId, menuId, paidAmount, orderedAt)를 만들지만, 주문 트랜잭션은 Kafka를 직접 호출하지 않고 같은 트랜잭션 안에서 `OutboxEvent`(payload는 JSON)만 저장합니다.
- 별도 `OutboxEventPublisher`가 2초 주기로 미발행 이벤트를 폴링해 Kafka로 발행하고, 성공 시 발행 상태를 갱신합니다. Kafka가 일시적으로 불가능해도 주문 DB 커밋은 영향받지 않고, 다음 폴링에서 자동 재시도합니다.
- Kafka Consumer(`ranking-consumer-group`)는 호환 처리 이력인 `processed_event`와 랭킹 전용 상태 원장인 `ranking_event_ledger`를 함께 기록합니다. 반복 재시도 후 실패한 메시지는 `order.completed.DLT`로 이동합니다.
- DLT는 자동 전체 재발행하지 않습니다. 운영자가 topic·partition·offset으로 한 건을 선택하면 recovery lock과 pending rebuild 여부를 먼저 확인하고 원본 topic으로 재발행합니다.

### 인기 메뉴 랭킹

- Kafka Consumer가 성공 처리한 주문마다 Redis ZSET(`popular:menus:{yyyy-MM-dd}`)의 메뉴별 점수를 증가시킵니다. 이때 Lua가 `ranking:applied-event:{eventId}` marker 기록과 ZSET 증가를 원자적으로 처리합니다.
- 조회 시 최근 7일 날짜별 key를 `ZUNION`으로 합산하고, 점수 내림차순·메뉴 ID 오름차순으로 정렬해 Top 3를 반환합니다.
- `ranking_event_ledger`는 `RESERVED → REDIS_APPLIED → COMMITTED` 상태로 Redis 반영 전후 crash를 구분합니다. 같은 `eventId`의 fingerprint가 다르면 점수를 바꾸지 않고 fail-closed합니다.
- Redis는 원천 데이터가 아닌 파생 데이터이며, 유실 시 maintenance mode의 `ranking-rebuild-group`으로 Kafka topic을 replay해 temp ZSET을 검증한 뒤 교체합니다. DLT replay와 rebuild는 공통 recovery lock과 ledger를 사용해 어느 순서에서도 중복 집계를 막습니다.
- ledger cleanup은 기본 비활성화입니다. 활성화하면 30일이 지난 독립 또는 완료 rebuild의 `COMMITTED` 행만 한 번에 최대 100건씩 삭제하며 `RESERVED`, `REDIS_APPLIED`, 미완료 rebuild 행은 보존합니다. Kafka·DLT·최대 rebuild recovery window가 ledger 보존 기간보다 길거나 Redis marker의 실효 TTL이 짧으면 기동을 거부합니다.

## API 명세

Base path는 `/api`이며, 에러 응답만 공통 포맷을 사용합니다.

```json
{
  "code": "INSUFFICIENT_POINT",
  "message": "포인트 잔액이 부족합니다."
}
```

### GET /api/menus

```json
[
  { "id": 1, "name": "아메리카노", "price": 4500 }
]
```

### GET /api/menus/popular

```json
[
  { "rank": 1, "menuId": 1, "menuName": "아메리카노", "orderCount": 12 }
]
```

### POST /api/points/charge

```json
{ "userId": 1, "amount": 10000 }
```

응답: `{ "userId": 1, "balance": 10000 }`

### POST /api/orders

```json
{ "userId": 1, "menuId": 1 }
```

응답(`201 Created`): `{ "orderId": 100, "userId": 1, "menuId": 1, "menuName": "아메리카노", "paidAmount": 4500, "status": "PAID", "orderedAt": "..." }`

### 오류 코드

| 코드 | HTTP | 상황 |
| --- | --- | --- |
| `INVALID_CHARGE_AMOUNT` | 400 | 충전 요청 값이 유효하지 않음 |
| `MENU_NOT_FOUND` | 404 | 메뉴가 없음 |
| `USER_POINT_NOT_FOUND` | 404 | 사용자 포인트 지갑이 없음 |
| `INSUFFICIENT_POINT` | 409 | 잔액 부족 |
| `ORDER_LOCK_NOT_ACQUIRED` | 409 | 동시 주문으로 락 획득 실패 |
| `INTERNAL_ERROR` | 500 | 서버 오류 |

전체 명세는 [API 명세](docs/api/api-spec.md)를 참고합니다.

## 로컬 실행

```powershell
docker compose -f docker/compose.yaml --profile tools up -d   # MySQL 13306, Redis 16379, Kafka 19092
$env:SPRING_PROFILES_ACTIVE = 'local'
.\gradlew.bat bootRun
```

Kafka UI는 http://localhost:18080, RedisInsight는 http://localhost:15540 입니다. 상세 절차와 실제 확인 명령은 [로컬 실행 Runbook](docs/operations/local-runbook.md)을 참고합니다.

## 테스트와 검증

- 단위·MVC slice·MySQL/Kafka/Redis Testcontainers 테스트와 실제 Docker·HTTP 검증으로 동시 주문 락 경합, Kafka Consumer 중복 처리, DLT 이동, Outbox 재시도, ranking ledger 복구를 확인했습니다. 최종 실행 결과는 [Issue #114 evidence](docs/testing/evidence/issue-114/verification.md)에 고정합니다.
- CI(GitHub Actions `quality-gates`)가 모든 PR에서 하네스 테스트, 컴파일, 전체 Gradle 테스트, 링크 검사를 실행합니다.
- k6 Load·Stress·Spike 결과는 [k6 결과](docs/performance/k6-results.md), DB 인덱스·EXPLAIN 근거는 [인덱스 검증](docs/db/indexing-explain.md)을 참고합니다.

## 문서 진입점

- [요구사항](docs/product/requirements.md) · [범위](docs/product/scope.md)
- [도메인 규칙](docs/domain/domain-rules.md) · [아키텍처](docs/architecture/overview.md)
- [API 명세](docs/api/api-spec.md) · [최종 ERD](docs/db/erd.md)
- [ADR-001 계층형 구조](docs/adr/ADR-001-layered-architecture.md) · [ADR-002 Redisson과 DB 락](docs/adr/ADR-002-redisson-and-db-pessimistic-lock.md) · [ADR-003 Kafka](docs/adr/ADR-003-kafka-vs-rabbitmq-vs-db.md) · [ADR-004 Redis ZSET](docs/adr/ADR-004-redis-zset-ranking.md)
- [ADR-005 Kafka replay](docs/adr/ADR-005-kafka-replay-recovery.md) · [ADR-006 QueryDSL과 인덱스](docs/adr/ADR-006-querydsl-and-indexing.md) · [ADR-007 k6](docs/adr/ADR-007-k6-test-priority.md) · [ADR-008 랭킹 복구 ledger](docs/adr/ADR-008-ranking-recovery-ledger.md)
- [테스트 전략](docs/testing/test-strategy.md) · [로컬 실행 Runbook](docs/operations/local-runbook.md) · [Kafka·Redis Runbook](docs/operations/kafka-redis-runbook.md)
- [AI 작업 규칙](docs/ai/agent-rules.md)

## 프로젝트 트러블슈팅

- [커피 주문 시스템 트러블슈팅: 정합성·메시징·복구·테스트 인프라](https://app.notion.com/p/3a1b1fddfba981a4bc52fd5a88e7c2e5)

## 제출 확인

- 저장소는 [GitHub 공개 저장소](https://github.com/namdongyeob/coffee-order-system)입니다.
- [커밋 이력](https://github.com/namdongyeob/coffee-order-system/commits/main)은 제출 기준 10개 이상입니다.
- 필수 API 4종, Level 5·6 실제 실행과 k6 결과는 [Issue #114 evidence](docs/testing/evidence/issue-114/verification.md)에서 확인할 수 있습니다.

## AI 협업 방식

이 프로젝트는 GitHub Issue를 작업의 단일 출처로 사용하고, Issue마다 Execution mode(SOLO/STANDARD/STRICT)를 선언해 필요한 검증 수준을 정합니다. 위험도가 높은 변경(트랜잭션, 락, Kafka, Redis)은 Dev 구현 후 독립 Review·QA를 거쳐야 merge할 수 있습니다. AI 활용 규칙과 검증 evidence는 `docs/ai/`, `docs/testing/evidence/`에서 Issue별로 확인할 수 있습니다.

## 검증 규칙

완료 주장은 반드시 검증 레벨과 실제 명령 결과를 `docs/testing/evidence/issue-{number}/verification.md`에 남긴 뒤에만 합니다. 전역 뷰는 [Evidence Guide](docs/testing/evidence-guide.md)의 on-demand 명령으로 재현하고 생성 파일은 커밋하지 않습니다.
