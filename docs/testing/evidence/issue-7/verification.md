# 검증 로그

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-11 | Issue #7 Redisson user lock | Level 4 | PASS | Redis·분산락 통합 | `RedisOrderLockIntegrationTest`; `docs/testing/evidence/issue-7/test-output.txt` | 독립 QA가 실제 `redis:7.4.2`에서 1 test, failures 0, errors 0을 확인했습니다. BUILD SUCCESSFUL 1분 15초, test 53.107초입니다. |
| 2026-07-11 | Issue #7 Redisson user lock | Level 1 | PASS | 빌드·Unit·전체 회귀 smoke | 전체 Gradle test; `docs/testing/evidence/issue-7/test-output.txt` | 독립 QA가 전체 25 tests, failures 0, errors 0, skipped 0을 확인했습니다. BUILD SUCCESSFUL 1분 23초입니다. |
| 2026-07-11 | Issue #7 Redisson user lock | Level 5 | PASS | 로컬 앱·인프라 기동 | `docs/testing/evidence/issue-7/manual-qa.md` | MySQL 8.4.10과 Redis 7.4.2, Flyway migration 4개, Redisson `127.0.0.1:16379`, 앱 14.961초 기동과 health 200 `UP`을 독립 QA가 확인하고 리소스를 정리했습니다. |
| 2026-07-11 | Issue #7 Redisson user lock | Level 6 | PASS | 실제 HTTP·DB·락 경합 | `docs/testing/evidence/issue-7/manual-qa.md` | 충전 200, 정상 주문 201/169ms와 잔액 10000→5500·PAID 1건을 확인했습니다. 동일 락 선점 요청은 409 `ORDER_LOCK_NOT_ACQUIRED`/2066ms, 동시 2요청은 409/409(2064/2071ms), DB 불변이었고 QA 리소스를 정리했습니다. |
