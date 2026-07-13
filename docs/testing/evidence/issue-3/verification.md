# 검증 로그

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-09 | Issue #3 Flyway schema | Level 3 | PASS | DB·트랜잭션·락 통합 | `./gradlew.bat test --tests com.example.coffeeordersystem.DatabaseSchemaIntegrationTest` | Flyway table 생성, 메뉴 seed, JPA repository 저장/조회 검증이 1분 14초 만에 통과했습니다. |
| 2026-07-09 | Issue #3 Flyway schema | Level 1 | PASS | 빌드·Unit·전체 회귀 smoke | `./gradlew.bat test` | 전체 Gradle 테스트가 1분 21초 만에 통과했습니다. |
