# 검증 매트릭스

각 기능이 어떤 검증을 통과해야 완료라고 말할 수 있는지 정리합니다.

| 기능 | 최소 검증 | 추가 검증 |
| --- | --- | --- |
| 메뉴 목록 조회 | Controller 테스트 | 실제 http 요청 |
| 포인트 충전 | Controller 테스트, DB 통합 테스트 | 동시 충전 케이스 |
| 주문 결제 | Controller 테스트, DB 통합 테스트 | 동시 주문, 잔액 부족, 메뉴 없음 |
| Redisson 락 | Redis Testcontainers 통합 테스트 | 다중 스레드 주문 시도 |
| Kafka 이벤트 발행 | Kafka Testcontainers 통합 테스트 | payload schema 검증 |
| Kafka Consumer 랭킹 반영 | Kafka, Redis 통합 테스트 | 중복 이벤트 멱등 처리 |
| 인기 메뉴 Top 3 | Controller 테스트, Redis 통합 테스트 | DB 원천 집계 비교 |
| DLT 이동 | Consumer 실패 통합 테스트 | DLT topic 메시지 확인 |
| Redis rebuild | 로컬 실행 검증 | DB 집계와 Redis 결과 비교 |
| k6 | 로컬 서버 실행 후 k6 결과 | stress, spike 분리 |

검증 결과는 `docs/testing/evidence/issue-{number}/verification.md`에 기록합니다. 전역 뷰가 필요하면 [Evidence Guide](evidence-guide.md)의 재현 명령을 사용하며 생성 파일은 커밋하지 않습니다.
