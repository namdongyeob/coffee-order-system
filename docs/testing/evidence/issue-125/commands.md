# Issue #125 Commands

Issue: #125
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/125
Execution head: da96594416d5286ea9a7e2675c5f5d316a2e5470

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
| Level 0 | 전체 회귀 뒤 Java PID·Testcontainers 조회 | PASS, 잔여 0 |

## 환경 메모

- 실제 저장소 경로의 한글 parent에서 Gradle worker classpath 문제가 재현되어 `subst W:`로 동일 worktree를 매핑했습니다.
- 장기 전체 회귀는 Test Executor PID `27552`, Gradle daemon PID `14312`, CPU 증가와 공유 Testcontainers 4개를 확인하고 재시작하지 않았습니다.
- Codex CLI `0.144.4`, 모델 `GPT-5` 계열, reasoning effort는 실행 환경에서 미노출, filesystem unrestricted, approval policy `never`로 관찰했습니다.
- Attempt 2에서는 전체 139 회귀와 Level 5를 반복하지 않았습니다. production 변경 이후 전체 회귀는 최신 PR head GitHub CI가 소유합니다.
