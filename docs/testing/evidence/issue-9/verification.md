# 검증 로그

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-11 | Issue #9 Redis ranking write | Level 4 | PASS | Redis ZSET 쓰기 실제 인프라 통합 | QA actual Redis focused suite; `docs/testing/evidence/issue-9/test-output.txt` | 독립 QA가 5 tests, failures 0, errors 0, skipped 0과 `BUILD SUCCESSFUL in 1m 03s`를 확인했습니다. raw probe에서 날짜별 key, menu member 분리와 score 1·2 누적을 확인했습니다. |
| 2026-07-11 | Issue #9 Redis ranking write | Level 1 | PASS | 빌드·Unit·전체 회귀 smoke | 전체 Gradle test; `docs/testing/evidence/issue-9/test-output.txt` | 독립 QA가 전체 35 tests, failures 0, errors 0, skipped 0과 `BUILD SUCCESSFUL in 1m 17s`를 확인했습니다. |
| 2026-07-11 | Issue #9 Redis ranking write | Level 5 | PASS | 로컬 앱·인프라 기동 | `docs/testing/evidence/issue-9/manual-qa.md` | 독립 QA가 MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2와 앱을 기동했고 앱 42.982초, health HTTP 200 `UP`, Redis `PONG`을 확인했습니다. QA key는 `DEL 2`, 후속 `EXISTS 0`으로 정리했고 기존 `rag-pgvector`는 건드리지 않았습니다. Level 6은 API 변경이 없어 NO입니다. |
