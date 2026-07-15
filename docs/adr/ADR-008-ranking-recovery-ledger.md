# ADR-008 Ranking rebuild와 DLT replay의 공통 적용 ledger

## 상태와 결정일

Accepted. 결정일: 2026-07-15.

## 맥락과 문제

`order.completed`는 일반 ranking consumer, maintenance rebuild, DLT replay를 통해 다시 인기 메뉴 점수에 도달할 수 있습니다. 같은 `eventId`가 이 경로들을 어떤 순서로 지나도 점수는 최대 한 번만 반영되어야 합니다.

현재 `processed_event`는 일반 `ranking-consumer-group`이 처리한 이력입니다. `event_id`만 unique이고 Redis 점수 반영 전 DB에 저장되며, [복구 전략](../architecture/recovery-strategy.md)은 rebuild가 이 이력을 사용하지 않는다고 명시합니다. 따라서 이 테이블을 rebuild와 DLT replay의 공통 완료 ledger로 재사용하면 Redis 반영 전후의 crash를 구분하지 못하고, rebuild가 이미 포함한 이벤트를 DLT가 다시 반영하는 것도 막을 수 없습니다.

이 ADR은 구현하지 않습니다. 기존 Kafka 흐름과 rebuild 안전 계약은 [Kafka 이벤트 흐름](../architecture/kafka-event-flow.md), [ADR-003](ADR-003-kafka-vs-rabbitmq-vs-db.md), [ADR-005](ADR-005-kafka-replay-recovery.md)를 따릅니다.

## 불변조건

`eventId` 하나는 어떤 `DLT → rebuild`, `rebuild → DLT`, 정상 consumer와의 동시 도착 순서에서도 인기 메뉴 live ZSET 점수에 **최대 한 번만** 반영됩니다.

payload가 같은 `eventId`와 다르면 어느 경로도 점수를 반영하지 않고 fail-closed 합니다. retry header, Kafka partition·offset, DLT header는 payload 동등성에 포함하지 않습니다.

## 검토한 선택지

| 선택지 | 장점 | 치명적인 한계 | 판단 |
| --- | --- | --- | --- |
| 기존 `processed_event` 재사용 | migration이 적고 normal consumer와 가까움 | eventId 단독 unique라 projection·경로·payload fingerprint·rebuild swap 상태를 표현하지 못합니다. DB 저장과 Redis 증가 사이 crash도 판정할 수 없습니다. | 제외 |
| DB ledger만 별도 생성 | 복구 이력과 retention을 영속 보관할 수 있습니다. | DB 완료 행만으로는 Redis 증가 직후 DB 갱신 실패를 구별할 수 없어 재시도 시 이중 증가 또는 누락이 생깁니다. | 제외 |
| 별도 DB ledger와 Redis 원자 marker | DB는 장기 복구 상태를, Redis Lua는 점수 증가와 event marker의 원자성을 맡습니다. crash 후에도 marker로 실제 반영 여부를 재결정할 수 있습니다. | migration, Lua script, pending-run recovery 구현이 필요합니다. | 채택 |

## 결정

`processed_event`는 기존 normal consumer의 호환 이력으로 유지하고, popularity ranking 전용 `ranking_event_ledger`를 새로 도입합니다. 이 ledger는 정상 consumer와 DLT replay의 공통 적용 경로, 그리고 rebuild가 swap에 포함한 eventId의 완료 기준으로 사용합니다.

DB ledger만으로 Redis 증분의 exactly-once를 주장하지 않습니다. 실제 점수 반영은 Redis Lua가 다음을 한 번에 수행해야 합니다.

1. `ranking:applied-event:{eventId}` marker가 없으면 payload fingerprint를 marker에 기록하고 해당 날짜·menu ZSET을 1 증가합니다.
2. marker가 같은 fingerprint면 ZSET을 증가하지 않고 이미 반영됨을 반환합니다.
3. marker가 다른 fingerprint면 점수를 바꾸지 않고 conflict를 반환합니다.

marker TTL은 DB ledger retention보다 짧아지면 안 됩니다. DB ledger는 복구의 영속 정본이고 Redis marker는 점수 증가를 원자화하는 실행 보조 상태입니다.

## ledger 계약

### key와 필드

테이블 이름은 `ranking_event_ledger`입니다. 이 테이블은 popularity ranking projection 하나만 담당하므로 `event_id`를 primary key로 사용합니다.

