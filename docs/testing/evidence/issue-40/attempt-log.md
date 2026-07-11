# Issue Attempt Log

Issue: #40
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/40
Branch: codex/issue-40-kafka-consumer-idempotency

## Attempt 1 - Kafka ranking Consumer idempotency

Attempt started at: 2026-07-11T13:54:26.475+09:00
Start source: Main Coordinator가 전체 시작 시 실측해 전달한 시각.
Attempt ended at: 2026-07-11T14:09:08.832+09:00
Attempt duration: 882.357s

### Generate

- acceptance evidence를 production/test 변경보다 먼저 작성했습니다.
- RED에서 `RankingEventProcessor` 미존재 compile failure를 확인한 뒤 listener와 별도 transactional processor를 최소 구현했습니다.
- processor는 정상 완료 중복 선조회, `processed_event` insert/flush, 기존 Redis 랭킹 서비스 호출 순서로 처리합니다.
- 실제 Kafka RED에서 String payload conversion failure를 확인하고 Consumer `JsonDeserializer` 설정만 보완했습니다.

### Evaluate

- PASS. 단위, 실제 MySQL unique/transaction, 실제 Kafka·Redis end-to-end, Level 5 runtime, fresh Level 1 전체 회귀와 harness를 통과했습니다.

### Failure Cause

- 최초 Level 4 RED 원인은 기본 `StringDeserializer`가 JSON을 `OrderCompletedEvent`로 변환하지 못한 `MessageConversionException`이었습니다.

### Change Scope

- `order.completed` ranking Consumer, 별도 transactional processor, 필요한 Consumer JSON 역직렬화 설정과 직접 테스트/evidence만 변경했습니다.
- Redis key/ZSET 구현, migration/unique, retry/error handler/DLT, replay/rebuild/Top3는 변경하지 않았습니다.

### Reverification

- Baseline Level 1: `BUILD SUCCESSFUL in 1m 50s`.
- TDD RED: missing `RankingEventProcessor`로 `BUILD FAILED in 18s`.
- Unit GREEN: 4 tests, `BUILD SUCCESSFUL in 26s`.
- Level 3 actual MySQL: 3 tests, `BUILD SUCCESSFUL in 1m 19s`.
- Level 4 RED: String에서 `OrderCompletedEvent` 변환 실패, 1 test failed, `BUILD FAILED in 1m 16s`.
- Level 4 GREEN actual Kafka/MySQL/Redis: 1 test, `BUILD SUCCESSFUL in 1m 05s`.
- Level 5: app started 44.082s, health HTTP 200/UP, MySQL·Kafka·Redis `Up`, listener partition assigned.
- Fresh Level 1: XML 43 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESSFUL in 1m 49s`.
- Harness: `Harness gate PASSED`.

### Next Attempt

- 독립 Review, QA, Docs와 CI를 실행합니다. Redis 성공 후 DB commit 전 process crash window와 Dev pre-push의 Docs 소유 verification-log gate를 확인합니다.
