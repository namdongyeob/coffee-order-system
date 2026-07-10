# ADR-003 Kafka와 RabbitMQ와 DB 비교

## 상태와 결정일

Accepted. 결정일: 2026-07-09.

## 맥락과 문제

주문 완료 뒤 랭킹용 파생 데이터를 갱신해야 합니다. 주문 원천 데이터의 트랜잭션과 비동기 갱신을 분리하면서도, 실패한 소비를 추적·복구할 수 있어야 합니다. 현재 이벤트 계약과 DLT 흐름은 [Kafka 이벤트 흐름](../architecture/kafka-event-flow.md)에 정의합니다.

## 결정 동인

- 주문 완료 이벤트를 consumer group으로 독립 소비합니다.
- replay와 DLT를 과제의 검증·설명 대상으로 남깁니다.
- 소비 실패가 주문 원천 데이터 손실로 오해되지 않게 합니다.

## 검토한 선택지

| 선택지 | 장애 동작 | 정합성 영향 | 검증 계획 | 운영 위험 | 판단 |
| --- | --- | --- | --- | --- | --- |
| Kafka | 소비 실패는 `FixedBackOff(1000L, 2L)` 뒤 DLT로 이동합니다. | 주문 DB와 Redis 랭킹은 비동기적이라 일시적 지연이 가능하며, consumer 멱등성이 필요합니다. | Level 4에서 producer, ranking consumer, 재시도, DLT 이동, 중복 event 처리 관찰을 계획합니다. | topic retention, consumer lag, DLT 누적과 중복 소비 관리. | 채택. replay·consumer group·DLT 학습 목표와 맞습니다. |
| RabbitMQ | 브로커 ack/nack와 재시도 정책에 따라 재전달 또는 DLQ 처리가 필요합니다. | 랭킹 비동기성은 같고 중복 전달 방어가 필요합니다. | ack, 재전달, DLQ, 멱등 처리 검증이 필요합니다. | 현재 과제의 replay·offset 설명을 별도 설계해야 합니다. | 제외. 현 과제 근거상 Kafka보다 추가 결정이 많습니다. |
| DB polling 또는 동기 DB 갱신 | polling 실패·지연은 별도 재처리 설계가 필요합니다. | 동기 갱신이면 주문 지연이 늘고, polling이면 랭킹 반영 지연이 생깁니다. | polling 누락·중복, 동기 실패 rollback 경계를 검증해야 합니다. | 조회 부하와 재처리 상태 관리가 DB에 집중됩니다. | 제외. 이벤트 replay·DLT 요구를 충족하지 못합니다. |

## 결정과 이유

주문 완료 이벤트 전송에는 Kafka를 사용합니다. Kafka의 consumer group, offset, replay 및 DLT 개념이 주문 완료 이벤트와 Redis 랭킹의 비동기 파생 흐름을 설명하고 검증하는 데 적합합니다.

## 결과와 단점

주문 처리와 랭킹 갱신을 분리하고 실패 메시지를 DLT로 격리할 수 있습니다. 반면 주문 성공 직후 랭킹이 즉시 반영된다는 보장은 없으며, eventId 기반 멱등 처리와 topic·consumer 운영이 필요합니다.

## 검증 현황과 계획

- 실제 근거: 이 ADR 보강 Issue에서는 Kafka 설정·계약·소비 동작을 변경하거나 실행하지 않았습니다.
- 계획된 검증: Level 4에서 성공 이벤트의 ZSET 반영, 재시도 2회 뒤 DLT 이동, 같은 `eventId` 중복 소비의 단일 반영, consumer lag·DLT 관찰을 검증합니다.

## 재검토 조건

- 이벤트 계약, 보존 기간 또는 DLT 재처리 정책이 바뀔 때.
- 메시지 지연·운영 복잡도가 과제의 요구를 넘을 때.

## 관련 항목

- 관련 Issue: [#28](https://github.com/namdongyeob/coffee-order-system/issues/28).
- 설계: [Kafka 이벤트 흐름](../architecture/kafka-event-flow.md), [복구 전략](../architecture/recovery-strategy.md).
- 대체·폐기 규칙: [ADR 운영 규칙](README.md)을 따릅니다.
