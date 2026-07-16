# Issue #119 Commands

Issue: #119
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/119
Execution head: 8506132df37034e31ee2e8037eb6a37dead2050f

| 구분 | 명령 또는 확인 | 결과 |
| --- | --- | --- |
| Redis marker TDD | 같은 eventId·같은 fingerprint no-op, 다른 fingerprint conflict focused tests | RED 후 GREEN |
| ledger TDD | normal 상태 전이, Redis 적용 뒤 DB 실패·재시도, fingerprint conflict focused tests | RED 후 GREEN |
| Review WRONGTYPE TDD | live ZSET을 string으로 만든 실제 Redis processor 회귀 | RED: marker-only 잔존, GREEN: RESERVED/marker 없음 뒤 retry score 1·COMMITTED |
| DLT guard TDD | processed_event 비의존, pending run 차단, replay source header, publish failure 재시도 tests | RED 후 GREEN |
| 양방향 통합 | `*RankingLedgerBilateralRecoveryIntegrationTest`, DLT↔Rebuild 실제 Kafka 흐름 | PASS, 6/6 |
| 관련 clean | `V:\gradlew.bat clean test --no-daemon --max-workers=1 --tests '*Ranking*' --tests '*PopularMenu*' --tests '*DltReplay*' --tests '*DatabaseSchemaIntegrationTest' --console=plain` | PASS, 76/76, failures/errors/skipped 0, 2m 35s |
| 전체 clean 정본 | `V:\gradlew.bat clean test --no-daemon --max-workers=1 --console=plain` | PASS, 126/126, failures/errors/skipped 0, BUILD SUCCESSFUL in 2m 46s |
| Compose | `docker compose -f docker/compose.yaml up -d --wait` | MySQL·Redis·Kafka healthy |
| normal Level 5 | 실제 charge/order API 뒤 DB·Redis·Kafka 조회 | COMMITTED/NORMAL_CONSUMER, processed 1, menu1 score 1 |
| DLT→Rebuild Level 5 | DLT offset 0 선택 replay 뒤 maintenance Rebuild | RESERVED/DLT_REPLAY→COMMITTED/DLT_REPLAY, menu2 score 1 |
| Rebuild→DLT Level 5 | Rebuild 완료 이벤트를 DLT replay 뒤 normal consumer 재기동 | current/end/lag 3/3/0, menu1 score 1 유지 |
| pending guard Level 5 | PREPARED run 중 선택 DLT replay runner | 기대한 DltReplayRetryableException, original end offset 3·score 1/1 불변 |
| lock cleanup | 각 runner 뒤 `EXISTS ranking:rebuild:lock` | 0 |
| diff | `git diff --check` | PASS, whitespace error 0 |
| cleanup | `docker compose -f docker/compose.yaml down -v --remove-orphans` | project container/network/volume 제거 |

Gradle 정본 검증은 한글 cwd의 worker classpath 문제를 피하기 위해 ASCII subst `V:\`에서 실행했습니다.
