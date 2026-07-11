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

## Attempt 2 - Review deterministic duplicate evidence

Attempt started at: 2026-07-11T14:15:12.165+09:00
Start source: Main Coordinator가 Review FAIL 수정 Attempt 시작 시 실측해 전달한 시각.
Attempt ended at: 2026-07-11T14:21:30.106+09:00
Attempt duration: 377.941s

### Generate

- production/config는 변경하지 않고 Level 4 duplicate 검증의 고정 `Thread.sleep`을 제거했습니다.
- 동일 `userId` Kafka key로 원본, 같은 eventId duplicate, 새 eventId sentinel을 순서대로 발행합니다.
- sentinel의 `processed_event` commit을 기다린 뒤 원본과 sentinel 2행, 동일 메뉴 Redis score `2.0`을 검증합니다.

### Evaluate

- Review P1 수정 대상입니다. 동일 partition 순서상 sentinel 완료는 그 앞 duplicate listener 호출이 정상 반환했음을 증명합니다.
- Review P2는 production 변경 없이 보증 범위를 명확히 제한했습니다.

### Failure Cause

- 기존 Level 4 테스트는 첫 이벤트 처리 뒤 score `1.0` 조건이 이미 참이어서 duplicate가 실제 소비됐는지 증명하지 못했고, `Thread.sleep(1_000)`에 의존했습니다.

### Change Scope

- Level 4 Kafka·Redis 통합 테스트와 Issue #40 evidence/metrics만 수정합니다.
- `existsByEventId` 뒤 `saveAndFlush`하는 production 경계는 유지합니다. 정상 sequential Kafka redelivery는 차단하지만 direct concurrent same-event 호출의 race loser는 DB unique 위반으로 실패할 수 있습니다.

### Reverification

- 강화 Level 4 actual Kafka/MySQL/Redis: 1 test, `BUILD SUCCESSFUL in 1m 08s`.
- Focused Level 3 actual MySQL: 3 tests, `BUILD SUCCESSFUL in 1m 05s`.
- Fresh Level 1: 43 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESSFUL in 2m 02s`.
- Harness: `Harness gate PASSED`.
- Level 5는 production/config가 Attempt 1과 동일하므로 Dev Attempt 2에서 재실행하지 않습니다. 독립 QA가 fresh runtime을 수행합니다.

### Next Attempt

- 독립 QA에 same-partition sentinel 증거와 direct concurrent 호출 비보장 범위를 전달합니다.
