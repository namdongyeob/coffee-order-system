# 검증 로그

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-11 | Issue #40 Kafka Consumer idempotency | Level 1 | PASS | 빌드·Unit·전체 회귀 smoke | `./gradlew.bat test --no-daemon`; `docs/testing/evidence/issue-40/test-output.txt` | 독립 QA가 fresh 전체 43 tests, 0 failures, 0 errors, 0 skipped와 `BUILD SUCCESSFUL in 1m 46s`를 확인했습니다. focused unit 4 tests도 19초에 PASS했습니다. |
| 2026-07-11 | Issue #40 Kafka Consumer idempotency | Level 3 | PASS | 실제 MySQL·트랜잭션·멱등성 통합 | `RankingEventProcessorDatabaseIntegrationTest`; `docs/testing/evidence/issue-40/test-output.txt` | 독립 QA가 순차 duplicate, 다른 eventId, Redis 실패 rollback의 3 tests를 실제 MySQL에서 1분 08초에 PASS했습니다. concurrent direct same-event 호출의 정상 반환은 보장하지 않습니다. |
| 2026-07-11 | Issue #40 Kafka Consumer idempotency | Level 4 | PASS | 실제 Kafka·MySQL·Redis Consumer 통합 | `RankingEventConsumerKafkaRedisIntegrationTest`; `docs/testing/evidence/issue-40/test-output.txt` | 원본, duplicate, same-key sentinel 순서 뒤 assertion에서 DB는 원본과 sentinel eventId 2건이고 Redis score는 `2.0`임을 확인했습니다. 정상 완료 뒤 순차 duplicate가 row/score를 추가하지 않는 범위이며 raw CLI eventId는 수집하지 않았습니다. |
| 2026-07-11 | Issue #40 Kafka Consumer idempotency | Level 5 | PASS | 로컬 앱·실제 인프라 기동 | `docs/testing/evidence/issue-40/manual-qa.md` | MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2와 앱이 기동했고 40.173초 start, partition assigned, health HTTP 200/`UP`을 확인했습니다. Level 6 traffic이 없어 runtime DB/ZSET은 비어 있었고 cleanup 뒤 기존 `pgvector`만 남았습니다. Level 6은 API 변경이 없어 NO입니다. |
