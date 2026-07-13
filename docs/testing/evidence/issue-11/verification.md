# 검증 로그

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-12 | Issue #11 Kafka retry와 DLT | Level 4 | PASS | 실제 Kafka retry 2회와 DLT 이동 | `RankingEventConsumerDltIntegrationTest`; `docs/testing/evidence/issue-11/dlt-output.txt` | Testcontainers focused 1 test와 관련 Kafka 회귀가 PASS했고, local Kafka에서도 Redis 장애 뒤 `order.completed.DLT` key·header·payload 원문을 확인했습니다. |
| 2026-07-12 | Issue #11 Kafka retry와 DLT | Level 1 | PASS | Dev 전체 회귀 | `./gradlew.bat test --no-daemon --rerun-tasks`; `docs/testing/evidence/issue-11/commands.md` | 최신 main merge 뒤 전체 48 tests, failures 0, errors 0, `BUILD SUCCESSFUL in 3m 16s`입니다. |
| 2026-07-12 | Issue #11 Kafka retry와 DLT | Level 5 | PASS | local profile 앱·MySQL·Redis·Kafka와 cleanup | `docs/testing/evidence/issue-11/manual-qa.md` | local profile 앱 12.729초 start, health 200 UP, Redis 장애 DLT 관찰 뒤 프로젝트 Compose `down -v`와 빈 `ps`를 확인했습니다. Level 6은 API 계약 변경이 없어 NO입니다. |
