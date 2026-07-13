# Issue #14 Commands

## TDD와 Level 4

- RED: `.\gradlew.bat test --tests "*RankingRebuildServiceIntegrationTest" --no-daemon` → `RankingRebuildService` 미존재 compile FAIL.
- Focused final: `.\gradlew.bat cleanTest test --tests "*RankingRebuildServiceTest" --tests "*RankingRebuildServiceIntegrationTest" --no-daemon --max-workers=1` → 6 tests PASS, `BUILD SUCCESSFUL in 3m 2s`.
- Related first attempt: Gradle test result binary file의 일시적 `NoSuchFileException`으로 infrastructure FAIL.
- Related reverification: `.\gradlew.bat cleanTest test --tests "*Ranking*" --tests "*PopularMenu*" --no-daemon --max-workers=1` → PASS, `BUILD SUCCESSFUL in 3m 6s`.

## 전체 회귀와 Level 5

- Full intermediate: 57 tests 중 기존 `RankingEventConsumerDltIntegrationTest`가 DLT record timing으로 1회 FAIL했습니다. 해당 test fresh rerun은 `BUILD SUCCESSFUL in 2m 16s`였습니다.
- Full final: `.\gradlew.bat cleanTest test --no-daemon --max-workers=1` → 57 tests, failures 0, errors 0, `BUILD SUCCESSFUL in 5m 40s`.
- Compose: `docker compose -f docker/compose.yaml up -d --wait` → MySQL·Redis·Kafka healthy.
- Normal local app: `.\gradlew.bat bootRun --args=--spring.profiles.active=local --no-daemon` → health 200, charge 200, order 201, Redis member `1` score `1`.
- Maintenance success: `SPRING_PROFILES_ACTIVE=local`, `RANKING_CONSUMER_ENABLED=false`, `RANKING_REBUILD_MAINTENANCE=true`, `RANKING_REBUILD_ENABLED=true` 환경으로 `bootRun` → 삭제한 live key가 member `1` score `1`로 복구됐고 normal group offset `1`, lag `0`, temp/backup key 0개였습니다.
- Maintenance mismatch: DB에만 PAID 주문 1건을 추가하고 같은 runner 실행 → `Kafka replay와 DB 집계가 일치하지 않습니다`, process exit, 기존 live member `1` score `1`, normal group offset `1`, temp/backup key 0개였습니다.
- Cleanup: `docker compose -f docker/compose.yaml --profile tools down -v` → project service 0개, port 8080 free.

## Review remediation

- Focused: `.\gradlew.bat cleanTest test --tests "*RankingRebuildServiceTest" --tests "*RankingRebuildServiceIntegrationTest" --no-daemon --max-workers=1` → 10 tests PASS, `BUILD SUCCESSFUL in 1m 29s`.
- Related: `.\gradlew.bat cleanTest test --tests "*Ranking*" --tests "*PopularMenu*" --no-daemon --max-workers=1` → PASS, `BUILD SUCCESSFUL in 3m 43s`.
- 실제 2-partition topic에서 partial offset update 뒤 timeout을 주입하고 이전 offset 전체와 live Redis가 복원되는지 확인했습니다.
- offset 보상 실패를 주입해 완전한 복원 확인 불가 메시지를 확인했습니다.
- 100ms lease를 30분으로 갱신하고 다른 token release 거부, 두 번째 acquire 거부, 위험 단계 전 lock 상실 fail-closed를 확인했습니다.
- non-midnight snapshot의 8개 날짜 key와 rollback 뒤 sentinel live key 보존을 확인했습니다.
- Full final: `.\gradlew.bat cleanTest test --no-daemon --max-workers=1` → 61 tests, failures 0, errors 0, `BUILD SUCCESSFUL in 3m 32s`.
- Level 5 final: clean Compose에서 health 200, charge 200, order 201 뒤 maintenance rebuild score `1`, normal offset `1`, lag `0`, rebuild key 0개를 확인하고 `down -v`와 port 8080 free를 확인했습니다.
