# Issue #5 수동 QA

## 수행하지 않은 항목

- 로컬 애플리케이션 기동 후 실제 HTTP 호출은 수행하지 않았습니다.
- Postman 또는 curl 산출물은 생성하지 않았습니다.
- 충전 동시성 부하 검증은 수행하지 않았습니다.
- 동일 `userId`의 최초 `UserPoint` row 생성 동시성 correctness 테스트는 수행하지 않았습니다.

## 자동 검증으로 대체한 범위

- Controller 계약은 `PointControllerTest`의 MockMvc 요청과 응답 JSON으로 확인했습니다.
- DB 저장과 조회 흐름은 `PointChargeIntegrationTest`의 Testcontainers MySQL 기반 통합 테스트로 확인했습니다.

## 서브에이전트 검토 반영

- Dev Agent는 동일 `userId` 최초 row 생성 동시성 검증이 미실행 상태라고 보고했습니다.
- Review Agent는 해당 항목을 이번 Issue의 승인 차단 결함이 아니라 follow-up 후보로 판단했습니다.
- QA Agent는 병렬 재검증 중 Gradle test result binary 오류가 재현되어 evidence 보강이 필요하다고 판단했습니다.
- Coordinator가 병렬 테스트 종료 후 `clean` 기반 focused 묶음 테스트와 전체 smoke test를 단일 실행으로 재검증했습니다.
