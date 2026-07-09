# 아키텍처 개요

```text
Client
  -> Coffee API Server
      -> Menu / Point / Order Services
      -> MySQL
      -> Redis / Redisson
      -> Kafka Producer
  -> Kafka topic: order.completed
      -> ranking-consumer-group
          -> Redis ZSET popular menu ranking
      -> repeated failure
          -> order.completed.DLT
```

## 계층

- Controller는 HTTP 요청, 응답, 검증을 담당합니다.
- Service는 트랜잭션, 비즈니스 규칙, 락, 이벤트 발행 흐름을 담당합니다.
- Repository는 JPA 영속성과 락 조회를 담당합니다.

## 원천 데이터

- MySQL은 메뉴, 포인트, 주문, 처리된 이벤트 이력을 저장합니다.
- Redis는 파생 랭킹 데이터를 저장합니다.
- Kafka는 비동기 Consumer와 도전 복구 흐름을 위한 주문 완료 이벤트를 저장합니다.