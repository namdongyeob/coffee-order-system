# 검증 로그

완료한 Issue의 검증 결과를 계속 추가합니다.

| 날짜 | Issue | Level | 명령 또는 확인 | 결과 | 비고 |
| --- | --- | --- | --- | --- | --- |
| 2026-07-09 | project bootstrap inspection | Level 1 smoke | `./gradlew.bat test` | PASS | 문서 구조 생성 전 초기 Spring context test가 Testcontainers 기반으로 4분 13초 만에 통과했습니다. |
| 2026-07-09 | dependency and docs audit | Level 1 smoke | `./gradlew.bat test` | PASS | Redisson starter 추가와 Testcontainers 이미지 tag 고정 후 2분 38초 만에 통과했습니다. |
| 2026-07-09 | Issue #2 project standards | Level 0 docs | `rg -n "issue-completion-checklist|agent-mistakes|verification-log|layered-design-policy" AGENTS.md .github docs` | PASS | 공통 완료 전 체크리스트와 3계층 설계 정책 연결 지점을 확인했습니다. |
| 2026-07-09 | Issue #2 project standards | Level 1 smoke | `./gradlew.bat test` | PASS | 변경 후 전체 Gradle 테스트가 6초 만에 통과했습니다. |
| 2026-07-09 | Issue #3 Flyway schema | Level 3 DB Integration | `./gradlew.bat test --tests com.example.coffeeordersystem.DatabaseSchemaIntegrationTest` | PASS | Flyway table 생성, 메뉴 seed, JPA repository 저장/조회 검증이 1분 14초 만에 통과했습니다. |
| 2026-07-09 | Issue #3 Flyway schema | Level 1 smoke | `./gradlew.bat test` | PASS | 전체 Gradle 테스트가 1분 21초 만에 통과했습니다. |
| 2026-07-09 | Issue #4 menu list API | Level 2 Controller | `./gradlew.bat clean test --tests com.example.coffeeordersystem.menu.controller.MenuControllerTest --no-daemon` | PASS | `@WebMvcTest`와 `MockMvc`로 `GET /api/menus` 응답을 검증했고 35초 만에 통과했습니다. |
| 2026-07-09 | Issue #4 menu list API | Level 1 smoke | `./gradlew.bat test --no-daemon` | PASS | 전체 Gradle 테스트가 1분 13초 만에 통과했습니다. |
| 2026-07-09 | Issue #5 point charge API | Level 2 + Level 3 focused | `./gradlew.bat clean test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon` | PASS | Controller 계약과 Testcontainers MySQL 기반 포인트 충전 DB 흐름이 1분 30초 만에 통과했습니다. |
| 2026-07-09 | Issue #5 point charge API | Level 1 smoke | `./gradlew.bat test --no-daemon` | PASS | 전체 Gradle 테스트가 1분 20초 만에 통과했습니다. |
| 2026-07-09 | Issue #6 order payment API | Level 2 + Level 3 focused | `./gradlew.bat clean test --tests com.example.coffeeordersystem.order.controller.OrderControllerTest --tests com.example.coffeeordersystem.OrderPaymentIntegrationTest --no-daemon` | PASS | 주문 생성 Controller 계약과 동시 주문 비관적 쓰기 락 검증을 포함한 Testcontainers MySQL 기반 주문 결제 트랜잭션 테스트가 1분 38초 만에 통과했습니다. |
| 2026-07-09 | Issue #6 order payment concurrency fix | Level 3 focused | `./gradlew.bat test --tests com.example.coffeeordersystem.OrderPaymentIntegrationTest --no-daemon` | PASS | 같은 userId 동시 주문 focused integration test를 포함한 `OrderPaymentIntegrationTest` 5건이 1분 36초 만에 통과했습니다. Gradle HTML report는 `build/reports/tests/test/index.html`입니다. |
| 2026-07-09 | Issue #6 related point regression | Level 2 + Level 3 focused | `./gradlew.bat test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon` | PASS | ErrorCode 메시지 정리와 UserPoint 결제 메서드 추가 후 포인트 focused regression이 1분 46초 만에 통과했습니다. |
| 2026-07-09 | Issue #6 order payment API | Level 1 smoke | `./gradlew.bat test --no-daemon` | PASS | 전체 Gradle 테스트가 1분 17초 만에 통과했습니다. |
