# ADR-005 Kafka Replay 복구

## Issue #14 구현 결정

Kafka replay 후보를 maintenance 전용 runner로 채택했습니다. online rebuild 대신 일반 consumer의 활성 member가 없음을 확인하고, earliest replay·고정 end offset·DB 정확 비교·Redis temp/backup Lua 교체·성공 후 정상 group offset 이동을 하나의 fail-closed 절차로 묶습니다.

## 상태와 결정일

Challenge candidate. 결정일: 2026-07-09.

## 맥락과 문제

Redis 랭킹은 파생 데이터이므로 유실되어도 주문 원천 데이터는 남습니다. 다만 재구성 과정에서 일반 consumer와 rebuild가 같은 key를 함께 증가시키거나, DB 재집계와 replay를 동시에 실행하면 점수가 중복될 수 있습니다.

## 결정 동인

- 원천 주문을 훼손하지 않고 파생 랭킹을 재구성합니다.
- Kafka 로그·consumer group·offset 학습 목표를 활용합니다.
- 복구 중 중복 증가와 정상 소비 간 간섭을 통제합니다.

## 검토한 선택지

| 선택지 | 장애 동작 | 정합성 영향 | 검증 계획 | 운영 위험 | 판단 |
| --- | --- | --- | --- | --- | --- |
| `ranking-rebuild-group` Kafka replay | Redis 유실 후 별도 group으로 재처리하며 일반 소비를 중지 또는 격리합니다. | 최근 7일 이벤트만 한 번 반영해야 하며, 일반 consumer와 동시 실행하면 중복 증가합니다. | rebuild 전 key 백업·초기화, replay 후 API 결과와 DB 주문 수 비교를 계획합니다. | retention 부족, offset 시작점 오류, 일반 consumer 동시 실행. | 1순위 도전 후보. |
| DB 주문 원천 재집계 | Kafka 접근 불가 또는 retention 부족 시 DB에서 집계합니다. | 원천 기준 재생성은 가능하지만 replay와 동시 실행하면 중복됩니다. | 기간·menuId별 DB 집계와 Redis Top 3 비교가 필요합니다. | 대량 집계의 DB 부하와 운영 시간 증가. | 보조 후보. |
| Redis 백업 복원만 수행 | 백업이 없거나 오래되면 복구할 수 없습니다. | 백업 시점 이후 주문이 빠질 수 있습니다. | 백업 시점·복원 후 원천 주문 비교가 필요합니다. | 백업 운영 의존성과 최신성 불확실성. | 제외. 단독 정합성 근거가 될 수 없습니다. |

## 결정과 이유

Redis 랭킹 유실 복구는 Kafka replay 전략으로 문서화합니다. MVP 이후 `ranking-rebuild-group` 기반 rebuild runner를 도전 구현 후보로 두며, DB 재집계는 보조 복구 후보로 남깁니다.

## 결과와 단점

파생 데이터를 Kafka 로그로 다시 만들 수 있는 경로를 갖습니다. 반면 retention, offset, key 초기화와 consumer 격리를 운영해야 하고, 이 Issue에서 구현·자동화하지 않은 계획입니다.

## 검증 현황과 계획

- 실제 근거: 이 ADR 보강 Issue에서는 rebuild runner, topic retention, Redis key를 변경하거나 replay를 실행하지 않았습니다.
- 계획된 검증: Level 4에서 일반 랭킹 쓰기 격리, 대상 key 초기화, 최근 7일 event replay, API Top 3와 DB 원천 주문 수 비교, 중복 실행 방지 및 실패 후 재시도 절차를 관찰합니다.

## 재검토 조건

- `order.completed` retention이 7일보다 짧거나 복구 요구 기간이 늘어날 때.
- rebuild가 정상 consumer의 가용성을 침해하거나 DB 재집계가 더 적합한 근거가 생길 때.

## 관련 항목

- 관련 Issue: [#28](https://github.com/namdongyeob/coffee-order-system/issues/28).
- 설계: [복구 전략](../architecture/recovery-strategy.md), [Kafka 이벤트 흐름](../architecture/kafka-event-flow.md), [Redis 랭킹 설계](../architecture/redis-ranking.md).
- 대체·폐기 규칙: [ADR 운영 규칙](README.md)을 따릅니다.
