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

## Independent QA

- Focused Level 4: `.\gradlew.bat cleanTest test --tests "*RankingRebuildServiceTest" --tests "*RankingRebuildServiceIntegrationTest" --no-daemon --max-workers=1` → 통합 7건과 단위 3건, 총 10 tests, failures/errors/skipped 0, `BUILD SUCCESSFUL in 1m 41s`.
- Level 5 success: clean Compose의 MySQL·Redis·Kafka가 healthy였고, local 앱 health 200, charge 200, order 201 뒤 live score `1`, normal group offset `1`, log-end `1`, lag `0`을 확인했습니다. Maintenance rebuild 뒤 삭제한 live key가 score `1`로 복구되고 temp/backup key는 0개였습니다.
- Level 5 mismatch: DB에만 PAID 주문을 추가한 실행은 예상된 집계 불일치로 non-zero 종료했고, live score `1`, normal offset `1`, lag `0`, temp/backup key 0개를 보존했습니다.
- Cleanup: QA가 시작한 앱과 project Compose·volume·network를 정리했고 port 8080은 free였습니다. 기존 `rag-pgvector`는 건드리지 않았습니다.
- 첫 background 기동 명령은 PowerShell 인수 분리로 애플리케이션 진입 전 Gradle CLI parse 오류가 났습니다. 환경변수 방식으로 명령을 교정한 뒤 위 성공·실패 시나리오를 완료했으며 구현 결함으로 집계하지 않았습니다.

## Retention P1 최종 remediation

- Review RED: current earliest가 `0`보다 크면 즉시 거부하던 구현에서 `retainedRecentEventsRebuildWhenCurrentEarliestOffsetIsGreaterThanZero`가 예상대로 FAIL했습니다. `BUILD FAILED in 1m 53s`였습니다.
- GREEN 2건: current earliest `> 0`이지만 최근 이벤트가 보존된 성공과 필요한 최근 이벤트가 실제 삭제된 DB mismatch 실패를 함께 실행해 2 tests PASS, `BUILD SUCCESSFUL in 1m 32s`였습니다.
- 최초 focused: 단위·통합 11 tests PASS, `BUILD SUCCESSFUL in 1m 24s`였습니다.
- 최초 related: `*Ranking*`·`*PopularMenu*` PASS, `BUILD SUCCESSFUL in 4m 36s`였습니다.
- 최초 full은 전체 62건 중 기존 DLT timing test 1건이 `No records found for topic`으로 FAIL했고, 깨끗한 프로세스의 해당 격리 1건도 `BUILD FAILED in 2m 6s`로 재현됐습니다. retention 변경과 무관한 blocker Issue #77로 분리했고 DLT 파일은 이 PR에서 수정하지 않았습니다.
- #77 merge commit `1f81662deaf28c86952604e330e4d392b2d6884a` 반영 뒤 focused: `cleanTest test --tests "*RankingRebuildServiceTest" --tests "*RankingRebuildServiceIntegrationTest" --no-daemon --max-workers=1` → 11 tests, failures/errors/skipped 0, `BUILD SUCCESSFUL in 2m 34s`.
- Related: `cleanTest test --tests "*Ranking*" --tests "*PopularMenu*" --no-daemon --max-workers=1` → 28 tests, failures/errors/skipped 0, `BUILD SUCCESSFUL in 4m 57s`.
- Full: `cleanTest test --no-daemon --max-workers=1` → 62 tests, failures/errors/skipped 0, `BUILD SUCCESSFUL in 4m 53s`. #77에서 안정화한 DLT test도 포함해 PASS했습니다.
- Level 5 success: clean Compose와 local API로 DB 주문과 Kafka recent event를 만들고 old offset `0`만 실제 삭제했습니다. earliest `1`, latest `2`에서 runner가 offset `1` recent event를 replay해 live member `1` score `1`, normal offset `2`, lag `0`, temp/backup key 0개로 완료했습니다.
- Level 5 retention loss: recent event까지 `delete-records`로 삭제해 earliest/latest `2/2`를 만든 뒤 runner가 `Kafka replay와 DB 집계가 일치하지 않습니다`로 exit `1`(`31.8s`)했습니다. live member `1` score `1`, normal offset `2`, lag `0`, temp/backup key 0개를 보존했습니다.
- Cleanup: 본 작업이 시작한 앱 프로세스와 Compose·volume·network를 정리했고 project service 0개, port 8080 free를 확인했습니다.
