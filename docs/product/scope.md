# 범위

## 먼저 구현할 항목

1. 문서와 Issue 템플릿.
2. 메뉴 목록 조회 API.
3. 포인트 충전 API.
4. 트랜잭션과 DB 비관적 락이 적용된 주문/결제 API.
5. Redisson 주문/결제 락.
6. Kafka 이벤트 발행.
7. Redis 랭킹 Consumer.
8. 인기 메뉴 Top 3 API.
9. DLT와 멱등 처리.
10. 검증 산출물.

## 도전 후보

- QueryDSL 검증 조회와 EXPLAIN 분석.
- k6 Load, Stress, Spike 스크립트.
- Kafka replay와 `ranking-rebuild-group` 기반 Redis 랭킹 rebuild runner.
- DLT replay 스크립트.

## 보류 항목

- Outbox 패턴.
- Redis Cluster.
- 완전 자동 DLT replay.
- Spring REST Docs.