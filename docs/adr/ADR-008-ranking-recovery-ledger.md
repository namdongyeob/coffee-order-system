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

ledger와 Redis marker는 안전 보존 공식에 맞게 보존합니다. 구현은 DLT 보존 기간, Kafka retention, 최대 복구 window의 최댓값을 고려한 안전 보존 공식을 충족하지 못하면 배포를 거부하도록 검증합니다. 만료 삭제는 안전 보존 공식을 충족하는 만료 기간이 지난 `COMMITTED` 행만 대상으로 하며, `RESERVED`·`REDIS_APPLIED`·미완료 rebuild run의 행은 자동 삭제하지 않습니다.

## 경로별 조회와 기록 순서

### 정상 consumer와 DLT replay

DLT replay는 `processed_event`를 사전 조회해 성공으로 판단하지 않습니다. 승인된 DLT record는 원본 topic으로 재발행하고, normal consumer와 같은 `applyRankingEvent` 경로가 ledger를 처리합니다.

| 단계 | normal consumer / DLT 재발행 뒤 consumer |
| --- | --- |
| 1 | `event_id`와 fingerprint로 ledger를 insert-or-read 합니다. 존재하지 않으면 이번 호출에서 새 `RESERVED` 행을 생성(insert)합니다. |
| 2 | 다른 fingerprint면 즉시 fail-closed 합니다. `COMMITTED`면 점수 변경 없이 성공 ack 합니다. |
| 3 | 이번 호출에서 새로 생성된 `RESERVED` 행인 경우에만 최초 Redis Lua marker+increment 호출을 허용합니다. |
| 4 | 이미 존재하던 pending `RESERVED` 또는 `REDIS_APPLIED` 행에 대해 Redis marker가 만료/유실되어 없는 상태인 경우, `PENDING_MARKER_EXPIRED_RECONCILE` 예외를 발생시키고 fail-closed 처리합니다. |
| 5 | Lua 호출이 성공하여 applied 또는 same-fingerprint marker를 반환하면 ledger를 `REDIS_APPLIED` 후 `COMMITTED`로 전이합니다. |
| 6 | conflict, Redis 오류, DB 오류는 Kafka ack 전에 예외로 반환합니다. |

새로 생성된 `RESERVED`에 대한 Lua 호출 성공 후 RDBMS에 `REDIS_APPLIED`로 업데이트하기 전 crash한 경우, 재시도 시 기존 pending `RESERVED` 상태이나 marker가 일치하므로 Lua 호출을 생략(또는 marker 복원 검증)한 채 `COMMITTED` 완료 처리할 수 있습니다. 반면, 동일 상황에서 marker가 유실된 경우에는 적용 증명이 불가하므로 자동 처리를 거부하고 fail-closed 처리해야 합니다.

### rebuild

rebuild는 event별 live ZSET increment를 하지 않습니다. 고정 end offset까지 replay한 unique event 집합으로 temp ZSET을 만들고 DB 집계와 비교한 뒤 swap합니다. 따라서 rebuild 완료의 ledger 반영은 temp 작성 전이나 swap 전이 아니라 **성공한 temp ZSET swap 뒤**에만 시작합니다.

rebuild 시작은 normal consumer와 DLT replay가 공유하는 recovery lock을 얻고, 진행 중 또는 `SWAPPED_PENDING_LEDGER` 상태의 rebuild run이 있으면 둘 다 실행을 거부해야 합니다. DLT replay는 이 lock을 얻지 못하면 재발행하지 않고 retryable failure로 남깁니다.

성공 swap 뒤 captured replay event마다 `source=REBUILD`, `rebuild_run_id`, fingerprint로 bulk insert-or-verify를 수행합니다. 이미 같은 fingerprint의 `COMMITTED` 행은 유지하고, 다른 fingerprint는 fail-closed 합니다. 모든 행이 `COMMITTED`가 될 때까지 lock을 해제하지 않습니다.

## crash와 재실행 상태 전이

