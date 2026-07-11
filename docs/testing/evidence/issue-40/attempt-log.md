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

## Attempt 2 Final Role Results

- Internal Review: 결정적 sentinel evidence 보강 뒤 PASS했고 남은 finding은 없습니다.
- Independent QA: focused unit 4 tests PASS in 19s; Level 3 actual MySQL 3 tests PASS in 1m 08s; Level 4 actual Kafka/MySQL/Redis 1 test PASS in 1m 02s; fresh Level 1 43/0/0/0 PASS in 1m 46s; Level 5 PASS입니다.
- Level 4 assertion: 원본, duplicate, same-key sentinel 순서에서 DB는 원본과 sentinel eventId 2건이고 Redis score는 `2.0`입니다. raw CLI eventId 값은 수집하지 않았습니다.
- Level 5 observation: MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2, application start 40.173s, partition assigned, health HTTP 200/`UP`입니다. Level 6 traffic이 없어 runtime DB/ZSET은 비어 있었습니다.
- Level 6: 요구하지 않았고 실행하지 않았습니다.
- Config/Cleanup: retry/error handler/DLT 설정은 없고, 검증 리소스 정리 뒤 기존 `pgvector`만 남았습니다.
- Residual risk: 보장 범위는 정상 완료 뒤 같은 key/partition의 순차 Kafka 재전달입니다. direct concurrent same-event 호출의 race loser는 DB unique 위반으로 실패할 수 있으며 exactly-once와 crash consistency는 보장하지 않습니다.

### Next Attempt

- 없음. 내부 Review/QA와 Docs evidence는 완료했습니다. GitHub Actions CI와 사람의 최종 승인은 Main Coordinator가 별도로 확인합니다.

## Attempt 3 - Claude approval minor follow-up

Attempt started at: 2026-07-11T15:50:41.290+09:00
Start source: 현재 Dev가 최신 원격 HEAD를 확인하기 직전에 실측한 시각.
Attempt ended at: 2026-07-11T15:56:31.725+09:00
Attempt duration: 350.435s

### Generate

- Kafka 통합 테스트가 첫 발행 전에 실제 listener container의 1개 partition assignment를 기다리도록 보강했습니다.
- Consumer topic 리터럴을 `OrderEventPublisher.ORDER_COMPLETED_TOPIC` 상수 참조로 교체했습니다.
- 기본 error handler 소진 뒤 skip/offset commit에 따른 장기 장애 이벤트 유실 위험과 Issue #11 retry/DLT, Issue #14 replay/rebuild 복구 경계를 evidence에 추가했습니다.

### Evaluate

- Claude 승인 리뷰의 비차단 MINOR 3건을 Issue #40 범위에서 최소 반영했습니다.

### Failure Cause

- 기존 Level 4 테스트는 listener의 cold rebalance가 끝났다는 명시적 전제 없이 첫 record를 발행했습니다.
- producer와 consumer가 같은 topic 문자열을 각각 소유했고, 현재 error handler의 장기 장애 유실 경계가 remaining risk에 빠져 있었습니다.

### Change Scope

- `RankingEventConsumer`, 해당 Kafka 통합 테스트, Issue #40 evidence/metrics만 변경합니다.
- retry/DLT/replay/rebuild production 구현은 Issue #11/#14 범위이므로 추가하지 않습니다.

### Reverification

- Focused unit + Level 3 + Level 4: `BUILD SUCCESSFUL in 2m 01s`.
- Fresh Level 1: `BUILD SUCCESSFUL in 1m 51s`.
- Fresh Level 1 XML: 43 tests, 0 failures, 0 errors, 0 skipped.
- Harness: `Harness gate PASSED`.

### Next Attempt

- semantic commit/push 후 새 live CI를 Main Coordinator가 확인합니다.
