# Issue #120 Acceptance Criteria

Issue: #120
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/120

Execution mode: STRICT
Execution mode reason: Testcontainers, Gradle test worker, Spring context와 scheduler 수명 경계를 재현·분류하는 테스트 인프라 진단 작업입니다.
Level 5 required: NO
Level 5 reason: 로컬 애플리케이션 기동이 아니라 test runtime의 수명 경계 원인 분류가 목표입니다.
Level 6 required: NO
Level 6 reason: HTTP API 계약을 변경하거나 검증하지 않습니다.

## 완료 기준

- [x] pre-fix `4d1b42d` clean 단일 실행에서 장시간 무결과 구간과 정상 종료를 모두 관찰했습니다.
- [x] Gradle daemon·Test Executor PID, CPU, JUnit XML·HTML, Docker container 상태를 같은 시점에 수집했습니다.
- [x] shutdown 대기 시점의 thread dump로 `Test worker` → Spring shutdown hook → scheduler → 종료된 MySQL 경계를 확인했습니다.
- [x] 최신 main `37d410f`에서 동일 clean 명령이 공유 container 1세트로 정상 종료하는 것을 확인했습니다.
- [x] 원인을 Gradle/JDK/Windows가 아닌 pre-fix test configuration의 Testcontainers 누적과 scheduler shutdown 간섭으로 분류했습니다.
- [x] #113이 이미 해결한 범위로 판정했고, #120에서 production·test·config 코드 수정이나 추가 후속 Issue가 필요하지 않습니다.
- [x] evidence 6종과 현재 main으로 merge된 PR #123 source head의 `quality-gates` SUCCESS 근거가 있습니다.

## 범위 잠금

- `src/main/**`, `src/test/**`, runtime·workflow·build configuration은 변경하지 않습니다.
- raw thread dump는 115,619 bytes의 임시 진단 자료로만 보관했고, 비밀·개인정보를 불필요하게 Git에 포함하지 않았습니다.
