# 범위

## 구현 완료 항목

1. 문서와 Issue 템플릿.
2. 메뉴 목록 조회 API.
3. 포인트 충전 API.
4. 트랜잭션과 DB 비관적 락이 적용된 주문/결제 API.
5. Redisson 주문/결제 락.
6. Kafka 이벤트 발행.
7. Redis 랭킹 Consumer.
8. 인기 메뉴 Top 3 API.
9. DLT와 멱등 처리.
10. Transactional Outbox와 미발행 이벤트 재시도.
11. Kafka replay ranking rebuild와 DLT 한 건 선택 재발행.
12. DB ledger·Redis Lua 기반 normal/DLT/rebuild 중복 집계 방지.
13. 30일 ranking ledger bounded retention.
14. 실제 Docker·HTTP·DB·Kafka·Redis·k6 검증 산출물.

## 함께 완료한 검증

- QueryDSL 검증 조회와 MySQL EXPLAIN 분석.
- k6 Load, Stress, Spike 실행.
- Testcontainers 기반 MySQL·Kafka·Redis 통합 테스트.
- Level 5 애플리케이션 실제 기동과 Level 6 필수 API 실제 HTTP 검증.

## 범위 밖·보류 항목

- Redis Cluster.
- 완전 자동 DLT replay.
- Spring REST Docs.
- 회원가입·인증, 메뉴 관리 CRUD, 주문 취소.
- Kubernetes와 외부 PG 결제.

최종 구현과 검증 진입점은 [README](../../README.md), 상세 API는 [API 명세](../api/api-spec.md), 최종 schema는 [ERD](../db/erd.md)에서 확인합니다.
