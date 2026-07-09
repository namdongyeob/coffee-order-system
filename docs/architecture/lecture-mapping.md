# 강의 개념 매핑

강의에서 배운 개념을 과제 구현에 어떻게 연결할지 정리합니다.

| 강의 개념 | 프로젝트 적용 |
| --- | --- |
| Spring MVC | 메뉴, 포인트, 주문, 인기 메뉴 API Controller 구현. |
| Validation | 요청 DTO의 `userId`, `menuId`, `amount` 검증. |
| JPA Transaction | 주문 생성과 포인트 차감을 하나의 트랜잭션으로 처리. |
| Lock | 포인트 차감 정합성 보장을 위해 DB 비관적 락 사용. |
| Redis | 최근 7일 인기 메뉴 Top 3 조회용 ZSET 사용. |
| Redisson | 같은 사용자 주문/결제 동시 진입 제어. |
| Kafka Producer | 주문 완료 이벤트 발행. |
| Kafka Consumer Group | 랭킹 반영 Consumer와 rebuild Consumer 분리. |
| Kafka Offset | Redis 랭킹 유실 시 replay 복구 후보로 활용. |
| DLT | Consumer 재시도 실패 메시지를 별도 topic으로 격리. |
| Testcontainers | MySQL, Redis, Kafka 통합 테스트에 사용. |
| k6 | 주문 API 동시성, 부하, spike 관찰에 사용. |

## Redis Kafka replay 관계

Redis는 랭킹의 빠른 조회를 위한 저장소이고 Kafka는 주문 완료 이벤트 로그입니다. Redis가 유실되면 Kafka의 `order.completed` 이벤트를 새 Consumer Group으로 다시 읽어 Redis ZSET을 재구성할 수 있습니다.

이 흐름은 Redis Cluster 복제나 Sentinel 장애조치와는 목적이 다릅니다. Cluster와 Sentinel은 Redis 자체 가용성을 높이는 운영 인프라이고, Kafka replay는 파생 데이터를 다시 만드는 애플리케이션 복구 전략입니다.

## DLT 재처리 관계

DLT는 실패 메시지를 버리지 않고 격리하는 장치입니다. 재처리는 DLT에 들어간 메시지를 원인 분석 후 원본 topic이나 별도 repair topic으로 다시 보내는 작업입니다.

이번 과제에서는 DLT 이동을 구현하고, 재처리는 수동 또는 스크립트 방식으로 문서화합니다.
