# Issue #5 검증 명령

| 순서 | 명령 | 목적 | 결과 |
| --- | --- | --- | --- |
| 1 | `./gradlew.bat test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --no-daemon` | RED 확인 | FAIL. 포인트 Controller, Service, DTO 미구현으로 `compileTestJava`가 실패했습니다. |
| 2 | `./gradlew.bat test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --no-daemon` | Level 2 Controller focused test | PASS. `POST /api/points/charge` 성공 응답과 request validation 에러 포맷을 확인했습니다. |
| 3 | `./gradlew.bat test --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon` | Level 3 DB Integration focused test | FAIL. Repository 메서드명이 파생 쿼리로 해석되어 `PropertyReferenceException`이 발생했습니다. |
| 4 | `./gradlew.bat test --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon` | Level 3 DB Integration focused test 재실행 | PASS. row 생성, 기존 잔액 누적, 최대 충전 금액 초과 거부를 확인했습니다. |
| 5 | `./gradlew.bat test --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon --stacktrace` | Gradle result store 재현 | FAIL. 이전 병렬 재검증 중 생성된 `build/test-results/test/binary` 상태를 읽다가 `SerializableTestResultStore.hasResults()`에서 `EOFException`이 발생했습니다. |
| 6 | `./gradlew.bat clean test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon` | Level 2 + Level 3 최종 focused 재검증 | PASS. Controller 계약과 Testcontainers MySQL 기반 포인트 충전 DB 흐름이 1분 30초 만에 통과했습니다. |
| 7 | `./gradlew.bat test --no-daemon` | 전체 테스트 smoke | PASS. 전체 Gradle 테스트가 1분 20초 만에 통과했습니다. |
