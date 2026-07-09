# 튜터 확인 질문

| 상태 | 질문 | 현재 추천 |
| --- | --- | --- |
| Open | 개인과제 범위에서 Redisson과 DB 비관적 락을 함께 사용해도 적절한가요? | 둘 다 사용합니다. Redisson은 진입 동시성을 줄이고 DB 락은 최종 정합성을 보장합니다. |
| Open | DLT 재처리는 API로 구현해야 하나요, 운영 문서로 충분한가요? | DLT 이동은 구현하고 재처리는 수동 또는 스크립트 방식으로 문서화합니다. |
| Open | Kafka replay 기반 Redis 랭킹 복구를 도전 기능으로 가져가도 괜찮을까요? | Consumer Group과 Redis ZSET 개념을 응용하는 도전 Issue로 둡니다. |
| Open | 인기 메뉴 API 자체에 QueryDSL이 꼭 필요한가요? | API 응답은 Redis 기준으로 하고, QueryDSL은 DB 검증 조회용 별도 Issue로 둡니다. |
| Open | Outbox 패턴을 구현해야 하나요? | ADR 후보로 보류합니다. |