# Issue #98 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/98

Execution mode: STRICT
Execution mode reason: Kafka Consumer와 Redis 처리 순서를 변경하는 concurrency·infra 위험이 있는 변경입니다.
Level 5 required: NO
Level 5 reason: 새로운 HTTP 진입점이나 로컬 서버 기동 시나리오 변경이 없고, Level 4 Testcontainers 통합 테스트로 컨슈머 재처리 시나리오를 검증합니다.
Level 6 required: NO
Level 6 reason: 외부 API 계약이 바뀌지 않으므로 실제 HTTP 요청 검증 대상이 아닙니다.

## 완료 기준

- [x] Redis 점수 증가가 DB 커밋 확정 이후에만 반영되도록 수정되었습니다(또는 동등한 안전장치가 적용되었습니다). `PopularMenuRankingService.increment(eventId, menuId, orderedAt)`가 eventId 기준 SISMEMBER→SADD→EXPIRE→ZINCRBY를 하나의 Lua `EVAL`로 원자 실행해, DB/Kafka 재시도 순서와 무관하게 같은 eventId의 두 번째 반영을 무시합니다.
- [x] Kafka 재처리(같은 eventId 2회 이상 소비) 시나리오에서 Redis 점수가 1회만 증가함을 테스트로 검증했습니다. `PopularMenuRankingRedisIntegrationTest.doesNotDoubleCountWhenIncrementCalledTwiceWithSameEventId`(실제 Redis Testcontainers)가 같은 eventId로 `increment()`를 두 번 호출해도 score가 1.0으로 유지됨을 확인합니다.
- [x] `docs/testing/evidence-guide.md`의 기본 evidence 파일을 작성했습니다.

## 참고

- 기존 안전장치("Redis 증가 실패 시 DB 트랜잭션 롤백", `RankingEventProcessorDatabaseIntegrationTest.rollsBackHistoryWhenRankingUpdateFails`)는 이 Issue에서 변경하지 않았습니다. `increment()`는 여전히 `@Transactional process()` 내부에서 동기 호출되며, Lua 스크립트 실행이 실패하면 예외가 그대로 전파되어 DB가 롤백됩니다.
- fresh 독립 Review Agent와 fresh 독립 QA Agent는 이 evidence 작성 시점의 uncommitted 작업 diff(커밋 `9bacc9314516991af6e84f7689b2c457d831aec2`와 동일 content)에서 각각 `APPROVED`(P0/P1/P2 없음), `PASS`(focused 12/12, 전체 회귀 69/69, Python 하네스 160/160)입니다. `Current head`는 이 커밋을 가리킵니다.