| crash 또는 경쟁 지점 | 남는 상태 | 다음 실행의 동작 | 점수 결과 |
| --- | --- | --- | --- |
| reservation 전 | ledger·marker 없음 | 새 `RESERVED`를 생성하고 적용 (최초 Lua 허용) | 최대 1회 |
| `RESERVED` 뒤 Redis Lua 전 | `RESERVED`, marker 없음 | `PENDING_MARKER_EXPIRED_RECONCILE` (Fail-Closed) 처리 후 수동 복구 | 0회 (수동 Rebuild 후 1회 완료) |
| Redis Lua 뒤 DB state 갱신 전 | `RESERVED`, same-fingerprint marker 있음 | Lua no-op 뒤 `COMMITTED` 완료 | 이미 1회 |
| `COMMITTED` 뒤 duplicate normal/DLT | `COMMITTED` | score 변경 없이 ack 또는 replay skip | 추가 0회 |
| rebuild temp 작성·DB 비교 전 실패 | live ZSET·ledger 불변 | temp 삭제, 새 run으로 처음부터 replay | 0회 또는 기존 live 유지 |
| rebuild swap 성공, ledger bulk 전 crash | rebuild run `SWAPPED_PENDING_LEDGER`, live ZSET 교체됨 | recovery가 같은 captured event 집합으로 ledger bulk를 재개, normal/DLT는 lock 때문에 거부 | swap에 포함된 값 1회 |
| rebuild ledger bulk 일부 완료 | 일부 `COMMITTED`, pending run | 같은 fingerprint는 유지하고 누락 행만 완료 | swap 값 추가 증가 없음 |
| DLT → rebuild | DLT가 먼저 `COMMITTED`일 수 있음 | rebuild는 absolute temp 집계를 만들고 swap 뒤 같은 ledger 행을 verify | live 점수는 rebuild snapshot 기준 1회 |
| rebuild → DLT | rebuild ledger가 `COMMITTED` | DLT 재발행은 consumer에서 no-op | 추가 0회 |
| normal/DLT와 rebuild 동시 요청 | recovery lock 경합 | 하나만 실행, 다른 쪽은 retryable fail-closed | 최대 1회 |

rebuild swap 뒤 ledger backfill을 실패한 채 lock을 해제하는 것은 금지합니다. 이 상태에서 “일단 DLT를 재발행”하거나 ledger를 만료 처리하면 불변조건을 잃습니다.

## Redis marker 만료 복구 계약

DB ledger 상태가 아직 `COMMITTED`로 전이되지 못하고 `RESERVED` 또는 `REDIS_APPLIED`인 상태에서 Redis의 marker가 만료/유실되는 예외 상황에 대한 세부 복구 계약을 정의합니다.

### 1. 상태별 허용 동작 조합 및 흐름 계약

이전에 처리 이력이 없는 신규 유입 이벤트와, DB에 이미 등록되어 있으나 완료되지 못한 채 재유입된 기존 pending 이벤트를 명확히 구분하여 처리 흐름을 일원화합니다.

| DB Ledger 상태 | Redis Marker 상태 | 허용 동작 (Action) | 상세 조건 및 설명 |
| --- | --- | --- | --- |
| 새로 생성됨 (DB 행 없음 -> `RESERVED` 신규 추가) | 만료/없음 | Lua 호출 -> 적용 성공 시 `REDIS_APPLIED` 후 `COMMITTED` | 최초 유입 이벤트이므로 마커가 없는 것이 정상. Lua 실행 및 랭킹 점수 1회 안전 반영 |
| 기존 pending (`RESERVED`) | 존재 (일치) | Lua 호출 -> 적용 성공 시 `REDIS_APPLIED` 후 `COMMITTED` | 이전 DB 트랜잭션 실패로 재시도. 마커가 존재하므로 Lua 재처리로 안전 반영 |
| 기존 pending (`RESERVED`) | 존재 (불일치) | `EVENT_ID_PAYLOAD_CONFLICT` (Fail-Closed) | 동일한 eventId에 다른 payload를 가진 악의적/오염된 이벤트로 처리 차단 |
| 기존 pending (`RESERVED`) | 만료/없음 | `PENDING_MARKER_EXPIRED_RECONCILE` (Fail-Closed) | 마커 유실로 Redis 반영 여부 증명 불가. 중복 집계 방지를 위해 fail-closed 및 알람 |
| 기존 pending (`REDIS_APPLIED`) | 존재 (일치) | Lua 호출 생략 (no-op) -> `COMMITTED` 전이 | Redis에 이미 적용되었음이 확실하므로 DB 상태만 완료 처리함 |
| 기존 pending (`REDIS_APPLIED`) | 존재 (불일치) | `EVENT_ID_PAYLOAD_CONFLICT` (Fail-Closed) | 동일 eventId payload 불일치 차단 |
| 기존 pending (`REDIS_APPLIED`) | 만료/없음 | `PENDING_MARKER_EXPIRED_RECONCILE` (Fail-Closed) | 이미 적용 단계 진입 후 크래시가 났으나 마커가 만료됨. 반영 증명 불가로 fail-closed 및 알람 |
| `COMMITTED` | 존재 (일치) | 성공 Ack (no-op) | 이미 처리가 완료된 중복 이벤트 |
| `COMMITTED` | 존재 (불일치) | `EVENT_ID_PAYLOAD_CONFLICT` (Fail-Closed) | 동일 eventId payload 불일치 차단 |
| `COMMITTED` | 만료/없음 | 성공 Ack (no-op) | 이미 완료된 이벤트이며, 마커 만료 여부와 무관하게 성공 ack 처리 |

