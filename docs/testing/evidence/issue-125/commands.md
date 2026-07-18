# Issue #125 Commands

Issue: #125
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/125
Execution head: d976fe6b56e95061382edddbc209e1405af14e62

| Level | 명령 또는 확인 | 결과 |
| --- | --- | --- |
| Level 0 | `git diff --check`, 허용 diff·금지 범위 검토 | PASS, whitespace error 0, Redis marker SCAN cleanup 없음 |
| Level 1·3·4 | `W:\gradlew.bat test --tests "com.example.coffeeordersystem.ranking.retention.*" --no-daemon` | PASS, 신규 13 tests |
| Level 3·4 | `W:\gradlew.bat test --tests "...RankingLedgerBilateralRecoveryIntegrationTest" --tests "...RankingEventLedgerIntegrationTest" --tests "...RankingEventConsumerDltIntegrationTest" --no-daemon` | PASS, 기존 9 tests, ADR-008/#119 불변조건 회귀 |
| Level 3 | MySQL `information_schema.statistics`, cleanup `EXPLAIN` | PASS, `idx_ranking_event_ledger_cleanup(state, committed_at)`, `type=range` |
| Level 4 | 실제 Redis marker TTL과 scheduler tick 뒤 marker 조회 | PASS, 설정 TTL 적용 및 별도 marker 유지 |
| Level 5 | Compose MySQL·Redis·Kafka + `bootRun`, retention `1s`, batch `2`, fixed delay `2s` | PASS, Flyway V7, `deleted=2 -> 1 -> 0`, pending 2행 보존 |
| Level 5 | `kafka-retention=2s`, `ledger-retention=1s` invalid `bootRun` | PASS, startup 거부, 기존 적격 `COMMITTED` 행 보존 |
| Level 1 | `W:\gradlew.bat clean test --no-daemon --max-workers=1 --console=plain` | PASS, 139 tests, failures/errors/skipped 0, 3m 12s |
| Level 1 | Attempt 2 properties RED→GREEN focused | PASS, disabled unknown 허용·enabled unknown startup 거부 |
| Level 1·3 | `W:\gradlew.bat test --tests "...RankingLedgerRetentionPropertiesTest" --tests "...RankingLedgerRetentionConfigurationTest" --tests "...cleanupIndexHasStateCommittedAtOrderAndExplainUsesIt" --no-daemon` | PASS, current production head targeted 6 tests, 1m |
| Level 3 | `EXPLAIN ` + production `RankingLedgerCleanupRepository.CANDIDATE_SQL` | PASS, production의 `FORCE INDEX (idx_ranking_event_ledger_cleanup)`, `type=range` |
| Level 1 | `gh run view 29633092590 --log-failed` | FAIL 원인 확인, 141 tests 중 cleanup 동시성 1건이 disabled 설정의 null `kafka-retention` 검증에서 실패 |
| Level 1·3 | CI 실패 동시성 테스트 단독 로컬 재현 | RED, CI와 동일 `ranking.ledger.cleanup.kafka-retention must be a known positive duration` |
| Level 1 | Attempt 3 disabled core·sub-second marker 회귀 테스트 | RED, 신규 2건 assertion failure → GREEN |
| Level 1·3 | `Set-Location W:\; .\gradlew.bat test --tests "...RankingLedgerRetentionPropertiesTest" --tests "...RankingLedgerRetentionConfigurationTest" --tests "...concurrentInvocationsKeepEachBatchBoundedWithoutDuplicateDeleteErrors" --no-daemon --console=plain` | PASS, production head focused 8 tests, `BUILD SUCCESSFUL in 1m 1s` |
| Level 1 | Attempt 4 `ledger-retention=1500ms`, `redis-marker-ttl=1500ms` | RED, 설정 Duration 비교는 통과하지만 Redis `EX` 실효 TTL 1초가 ledger보다 짧은 신규 assertion 1건 실패 |
| Level 1 | `Set-Location W:\; .\gradlew.bat test --tests "...RankingLedgerRetentionPropertiesTest" --no-daemon --console=plain` | PASS, production head focused 6 tests, failures/errors/skipped 0, `BUILD SUCCESSFUL in 25s` |
| Level 0 | 전체 회귀 뒤 Java PID·Testcontainers 조회 | PASS, 잔여 0 |

## 환경 메모

- 실제 저장소 경로의 한글 parent에서 Gradle worker classpath 문제가 재현되어 `subst W:`로 동일 worktree를 매핑했습니다.
- 장기 전체 회귀는 Test Executor PID `27552`, Gradle daemon PID `14312`, CPU 증가와 공유 Testcontainers 4개를 확인하고 재시작하지 않았습니다.
- Codex CLI `0.144.4`, 모델 `GPT-5` 계열, reasoning effort는 실행 환경에서 미노출, filesystem unrestricted, approval policy `never`로 관찰했습니다.
- Attempt 2에서는 전체 139 회귀와 Level 5를 반복하지 않았습니다. production 변경 이후 전체 회귀는 최신 PR head GitHub CI가 소유합니다.
- Attempt 3 첫 두 로컬 명령은 `W:\gradlew.bat`만 호출해 실제 working directory가 한글 경로로 남아 test class loading에 실패했습니다. `Set-Location W:\`로 process working directory까지 전환한 뒤 애플리케이션 RED와 GREEN을 재현했습니다.
- Attempt 3에서는 전체 141 회귀와 Level 5를 반복하지 않았습니다. 최종 전체 회귀는 최신 PR head GitHub CI가 소유합니다.
- Attempt 4에서도 전체 141 회귀와 Level 5를 실행하지 않았습니다. 최신 evidence-only PR head GitHub CI가 전체 회귀를 소유합니다.
