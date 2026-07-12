# Issue #21 Commands

## TDD와 검증 결과

- RED: `.\gradlew.bat test --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon` → missing row 최초 동시 충전에서 `CannotAcquireLockException` / `MySQLTransactionRollbackException`, 5 tests 중 1 failure.
- 폐기한 설계 확인: 같은 focused 명령 → `insert ignore` 뒤 lock conversion 방식에서 기존 row와 missing row 동시 테스트 2 failures.
- focused Level 3: 같은 focused 명령 → 5 tests PASS, `BUILD SUCCESSFUL in 1m 21s`.
- 관련 회귀: `.\gradlew.bat test --tests com.example.coffeeordersystem.PointChargeIntegrationTest --tests com.example.coffeeordersystem.point.controller.PointControllerTest --tests com.example.coffeeordersystem.OrderPaymentIntegrationTest --no-daemon` → 14 tests PASS, `BUILD SUCCESSFUL in 1m 24s`.
- Dev 전체 회귀: `.\gradlew.bat test --no-daemon` → 50 tests, failures 0, errors 0, skipped 0, `BUILD SUCCESSFUL in 3m 7s`.

## 정적 검증

- `python scripts/harness_gate.py --issue 21 --branch codex/issue-21-point-concurrency --base-ref origin/main --check-links` → `Harness gate PASSED`.
- `git diff --check` → PASS.
- 변경 파일과 diff를 확인해 포인트 충전 service/test, 포인트 정책과 Issue #21 evidence에만 변경이 있음을 확인했습니다.
- secret 정적 검색에서 새 비밀값은 없었고 기존 `application-local.properties`의 환경변수 기본 개발값만 검색됐습니다.