| 필드 | 계약 |
| --- | --- |
| `event_id` | UUID 문자열. primary key이자 unique 제약입니다. |
| `event_type` | 현재 `order.completed`만 허용합니다. |
| `payload_fingerprint` | `orderId`, `userId`, `menuId`, `paidAmount`, `orderedAt`를 canonical serialization한 SHA-256입니다. #110의 core payload 비교와 같은 필드입니다. |
| `state` | `RESERVED`, `REDIS_APPLIED`, `COMMITTED` 중 하나입니다. |
| `source` | 최초 예약 경로 `NORMAL_CONSUMER`, `DLT_REPLAY`, `REBUILD`입니다. 이후 retry는 source를 덮어쓰지 않습니다. |
| `rebuild_run_id` | rebuild가 bulk 완료시킨 행에만 채웁니다. normal/DLT 단건 적용은 null입니다. |
| `reserved_at`, `redis_applied_at`, `committed_at` | 상태 전이 감사와 stale pending recovery에 사용합니다. |

`event_id` 재시도에서 fingerprint가 같으면 기존 상태를 이어서 처리합니다. fingerprint가 다르면 `EVENT_ID_PAYLOAD_CONFLICT`로 fail-closed 하고 Redis·ledger 상태를 변경하지 않습니다.

### 보존

ledger와 Redis marker는 `30일` 보존합니다. 구현은 DLT 보존 기간, Kafka retention, 최대 복구 window의 최댓값이 30일을 넘으면 배포를 거부하거나 retention을 함께 늘려야 합니다. 만료 삭제는 `COMMITTED` 행만 대상으로 하며, `RESERVED`·`REDIS_APPLIED`·미완료 rebuild run의 행은 자동 삭제하지 않습니다.

## 경로별 조회와 기록 순서

### 정상 consumer와 DLT replay

DLT replay는 `processed_event`를 사전 조회해 성공으로 판단하지 않습니다. 승인된 DLT record는 원본 topic으로 재발행하고, normal consumer와 같은 `applyRankingEvent` 경로가 ledger를 처리합니다.

| 단계 | normal consumer / DLT 재발행 뒤 consumer |
| --- | --- |
| 1 | `event_id`와 fingerprint로 ledger를 insert-or-read 합니다. 없으면 `RESERVED`를 만듭니다. |
| 2 | 다른 fingerprint면 즉시 fail-closed 합니다. `COMMITTED`면 점수 변경 없이 성공 ack 합니다. |
| 3 | `RESERVED` 또는 `REDIS_APPLIED`면 Redis Lua marker+increment를 호출합니다. |
| 4 | Lua가 applied 또는 same-fingerprint marker를 반환하면 ledger를 `REDIS_APPLIED` 후 `COMMITTED`로 전이합니다. |
| 5 | conflict, Redis 오류, DB 오류는 Kafka ack 전에 예외로 반환합니다. 재시도는 같은 ledger 행과 marker를 재사용합니다. |

`RESERVED`를 만든 뒤 crash하면 재시도는 marker를 보고 아직 증가하지 않았으면 증가합니다. Redis 증가 뒤 DB 전이가 실패하면 재시도는 same-fingerprint marker를 받아 증가 없이 `COMMITTED`를 완료합니다.

### rebuild

rebuild는 event별 live ZSET increment를 하지 않습니다. 고정 end offset까지 replay한 unique event 집합으로 temp ZSET을 만들고 DB 집계와 비교한 뒤 swap합니다. 따라서 rebuild 완료의 ledger 반영은 temp 작성 전이나 swap 전이 아니라 **성공한 temp ZSET swap 뒤**에만 시작합니다.

rebuild 시작은 normal consumer와 DLT replay가 공유하는 recovery lock을 얻고, 진행 중 또는 `SWAPPED_PENDING_LEDGER` 상태의 rebuild run이 있으면 둘 다 실행을 거부해야 합니다. DLT replay는 이 lock을 얻지 못하면 재발행하지 않고 retryable failure로 남깁니다.

성공 swap 뒤 captured replay event마다 `source=REBUILD`, `rebuild_run_id`, fingerprint로 bulk insert-or-verify를 수행합니다. 이미 같은 fingerprint의 `COMMITTED` 행은 유지하고, 다른 fingerprint는 fail-closed 합니다. 모든 행이 `COMMITTED`가 될 때까지 lock을 해제하지 않습니다.

## crash와 재실행 상태 전이