### 2. 안전 여유(Safe Margin) 및 임계값 정의

- **계산 공식**:
  $$\text{redisMarkerTtl} > \text{kafkaRetention} + \text{dltRetention} + \text{maximumRebuildRecoveryWindow} + \text{retryHorizon} + \text{segmentDelayMargin}$$
  - `redisMarkerTtl`: Redis 마커의 TTL
  - `kafkaRetention`: Kafka 원본 토픽의 최대 보존 주기
  - `dltRetention`: DLT 토픽의 최대 보존 주기
  - `maximumRebuildRecoveryWindow`: 재빌드 진행 시 커버 가능한 복구 윈도우 최대 크기
  - `retryHorizon`: 컨슈머 그룹의 백오프 및 재시도에 따른 최대 지연 한계 시간
  - `segmentDelayMargin`: Kafka 브로커의 세그먼트 롤링(Segment Roll), 클린업 주기(Check), 물리적 디스크 삭제(Delete) 지연 마진
- **동일 설정값 금지**: 마커 TTL이 Kafka/DLT 보존 주기와 완전히 동일하게 설정되는 것은 마커의 선만료에 의한 중복 집계 위험이 있으므로 엄격히 금지합니다.
- **기동 조건 검증 (Application Startup Check)**: 애플리케이션 시작 시 `RankingLedgerRetentionProperties` 바인딩 검증 단계에서 위의 계산 공식을 실제로 평가하여, `redisMarkerTtl`이 안전 임계값 합계를 넘지 못하면 즉시 `IllegalStateException`을 발생시키고 구동을 거부(fail-fast)합니다.
- **DB Ledger Cleanup**: 안전 보존 공식을 충족하는 만료 기간이 지난 `COMMITTED` 상태의 행만 삭제합니다. `RESERVED` 또는 `REDIS_APPLIED` 상태의 미완료 행은 영구 보존하여 추적 가능하게 합니다.

### 3. 마커 만료 후 동일 fingerprint pending event 유입 시 처리 및 예외 격리
- **결정**: **Fail-Closed 및 예외 발생**
- **오류 코드 명확화**: `PENDING_MARKER_EXPIRED_RECONCILE`은 DB ledger의 물리적인 `state`가 아니며, Consumer가 랭킹 이벤트 처리 시 마커 유실을 감지했을 때 던지는 **예외 오류 코드(Error Code / Exception Reason)**입니다. 이 오류 발생 시에도 DB ledger 상의 행은 기존 pending 상태(`RESERVED` 혹은 `REDIS_APPLIED`)와 증거를 그대로 유지하여 복구를 위한 추적 용도로 보존합니다.

### 4. 마커 만료 미결 이벤트(`PENDING_MARKER_EXPIRED_RECONCILE`) 상태의 운영자 복구 절차

