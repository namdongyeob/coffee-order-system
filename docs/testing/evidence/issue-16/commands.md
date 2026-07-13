# 실행 명령

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `./gradlew.bat test --tests com.example.coffeeordersystem.OrderRepositoryQuerydslIntegrationTest --no-daemon` | QueryDSL repository와 MySQL EXPLAIN focused Level 3 검증 | PASS. 2 tests, failures 0, errors 0. |
| `./gradlew.bat test --no-daemon` | 의존성·repository 변경 뒤 전체 Level 1 회귀 smoke | PASS. 24 suites, 68 tests, failures 0, errors 0, skipped 0. |
| `git diff --check` | 공백 오류 확인 | PASS. |
| `python scripts/harness_gate.py --issue 16 --check-links --include-worktree` | Issue evidence 및 변경 Markdown 링크 preflight | PASS. |