| crash 또는 경쟁 지점 | 남는 상태 | 다음 실행의 동작 | 점수 결과 |
| --- | --- | --- | --- |
| reservation 전 | ledger·marker 없음 | 새 `RESERVED`를 만들고 적용 | 최대 1회 |
| `RESERVED` 뒤 Redis Lua 전 | `RESERVED`, marker 없음 | Lua를 최초 실행 | 최대 1회 |
| Redis Lua 뒤 DB state 갱신 전 | `RESERVED`, same-fingerprint marker 있음 | Lua no-op 뒤 `COMMITTED` 완료 | 이미 1회 |
| `COMMITTED` 뒤 duplicate normal/DLT | `COMMITTED` | score 변경 없이 ack 또는 replay skip | 추가 0회 |
| rebuild temp 작성·DB 비교 전 실패 | live ZSET·ledger 불변 | temp 삭제, 새 run으로 처음부터 replay | 0회 또는 기존 live 유지 |
| rebuild swap 성공, ledger bulk 전 crash | rebuild run `SWAPPED_PENDING_LEDGER`, live ZSET 교체됨 | recovery가 같은 captured event 집합으로 ledger bulk를 재개, normal/DLT는 lock 때문에 거부 | swap에 포함된 값 1회 |
| rebuild ledger bulk 일부 완료 | 일부 `COMMITTED`, pending run | 같은 fingerprint는 유지하고 누락 행만 완료 | swap 값 추가 증가 없음 |
| DLT → rebuild | DLT가 먼저 `COMMITTED`일 수 있음 | rebuild는 absolute temp 집계를 만들고 swap 뒤 같은 ledger 행을 verify | live 점수는 rebuild snapshot 기준 1회 |
| rebuild → DLT | rebuild ledger가 `COMMITTED` | DLT 재발행은 consumer에서 no-op | 추가 0회 |
| normal/DLT와 rebuild 동시 요청 | recovery lock 경합 | 하나만 실행, 다른 쪽은 retryable fail-closed | 최대 1회 |

rebuild swap 뒤 ledger backfill을 실패한 채 lock을 해제하는 것은 금지합니다. 이 상태에서 “일단 DLT를 재발행”하거나 ledger를 만료 처리하면 불변조건을 잃습니다.

## 구현 이슈의 acceptance criteria

후속 구현은 이 ADR을 다음 작업 단위로 나눕니다.

1. migration으로 `ranking_event_ledger`와 primary key, state·fingerprint·run 필드를 추가하고 `processed_event`를 삭제하거나 재의미화하지 않습니다.
2. normal consumer와 DLT replay가 공통 `applyRankingEvent` 경로를 사용하고, same eventId/same fingerprint는 no-op, different fingerprint는 fail-closed임을 통합 테스트로 증명합니다.
3. Redis Lua가 marker 기록과 ZSET 증가를 원자적으로 수행하고, Redis 뒤 DB crash 재시도에서 점수가 두 번 증가하지 않음을 검증합니다.
4. rebuild run 상태와 shared recovery lock을 도입하고, swap 뒤 ledger bulk 완료 전 crash는 `SWAPPED_PENDING_LEDGER` recovery만 허용함을 검증합니다.
5. `DLT → rebuild`, `rebuild → DLT`, concurrent normal/DLT/rebuild 순서별로 같은 eventId 점수가 최대 한 번임을 Kafka·Redis·DB 통합 테스트로 검증합니다.
6. retention job은 30일 지난 `COMMITTED` 행과 marker만 삭제하며 pending 행을 삭제하지 않음을 검증합니다.

## 결과와 단점

새 DB table, Redis marker와 Lua, rebuild run recovery가 필요합니다. 대신 기존 `processed_event`의 normal consumer 호환 의미를 보존하면서, Redis와 DB가 원자적으로 함께 commit될 수 없는 crash 구간을 명시적으로 복구할 수 있습니다.

## 검증 현황과 계획

- 실제 근거. 이 Issue는 ADR 문서만 추가했습니다. Java, migration, Kafka·Redis·DLT runtime 테스트는 실행하지 않았습니다.
- 계획된 검증. 후속 구현 Issue에서 위 acceptance criteria의 Testcontainers 통합 테스트와 crash injection을 실행합니다.

## 관련 항목

- 관련 Issue. [#111](https://github.com/namdongyeob/coffee-order-system/issues/111), [#110](https://github.com/namdongyeob/coffee-order-system/issues/110).
- 기존 결정. [ADR-003 Kafka와 RabbitMQ와 DB 비교](ADR-003-kafka-vs-rabbitmq-vs-db.md), [ADR-005 Kafka Replay 복구](ADR-005-kafka-replay-recovery.md).
- 설계. [Kafka 이벤트 흐름](../architecture/kafka-event-flow.md), [복구 전략](../architecture/recovery-strategy.md).
- 운영 규칙. [ADR 운영 규칙](README.md).