1. 운영자는 복구 조치를 진행하기 전에 시스템 영향도를 방지하기 위해 **maintenance window(점검창)에 진입**합니다.
2. 현재 실행 중인 랭킹 컨슈머들과의 경합을 완벽하게 피하기 위해, Kafka cluster에서 **`ranking-consumer-group` 의 active member 수가 0(즉, 모든 컨슈머 인스턴스가 안전하게 중지됨)임을 확인**합니다.
3. 컨슈머 비활성화가 확인되면, 동일 기간 동안 다른 자동화 복구 도구나 DLT 재발행이 실행되지 않도록 **공유 복구 락(`ranking:rebuild:lock`)을 획득**합니다.
4. Redis ZSET 랭킹 점수판에 ZINCRBY 수동 증량 등의 ad-hoc 쓰기를 직접 실행하는 방식을 금지합니다. ZSET은 실시간성이 결합된 캐시성 ZSET이므로 수동 직접 수정은 정합성 불일치를 가중시킵니다.
5. RDBMS `orders` 테이블과 Redis ZSET의 데이터를 대조하고 검증합니다.
6. 메뉴 랭킹 롤링 윈도우 집계를 완전 재빌드(`Ranking Rebuild`)하는 절차를 실행하여 Redis ZSET을 올바른 데이터로 동기화 및 갱신합니다.
7. **Rebuild와 DB 대조가 모두 성공적으로 완료(성공)된 뒤에만**, pending 상태의 ledger 행을 `COMMITTED` 상태로 수동 업데이트하여 종료합니다.
8. **Rebuild 단계에 따른 실패 처리 (Pre-Swap vs Post-Swap)**:
   - **Pre-Swap 단계 실패 (재빌드 또는 DB 대조 과정 실패)**: Redis ZSET swap이 일어나지 않았으므로 기존 실시간 집계판이 유지됩니다. 이 경우 pending ledger 행은 기존 pending 상태(`RESERVED`/`REDIS_APPLIED`)와 증거를 그대로 유지하며, 복구 락을 즉시 해제하고 점검창을 종료해도 무방합니다.
   - **Post-Swap 단계 실패 (재빌드 swap 성공 후 Ledger Backfill/업데이트 실패)**: Redis ZSET은 이미 재빌드된 데이터로 교체되었으나, RDBMS ledger 행들을 `COMMITTED`로 업데이트하는 벌크 백필이 미완료된 상태입니다. 이 상태에서 컨슈머가 조기 가동되거나 DLT가 재발행되면 exactly-once가 파괴되므로, **반드시 모든 ledger 백필이 성공하여 안전한 terminal state에 도달하기 전까지 복구 락 및 점검 상태를 강제로 유지**하고 lock 해제를 차단해야 합니다.
9. **finally 방식의 안전 릴리즈 적용 기준**:
   - **Pre-Swap 단계 실패 시**: finally 블록을 통해 복구 락(`ranking:rebuild:lock`)을 즉시 안전하게 해제하고 점검창을 종료한 뒤 컨슈머 그룹을 재기동합니다.
   - **Post-Swap 단계 미완료/실패 시**: finally 블록의 자동 락 해제 대상에서 제외하거나, 백필 트랜잭션이 최종 완료(terminal state)된 이후에만 락이 해제되도록 제어하여 안전성을 보장합니다.


### 5. 보존, 정리 및 관측 지표 계약
- **보존**: `COMMITTED`가 아닌 미결 행은 자동 정리 대상에서 영구 제외합니다.
- **관측 지표 (Metrics)**:
  - `ranking_ledger_stale_reserved_count`: 24시간 이상 `RESERVED` 상태인 미결 행 수.
  - `ranking_ledger_stale_applied_count`: 24시간 이상 `REDIS_APPLIED` 상태인 미결 행 수.
  - 위 지표가 0보다 크면 알람(Alert)을 발생시켜 운영자가 인지하도록 합니다.

## 구현 이슈의 acceptance criteria

