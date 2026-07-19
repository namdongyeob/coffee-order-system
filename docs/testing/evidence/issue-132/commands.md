# Issue #132 Commands

Issue: #132
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/132
Execution head: edf3984688fbfe5efb9a9e3753da48f71dfbf08e

| Level | 명령 또는 확인 | 결과 |
| --- | --- | --- |
| Level 0 | `git status --short --branch`, `git diff --check`, 허용 diff 검토 | PASS, 범위 밖 변경 0, whitespace 오류 0 |
| Level 1·3·4 | `C:\\Users\\user\\codex-ascii\\issue-132\\gradlew.bat test --tests "...RankingRebuildLateJoinIntegrationTest" --tests "...RankingRebuildServiceIntegrationTest" --tests "...RankingLedgerBilateralRecoveryIntegrationTest" --tests "...RankingEventLedgerIntegrationTest" --tests "...RankingEventProcessorDatabaseIntegrationTest" --tests "...RankingEventConsumerKafkaRedisIntegrationTest" --tests "...RankingEventConsumerDltIntegrationTest" --tests "...RankingEventProcessorTest" --tests "...RankingEventConsumerTest" --no-daemon --rerun-tasks --console=plain` | PASS, 실제 매치된 44 tests, failures/errors/skipped 0, 2m 34s. `RankingEventLedgerIntegrationTest`의 첫 filter는 실제 package와 달라 별도 보완 |
| Level 3·4 | `C:\\Users\\user\\codex-ascii\\issue-132\\gradlew.bat test --tests "com.example.coffeeordersystem.ranking.consumer.RankingEventLedgerIntegrationTest" --no-daemon --rerun-tasks --console=plain` | PASS, 6 tests, failures/errors/skipped 0, 1m 13s |
| Level 5 | `docker compose -f docker/compose.yaml down -v --remove-orphans`, `docker compose -f docker/compose.yaml up -d --wait mysql redis kafka`, `gradlew.bat bootJar --no-daemon` | PASS, MySQL 8.4.5·Redis 7.4.2·Kafka 3.9.1 healthy, jar build 성공 |
| Level 5 | 실제 jar normal consumer + `SET ranking:rebuild:lock level5-manual NX EX 120` + typed Kafka event publish | PASS, lock 보유 5초 동안 ledger 0·marker 없음·score 없음·group active |
| Level 5 | recovery lock 삭제 뒤 DB/Redis/Kafka 조회 | PASS, ledger `COMMITTED/NORMAL_CONSUMER`, marker fingerprint 일치, score `1`, group current/end/lag `1/1/0` |
| Level 5 | active normal consumer 상태에서 maintenance rebuild jar 실행 | PASS(fail-closed), `ranking_rebuild_fence_blocked reason=ACTIVE_NORMAL_CONSUMER phase=START memberCount=1`, swap/run 없음 |
| Level 5 | normal consumer 종료·group member 0 확인 뒤 maintenance rebuild jar 실행 | PASS, `ranking_rebuild_offset_captured ... partition=0 offset=1`, `ranking_rebuild_completed inputRecords=1 uniqueEvents=1 conflicts=0`, run `COMPLETED`, score `1`, lock/temp key 0 |
| Level 0 | `docker compose -f docker/compose.yaml down -v --remove-orphans`, compose/Java/port/temp 조회 | PASS, Issue #132 컨테이너·네트워크·볼륨·jar 프로세스 없음 |

## TDD RED 기록

- late-join 첫 실행: ledger count가 `expected 0L but was 1L`로 실패해 기존 capture 이후 consumer mutation 가능성을 재현했습니다.
- 장시간 fence 첫 실행: 기존 bounded error handler가 3회 뒤 DLT로 recover해 원 offset 처리 대기가 실패했습니다.
- pause handler를 모든 오류에 적용한 중간 실행: test 전용 disabled scheduler와 일반 DLT 회귀가 timeout으로 실패했습니다. fence 예외만 pausing handler를 쓰도록 최소 분리했습니다.

## 환경 메모

- 한글 parent 경로의 Gradle worker classpath 오류는 `C:\\Users\\user\\codex-ascii\\issue-132` junction에서 process cwd까지 ASCII로 바꿔 해결했습니다.
- Codex CLI `0.144.4`, Java 21, filesystem unrestricted, approval policy `never`로 관찰했습니다.
- Dev 전체 Level 1 suite는 지시대로 실행하지 않았습니다. 최신 PR-head 전체 회귀는 GitHub Actions CI가 소유합니다.
