# Issue #112 Commands

Issue: #112
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/112
Execution head: f33b054

| 구분 | 명령 또는 확인 | 결과 |
| --- | --- | --- |
| fingerprint/schema | `Push-Location U:\; .\gradlew.bat test --no-daemon --max-workers=1 --tests '*RankingRebuildEventTest' --tests '*RankingRebuildLedgerSchemaTest'` | PASS, BUILD SUCCESSFUL in 34s |
| test lifecycle | `Push-Location U:\; .\gradlew.bat test --no-daemon --max-workers=1 --tests '*SharedTestcontainersLifecycleIntegrationTest'` | PASS, BUILD SUCCESSFUL in 1m 10s |
| conflict/pending RED | focused 2 tests, prevalidation·recovery short-circuit 구현 전 실행 | RED, live 변경 및 두 번째 run 생성 2건 실패 |
| conflict/pending GREEN | 같은 focused 2 tests 재실행 | PASS, BUILD SUCCESSFUL in 1m 35s |
| nanos RED | nanoseconds orderedAt 동일 재실행 focused test | RED, EVENT_ID_PAYLOAD_CONFLICT 재현 |
| nanos GREEN | 저장된 run-event fingerprint 사용 후 같은 focused test | PASS, BUILD SUCCESSFUL in 1m 33s |
| Rebuild bundle | `Push-Location U:\; .\gradlew.bat test --no-daemon --max-workers=1 --tests '*RankingRebuildEventTest' --tests '*RankingRebuildLedgerSchemaTest' --tests '*RankingRebuildServiceIntegrationTest'` | PASS, integration 14 포함, BUILD SUCCESSFUL in 1m 33s |
| 관련 clean bundle | `Push-Location U:\; .\gradlew.bat clean test --no-daemon --max-workers=1 --tests '*Ranking*' --tests '*PopularMenu*'` | PASS, 43/43, failures/errors/skipped 0, BUILD SUCCESSFUL in 2m 19s |
| 전체 | `Push-Location U:\; .\gradlew.bat clean test --no-daemon --max-workers=1 --console=plain` | PASS, 99/99, failures/errors/skipped 0, BUILD SUCCESSFUL in 3m 15s |
| Compose | `docker compose -f docker\compose.yaml up -d --wait` | MySQL·Redis·Kafka healthy |
| 최초 rebuild | maintenance runner 실행 뒤 MySQL ledger/run과 Redis score 조회 | ledger 1, COMMITTED/REBUILD, completed run 1, score 1, lock 0, temp key 0 |
| 동일 재실행 | 동일 Kafka event로 runner 재실행 뒤 같은 조회 | ledger 1, distinct fingerprint 1, completed run 2, score 1, lock 0 |
| pending 복구 | 최신 run을 SWAPPED_PENDING_LEDGER로 조성하고 ledger 행 삭제 뒤 runner 실행 | input/unique/conflict 0/0/0, run 총수 2 유지, 같은 run id로 COMMITTED backfill |
| Kafka group | AdminClient로 정상 consumer group member와 current/end/lag 확인 | active member 없음, current=end=1, lag 0 |
| diff | `git diff --cached --check` | PASS, LF/CRLF 안내 외 오류 없음 |
| 범위 | staged path와 `DltReplayService`·consumer production·Redis production pattern 비교 | 대상 변경 0개 |
| 보안·크기 | staged diff token/private-key/password pattern, 파일별 1MB 초과 검사 | secret hit 0, oversized file 0 |
| cleanup | runner PID 종료 후 `docker compose ... down`, `docker ps` 확인 | Compose container/network 제거, 잔여 container 0 |

모든 Gradle 검증은 한글 worktree 경로 문제를 피하기 위해 ASCII subst `U:\`에서 실행했습니다. Level 5 runner는 기능 완료 로그와 DB·Redis evidence를 확보한 뒤 장기 실행 ApplicationRunner PID만 종료했으며, 이때 Ctrl+C로 wrapper가 exit 1을 반환한 것은 기능 실패가 아닙니다.
