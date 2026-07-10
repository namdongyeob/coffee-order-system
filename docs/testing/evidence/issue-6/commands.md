# Issue #6 Commands

## Fix Agent Focused Integration Recheck

```powershell
.\gradlew.bat test --tests com.example.coffeeordersystem.OrderPaymentIntegrationTest --no-daemon
```

- 목적: Review Agent P2 대응으로 추가한 같은 `userId` 동시 주문 focused integration test를 포함해 주문 결제 DB 트랜잭션과 비관적 쓰기 락 경로를 재검증.
- 포함된 신규 검증: 잔액 4,500인 사용자에게 메뉴 1 주문 2건을 동시에 시작했을 때 성공 1건, `INSUFFICIENT_POINT` 1건, 최종 잔액 0, 주문 저장 +1건을 확인.
- 결과: PASS.
- 소요 시간: 1m 36s.
- Gradle HTML report: `build/reports/tests/test/index.html`.
- Raw output 전체는 첨부하지 않고, 필요 시 위 HTML report와 `build/test-results/test/TEST-com.example.coffeeordersystem.OrderPaymentIntegrationTest.xml`에서 재현 결과를 확인합니다.

## Focused Controller + DB Integration

```powershell
.\gradlew.bat clean test --tests com.example.coffeeordersystem.order.controller.OrderControllerTest --tests com.example.coffeeordersystem.OrderPaymentIntegrationTest --no-daemon
```

- 목적: 주문 생성 Controller 계약과 Testcontainers MySQL 기반 주문 결제 DB 트랜잭션 검증.
- 결과: PASS.
- 소요 시간: 1m 38s.
- 범위: `POST /api/orders` 201 응답, `MENU_NOT_FOUND` 404, `USER_POINT_NOT_FOUND` 404, `INSUFFICIENT_POINT` 409, 포인트 차감과 주문 저장 트랜잭션 정합성, 같은 `userId` 동시 주문 시 비관적 쓰기 락 기반 이중 차감 방지.

## Related Focused Regression

```powershell
.\gradlew.bat test --tests com.example.coffeeordersystem.point.controller.PointControllerTest --tests com.example.coffeeordersystem.PointChargeIntegrationTest --no-daemon
```

- 목적: `ErrorCode` 메시지 UTF-8 정리와 `UserPoint` 변경이 기존 포인트 충전 범위를 깨지 않는지 확인.
- 결과: PASS.
- 소요 시간: 1m 46s.

## Coordinator Full Smoke

```powershell
.\gradlew.bat test --no-daemon
```

- 목적: Issue #6 변경이 기존 Flyway schema, 메뉴 조회, 포인트 충전 테스트를 깨지 않는지 확인.
- 결과: PASS.
- 소요 시간: 1m 17s.

## Coordinator Final Recheck

- Fix Agent는 요청 범위에 따라 전체 `.\gradlew.bat test --no-daemon`을 실행하지 않았습니다.
- Coordinator가 전체 테스트, Controller focused test 포함 여부, production code diff 미변경 여부를 재확인했습니다.
