# Issue #112 Commands

Issue: #112
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/112
Execution head: 3e25f27

| 구분 | 명령 또는 확인 | 결과 |
| --- | --- | --- |
| fingerprint/schema | `Push-Location U:\; .\gradlew.bat test --no-daemon --max-workers=1 --tests '*RankingRebuildEventTest' --tests '*RankingRebuildLedgerSchemaTest'` | PASS, BUILD SUCCESSFUL in 34s |
| test lifecycle | `Push-Location U:\; .\gradlew.bat test --no-daemon --max-workers=1 --tests '*SharedTestcontainersLifecycleIntegrationTest'` | PASS, BUILD SUCCESSFUL in 1m 10s |
| conflict/pending RED | focused 2 tests, prevalidation·recovery short-circuit 구현 전 실행 | RED, live 변경 및 두 번째 run 생성 2건 실패 |
| conflict/pending GREEN | 같은 focused 2 tests 재실행 | PASS, BUILD SUCCESSFUL in 1m 35s |
| nanos RED | nanoseconds orderedAt 동일 재실행 focused test | RED, EVENT_ID_PAYLOAD_CONFLICT 재현 |
| nanos GREEN | 저장된 run-event fingerprint 사용 후 같은 focused test | PASS, BUILD SUCCESSFUL in 1m 33s |
| Review crash RED | swap mark failure, partial offset crash, incomplete compensation, 101-event heartbeat focused tests | RED, marker 없음·offset 미복구·run/events 삭제·renew 미호출을 각각 확인 |
| Review crash GREEN | 같은 5 focused tests | PASS, swap marker/same-run offset/cascade or uncertain/heartbeat 상태 확인 |
| Retry-boundary RED | 부분 backfill+offset verify, cancel 실패 봉인, prepare batch lease, swap 직전 lease focused 4 tests | RED, 4/4 예상 실패 |
| Retry-boundary GREEN | 같은 focused 4 tests와 기존 backfill heartbeat focused test | PASS, 4/4 및 1/1 |
| Artifact/lock RED | durable cleanup, PREPARED marker 소실, SWAPPED backup·존재 메타 소실, recovery lock 해제 focused 5 tests | RED, 5/5 예상 실패 |
| Artifact/lock GREEN | 같은 focused 5 tests | PASS, 5/5, BUILD SUCCESSFUL in 1m 6s |
| Rebuild bundle | `Push-Location U:\; .\gradlew.bat test --no-daemon --max-workers=1 --tests '*RankingRebuild*' --console=plain` | PASS, 31/31, BUILD SUCCESSFUL in 1m 34s |
| 관련 clean bundle | `Push-Location U:\; .\gradlew.bat clean test --no-daemon --max-workers=1 --tests '*Ranking*' --tests '*PopularMenu*' --console=plain` | PASS, 54/54, failures/errors/skipped 0, BUILD SUCCESSFUL in 2m 10s |
| 전체 | `Push-Location U:\; .\gradlew.bat clean test --no-daemon --max-workers=1 --console=plain` | PASS, 110/110, failures/errors/skipped 0, BUILD SUCCESSFUL in 2m 23s |
| Compose | `docker compose -f docker\compose.yaml up -d --wait` | MySQL·Redis·Kafka healthy |
| 최초 rebuild | maintenance runner 실행 뒤 MySQL ledger/run과 Redis score 조회 | ledger 1, COMMITTED/REBUILD, completed run 1, score 1, lock 0, temp key 0 |
| 동일 재실행 | 동일 Kafka event로 runner 재실행 뒤 같은 조회 | ledger 1, distinct fingerprint 1, completed run 2, score 1, lock 0 |
| Level 5 최초 | Compose healthy, normal API 주문 1건 뒤 maintenance runner | input/unique/conflict 1/1/0, run/ledger/offset plan 각 1, current=end=1, lag 0, score 1 |
| Level 5 swap-mark crash | 완료 run을 PREPARED+swap marker로 조성, ledger 삭제, normal offset 0(lag 1) 뒤 runner | input/unique/conflict 0/0/0, run 총수 1·같은 runId, offset 1/lag 0, ledger COMMITTED, marker·lock 0 |
| Level 5 durable recovery | 최신 코드 완료 run을 intact PREPARED+무기한 marker/backup/존재 메타로 조성 뒤 runner | input/unique/conflict 0/0/0, 같은 run COMPLETED, run/events 1/1, current/end/lag 1/1/0, score 1, marker/meta/backup/lock 0 |
| Level 5 artifact loss | 같은 run을 SWAPPED_PENDING_OFFSET로 조성한 뒤 원래-live backup 1개 삭제 후 runner | 예상 fail-closed, RECOVERY_REQUIRED, run/events 1/1, ledger 1, score 1, current/end/lag 1/1/0, lock 1, marker/meta TTL -1 |
| Kafka group | AdminClient로 정상 consumer group member와 current/end/lag 확인 | active member 없음, current=end=1, lag 0 |
| diff | `git diff --cached --check` | PASS, LF/CRLF 안내 외 오류 없음 |
| 범위 | staged path와 `DltReplayService`·consumer production·Redis production pattern 비교 | 대상 변경 0개 |
| 보안·크기 | staged diff token/private-key/password pattern, 파일별 1MB 초과 검사 | secret hit 0, oversized file 0 |
| cleanup | runner PID 종료 후 `docker compose ... down`, `docker ps` 확인 | Compose container/network 제거, 잔여 container 0 |

모든 Gradle 검증은 한글 worktree 경로 문제를 피하기 위해 ASCII subst `U:\`에서 실행했습니다. Level 5 runner는 기능 완료 로그와 DB·Redis evidence를 확보한 뒤 장기 실행 ApplicationRunner PID만 종료했으며, 이때 Ctrl+C로 wrapper가 exit 1을 반환한 것은 기능 실패가 아닙니다.
