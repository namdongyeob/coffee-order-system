# Issue #6 Manual QA

## 실행한 확인

- 자동화된 focused Controller test와 DB integration test로 API 계약, 에러 매핑, 포인트 차감, 주문 저장, 실패 시 잔액 유지와 주문 미저장을 확인했습니다.
- Fix Agent focused integration recheck에서 같은 `userId` 동시 주문 2건을 동시에 시작해 DB 비관적 쓰기 락 기반 포인트 차감이 이중 차감을 막는지 확인했습니다.
- 신규 동시성 검증 결과는 성공 1건, `INSUFFICIENT_POINT` 1건, 최종 잔액 0, 주문 저장 +1건입니다.

## 미실행 확인

- 로컬 서버 기동 후 `curl` 또는 Postman으로 실제 `POST /api/orders`를 호출하는 Level 5/6 확인은 실행하지 않았습니다.
- 전체 `.\gradlew.bat test --no-daemon` smoke test는 Main/Coordinator가 실행해 PASS를 확인했습니다.
- Fix Agent는 요청 범위에 따라 전체 테스트를 다시 실행하지 않았고, 이후 Coordinator가 전체 테스트를 최종 재실행해 PASS를 확인했습니다.
