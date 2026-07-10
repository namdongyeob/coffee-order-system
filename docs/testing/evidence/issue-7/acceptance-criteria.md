# Issue #7 완료 기준

- [x] 주문 진입 락 키는 `lock:order:user:{userId}` 형식을 사용합니다.
- [x] 같은 사용자의 주문/결제 핵심 로직은 Redisson 락 획득 후에만 실행합니다.
- [x] 락 획득은 `waitTime` 2초, `leaseTime` 5초를 사용합니다.
- [x] 락 획득 실패는 프로젝트 공통 예외 계약의 `409 Conflict`로 응답합니다.
- [x] 획득한 락은 성공과 실패 모두 현재 스레드가 소유할 때 해제합니다.
- [x] 기존 `UserPointRepository#findByUserIdForUpdate` DB 비관적 락을 유지합니다.
- [x] Redis Testcontainers 기반 Level 4 통합 테스트로 실제 락 경합과 획득 실패를 검증합니다.
- [x] focused 테스트와 전체 `./gradlew.bat test --no-daemon` 회귀 테스트를 통과합니다.

Execution mode: STRICT
Execution mode reason: Redisson 분산락, 주문 트랜잭션 진입 경계와 Redis 인프라 통합을 변경하므로 독립 Dev, Review, QA, Docs 검증과 CI가 필요합니다.
Level 5 required: YES
Level 5 reason: 애플리케이션 런타임의 Redisson 연결과 주문 서비스 진입 경계가 변경됩니다.
Level 6 required: YES
Level 6 reason: 락 획득 실패의 실제 HTTP 409 응답 계약을 확인해야 합니다.
