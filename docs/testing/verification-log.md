# 검증 로그

완료한 Issue의 검증 결과를 계속 추가합니다.

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-09 | project bootstrap inspection | Level 1 | PASS | 전체 회귀 smoke | `./gradlew.bat test` | 문서 구조 생성 전 초기 Spring context test가 Testcontainers 기반으로 4분 13초 만에 통과했습니다. |
| 2026-07-09 | dependency and docs audit | Level 1 | PASS | 전체 회귀 smoke | `./gradlew.bat test` | Redisson starter 추가와 Testcontainers 이미지 tag 고정 후 2분 38초 만에 통과했습니다. |
| 2026-07-09 | Issue #2 project standards | Level 0 | PASS | 문서·정적 검사 | `rg -n "issue-completion-checklist|agent-mistakes|verification-log|layered-design-policy" AGENTS.md .github docs` | 공통 완료 전 체크리스트와 3계층 설계 정책 연결 지점을 확인했습니다. |
| 2026-07-09 | Issue #2 project standards | Level 1 | PASS | 빌드·Unit·전체 회귀 smoke | `./gradlew.bat test` | 변경 후 전체 Gradle 테스트가 6초 만에 통과했습니다. |
| 2026-07-09 | Issue #3 Flyway schema | Level 3 | PASS | DB·트랜잭션·락 통합 | `./gradlew.bat test --tests com.example.coffeeordersystem.DatabaseSchemaIntegrationTest` | Flyway table 생성, 메뉴 seed, JPA repository 저장/조회 검증이 1분 14초 만에 통과했습니다. |
| 2026-07-09 | Issue #3 Flyway schema | Level 1 | PASS | 빌드·Unit·전체 회귀 smoke | `./gradlew.bat test` | 전체 Gradle 테스트가 1분 21초 만에 통과했습니다. |
| 2026-07-09 | Issue #4 menu list API | Level 2 | PASS | Controller/API 계약 | `./gradlew.bat clean test --tests com.example.coffeeordersystem.menu.controller.MenuControllerTest --no-daemon` | `@WebMvcTest`와 `MockMvc`로 `GET /api/menus` 응답을 검증했고 35초 만에 통과했습니다. |
| 2026-07-09 | Issue #4 menu list API | Level 1 | PASS | 빌드·Unit·전체 회귀 smoke | `./gradlew.bat test --no-daemon` | 전체 Gradle 테스트가 1분 13초 만에 통과했습니다. |
| 2026-07-09 | Issue #5 point charge API | Level 2 | PASS | Controller/API 계약 | `./gradlew.bat clean test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon` | Controller 계약과 Testcontainers MySQL 기반 포인트 충전 DB 흐름이 1분 30초 만에 통과했습니다. |
| 2026-07-09 | Issue #5 point charge API | Level 3 | PASS | DB·트랜잭션·락 통합 | `./gradlew.bat clean test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon` | Controller 계약과 Testcontainers MySQL 기반 포인트 충전 DB 흐름이 1분 30초 만에 통과했습니다. |
| 2026-07-09 | Issue #5 point charge API | Level 1 | PASS | 빌드·Unit·전체 회귀 smoke | `./gradlew.bat test --no-daemon` | 전체 Gradle 테스트가 1분 20초 만에 통과했습니다. |
| 2026-07-09 | Issue #6 order payment API | Level 2 | PASS | Controller/API 계약 | `./gradlew.bat clean test --tests com.example.coffeeordersystem.order.controller.OrderControllerTest --tests com.example.coffeeordersystem.OrderPaymentIntegrationTest --no-daemon` | 주문 생성 Controller 계약과 동시 주문 비관적 쓰기 락 검증을 포함한 Testcontainers MySQL 기반 주문 결제 트랜잭션 테스트가 1분 38초 만에 통과했습니다. |
| 2026-07-09 | Issue #6 order payment API | Level 3 | PASS | DB·트랜잭션·락 통합 | `./gradlew.bat clean test --tests com.example.coffeeordersystem.order.controller.OrderControllerTest --tests com.example.coffeeordersystem.OrderPaymentIntegrationTest --no-daemon` | 주문 생성 Controller 계약과 동시 주문 비관적 쓰기 락 검증을 포함한 Testcontainers MySQL 기반 주문 결제 트랜잭션 테스트가 1분 38초 만에 통과했습니다. |
| 2026-07-09 | Issue #6 order payment concurrency fix | Level 3 | PASS | DB·트랜잭션·락 통합 | `./gradlew.bat test --tests com.example.coffeeordersystem.OrderPaymentIntegrationTest --no-daemon` | 같은 userId 동시 주문 focused integration test를 포함한 `OrderPaymentIntegrationTest` 5건이 1분 36초 만에 통과했습니다. Gradle HTML report는 `build/reports/tests/test/index.html`입니다. |
| 2026-07-09 | Issue #6 related point regression | Level 2 | PASS | Controller/API 계약 | `./gradlew.bat test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon` | ErrorCode 메시지 정리와 UserPoint 결제 메서드 추가 후 포인트 focused regression이 1분 46초 만에 통과했습니다. |
| 2026-07-09 | Issue #6 related point regression | Level 3 | PASS | DB·트랜잭션·락 통합 | `./gradlew.bat test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon` | ErrorCode 메시지 정리와 UserPoint 결제 메서드 추가 후 포인트 focused regression이 1분 46초 만에 통과했습니다. |
| 2026-07-09 | Issue #6 order payment API | Level 1 | PASS | 빌드·Unit·전체 회귀 smoke | `./gradlew.bat test --no-daemon` | 전체 Gradle 테스트가 1분 17초 만에 통과했습니다. |
| 2026-07-10 | Issue #23 harness quality gates | Level 0 | PASS | 문서·정적·하네스·도구 | `python -m unittest discover -s scripts/tests -p "test_*.py"` | branch 보호, Level 5/6 결정, Attempt 연결, evidence 내용, verification log, Markdown 링크 검사 16건이 통과했습니다. |
| 2026-07-10 | Issue #23 harness repository gate | Level 0 | PASS | 문서·정적·하네스·도구 | `python scripts/harness_gate.py --issue 23 --base-ref origin/main --check-links` | Issue evidence와 변경 Markdown 상대 링크를 확인했습니다. |
| 2026-07-10 | Issue #23 Git hooks | Level 0 | PASS | 문서·정적·하네스·도구 | `git hook run pre-commit`, `git hook run pre-push` | Issue branch에서 branch guard와 pre-push harness gate가 통과했습니다. `main` 입력은 의도대로 종료 코드 1을 반환했습니다. |
| 2026-07-10 | Issue #23 Java compile | Level 1 | PASS | 빌드 | `.\gradlew.bat compileJava --no-daemon` | Java production 코드를 변경하지 않은 상태에서 컴파일이 22초에 통과했습니다. |
| 2026-07-10 | Issue #23 full regression | Level 1 | PASS | 빌드·Unit·전체 회귀 smoke | `.\gradlew.bat test --no-daemon` | 최초 Docker daemon 미가동 실패 후 daemon을 시작하고 동일 명령을 재실행해 1분 48초에 통과했습니다. |
| 2026-07-10 | Issue #23 final regression | Level 1 | PASS | 빌드·Unit·전체 회귀 smoke | `.\gradlew.bat test --no-daemon` | 독립 Review 수정 후 전체 Gradle 테스트가 31초에 통과했습니다. |
| 2026-07-10 | Issue #23 coordinator-only follow-up | Level 0 | PASS | 문서·정적·하네스·도구 | QA Agent가 하네스 테스트, repository gate, diff check 실행 | 하네스 17건과 repository gate가 통과했고 Review Agent가 Main 비실행 역할 경계를 확인했습니다. |
| 2026-07-10 | Issue #23 adaptive orchestration | Level 0 | PASS | 문서·정적·하네스·도구 | QA Agent의 28건 테스트, PR body validation, harness, diff, YAML 검증과 Reviewer 확인 | adaptive SOLO/STANDARD/STRICT 생성, Dev stalled replacement, runtime Skill gate의 exact slot wording을 확인했습니다. Java 변경은 없습니다. |
| 2026-07-10 | Issue #25 verification level gate | Level 0 | PASS | 문서·정적·하네스 | `python -m unittest discover -s scripts/tests -p "test_*.py"`, `py_compile`, `git diff --check` | Final pre-evidence QA가 unittest 45건 PASS, py_compile PASS, diff check PASS를 확인했습니다. 최종 repository gate와 CI는 아직 미검증입니다. |