이 설계에 기반한 후속 구현 작업(이슈 #134)은 다음 대상 파일의 수정과 테스트 명세를 통해 완수합니다.

### 1. 수정 대상 파일
- [PopularMenuRankingService.java](../../src/main/java/com/example/coffeeordersystem/ranking/service/PopularMenuRankingService.java) (marker 조회, fingerprint 비교, Lua 증가 스크립트 실행의 실질적인 호출 주체 및 충돌 주의 제어 구현)
- [RankingEventProcessor.java](../../src/main/java/com/example/coffeeordersystem/ranking/consumer/RankingEventProcessor.java) (공통 예외 격리, Fencing 제어 및 `PENDING_MARKER_EXPIRED_RECONCILE` 예외 발생 경계 구현)
- [RankingEventLedger.java](../../src/main/java/com/example/coffeeordersystem/ranking/consumer/RankingEventLedger.java) (신규 insert `RESERVED`와 기존 pending `RESERVED` 구분 및 수동 복구 연동 인터페이스 구현)
- [RankingLedgerRetentionProperties.java](../../src/main/java/com/example/coffeeordersystem/ranking/retention/RankingLedgerRetentionProperties.java) (보존 기간 공식 기동 검증 규칙 실제 평가 구현)


### 2. #134 필수 검증 및 테스트 명세
- **신규 RESERVED + marker 없음**: 처음 인서트된 행이 마커가 없는 상태로 유입 시, Lua 스크립트를 즉시 호출하여 마커를 기록하고 ZSET 점수를 안전하게 반영하는지 검증합니다.
- **만료된 RESERVED + marker 없음**: 이미 `RESERVED`로 DB에 등록되어 있던 pending 행이 유입되었으나 Redis 마커가 유실/만료된 경우, 자동 처리하지 않고 `PENDING_MARKER_EXPIRED_RECONCILE` 예외를 발생시키며 fail-closed 되는지 검증합니다.
- **REDIS_APPLIED + marker 없음**: DB 상태가 `REDIS_APPLIED`이나 Redis 마커가 없는 경우, `PENDING_MARKER_EXPIRED_RECONCILE` 예외를 던지고 fail-closed 처리되는지 검증합니다.
- **marker 만료 후 DLT replay**: DLT 재발행 시점에 마커 만료 조건에 걸리는 경우, 중복 집계를 방지하기 위해 처리가 격리 및 거부되는 흐름을 검증합니다.
- **불안전한 TTL 설정의 기동 거부**: $\text{redisMarkerTtl} \le \text{kafkaRetention} + \text{dltRetention} + \text{maximumRebuildRecoveryWindow} + \text{retryHorizon} + \text{segmentDelayMargin}$ 조건으로 설정을 주입했을 때, 컨텍스트 초기화 단계에서 구동을 차단하고 `IllegalStateException`을 던지는지 검증합니다. (검증 대상 테스트 파일: [RankingLedgerRetentionConfigurationTest.java](../../src/test/java/com/example/coffeeordersystem/ranking/retention/RankingLedgerRetentionConfigurationTest.java))
- **DB Crash Injection Test (트랜잭션 분리 및 Crash 경계 명시)**:
  - **트랜잭션 경계 설계**: 이벤트 처리 전체를 감싸는 단일 `@Transactional` 내에서는 `REDIS_APPLIED` 상태로의 RDBMS 전이가 최종 커밋 전까지 DB에 반영(durable)되지 않습니다. 따라서 `REDIS_APPLIED` 상태 전이(RDBMS 업데이트)는 반드시 독립적인 별도 커밋/트랜잭션 전파 속성(예: `Propagation.REQUIRES_NEW`)을 적용하여 비즈니스 로직(메인 DB 커밋) 수행 전에 먼저 DB에 물리적으로 저장되어야 합니다.
  - **Crash 주입 및 검증**: Redis Lua 스크립트 실행(마커 기록 및 ZSET 점수 반영)이 성공하고 `REDIS_APPLIED` 상태로의 DB 커밋까지 완전히 완료된 직후, 메인 비즈니스 DB 트랜잭션의 최종 커밋 직전에 강제로 애플리케이션/컨테이너 Crash를 주입(Mocking 또는 Testcontainers 제어)하여 `REDIS_APPLIED` 상태로 방치되는 시나리오를 구성합니다.
  - **Exactly-Once 검증**: 이후 애플리케이션 재기동 시 해당 eventId가 재유입될 때 중복 점수 증가 없이 정상적으로 `COMMITTED`로 완료 전이되고 exactly-once 집계가 보장되는지 검증해야 합니다.


- **retention 검증**: retention job은 만료 기간이 지난 `COMMITTED` 행과 marker만 삭제하며 pending 행을 삭제하지 않음을 검증합니다.

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
