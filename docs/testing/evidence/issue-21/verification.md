# 검증 로그

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-12 | Issue #21 포인트 충전 동시성 | Level 3 | PASS | 기존 row·최초 생성 row와 독립 transaction 경계 | `PointChargeIntegrationTest`; `docs/testing/evidence/issue-21/manual-qa.md` | Testcontainers MySQL에서 기존 row와 missing row 각각 동시 충전 10건이 모두 성공해 총액을 보존했고, outer rollback 뒤에도 `REQUIRES_NEW` 충전은 commit됐습니다. 독립 QA focused 6건이 통과했습니다. |
| 2026-07-12 | Issue #21 포인트 충전 동시성 | Level 1 | PASS | 전체 회귀 | `.\gradlew.bat test --no-daemon`; `docs/testing/evidence/issue-21/commands.md` | Kafka Testcontainer startup timeout 1회 뒤 코드 변경 없는 단일 fresh rerun에서 전체 51 tests가 `BUILD SUCCESSFUL in 2m 46s`로 통과했습니다. |
