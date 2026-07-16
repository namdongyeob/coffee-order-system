# Issue #113 Acceptance Criteria

Issue: #113
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/113

Execution mode: STRICT
Execution mode reason: DB·Kafka·Redis Testcontainers와 scheduler 수명 경계를 변경하는 테스트 인프라 작업입니다.
Level 5 required: NO
Level 5 reason: 테스트 프로세스의 인프라 수명 문제를 고치며 로컬 애플리케이션 기동 자체는 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: 실제 HTTP API 동작이나 계약을 변경하지 않습니다.

## Dev 단계 완료 기준

- [x] 기존 묶음 실행의 context별 컨테이너 누적과 종료 후 scheduled task 접근 원인을 #120 관찰과 연결해 기록했습니다.
- [x] `SharedTestcontainers`로 Kafka·MySQL·Redis 수명을 공유하고 Spring bean destroy를 막았습니다.
- [x] 테스트 context에서 scheduled task가 실행되지 않도록 test-only scheduler를 적용했습니다.
- [x] production 업무 로직, Kafka·Redis 복구 정책, DLT replay 코드는 변경하지 않았습니다.
- [x] clean 묶음 실행과 `RankingRebuildServiceIntegrationTest` 단독 실행이 PASS했습니다.
- [x] 공유 Kafka를 사용하는 ranking consumer 테스트가 listener 중지·topic purge·재시작 순서로 이전 레코드와 in-flight 처리를 격리합니다.
- [x] 테스트 종료 뒤 Java/Gradle 및 Testcontainers 잔여 프로세스·컨테이너가 없고 connection-refused/scheduler 오류가 관찰되지 않았습니다.
- [x] `git diff --check`와 변경 범위 검사를 통과했습니다.

## STRICT 후속 게이트

Dev evidence와 draft PR은 독립 Review, 독립 QA, 최신 GitHub Actions `quality-gates` 실행을 시작하기 위한 입력입니다. Review·QA·CI 판정과 현재 head·mergeability는 저장소 파일에 snapshot으로 복제하지 않고 GitHub 정본에서 확인합니다.
