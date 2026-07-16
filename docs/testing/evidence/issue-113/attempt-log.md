# Issue Attempt Log

Issue: #113
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/113
Branch: issue-113
Current disposition: PASS
Current Attempt: 2
Current head: c1d1b28

## Attempt 1

### Generate

- `SharedTestcontainers`에 Kafka·MySQL·Redis Testcontainers singleton과 조건부 `start()`를 추가했습니다.
- `TestcontainersConfiguration` bean의 destroy method를 비활성화하고 모든 통합 테스트 context가 singleton을 사용하도록 연결했습니다.
- scheduled task가 테스트 인프라 종료 뒤 실행되지 않도록 test-only `DisabledTaskScheduler`와 configuration·unit test를 추가했습니다.
- 변경 파일은 `src/test/**` 7개뿐이며 production 코드는 변경하지 않았습니다.

### Evaluate

- PASS. ASCII subst 경로의 clean 묶음 실행은 `BUILD SUCCESSFUL in 3m 11s`, XML 19개, tests=57, failures=0, errors=0, skipped=0이었습니다.
- PASS. 같은 head의 clean 단독 `RankingRebuildServiceIntegrationTest`는 `BUILD SUCCESSFUL in 1m 42s`, tests=10, failures=0, errors=0, skipped=0이었습니다.
- 두 실행 모두 종료 후 Java/Gradle와 Testcontainers 잔여 컨테이너가 0개였습니다.

### Failure Cause

- 현재 Attempt의 테스트 실패는 없습니다. 선행 #120에서 확인된 원인은 Spring context마다 컨테이너를 새로 생성해 누적하고, 종료 순서 뒤 scheduler·Kafka producer가 종료된 인프라에 재접속한 lifecycle 간섭입니다.

### Change Scope

- 허용 범위는 `src/test/**`의 Testcontainers 수명과 scheduler 실행 제어입니다. `src/main/**`, migration, runtime 설정, DLT replay와 정상 consumer production 코드는 수정하지 않았습니다.

### Reverification

- `S:\gradlew.bat clean test --no-daemon --tests '*ControllerTest' --tests '*IntegrationTest' --tests '*LocalRuntimeConfigurationTest'` — PASS, XML 19개, 57/0/0/0.
- `S:\gradlew.bat clean test --no-daemon --tests '*RankingRebuildServiceIntegrationTest'` — PASS, XML 1개, 10/0/0/0.
- 각 실행 종료 뒤 `docker ps`, Testcontainers label 조회, Java/Gradle process 조회 — 모두 0개.
- build test report에서 `connection refused`, `Connection refused`, `scheduler`, `TaskScheduler`, `CannotCreateTransactionException`, `Communications link failure` — 0건.
- `git diff --cached --check`와 production 변경 범위 확인 — PASS.

### Next Attempt

없음. Dev 단계는 종료했으며 독립 Review·QA와 최신 CI는 GitHub 정본에서 후속 확인합니다.

## Attempt 2

### Generate

- 이전 묶음에서 재현된 `RankingEventConsumerDltIntegrationTest`·`RankingEventConsumerKafkaRedisIntegrationTest`의 stale Kafka record 문제만 test-only 범위로 격리했습니다.
- 먼저 두 테스트의 `@BeforeEach`에 `SharedTestcontainers.clearKafkaTopics()` 호출을 추가하고 helper를 아직 구현하지 않은 상태에서 focused 명령을 실행해 `compileTestJava` RED를 확인했습니다.
- `SharedTestcontainers`에 `order.completed`·`order.completed.DLT`의 latest offset 이전 레코드를 AdminClient로 삭제하는 `clearKafkaTopics()`를 추가했습니다.
- 두 ranking consumer 테스트는 listener stop → topic purge → DB/Redis 정리 → listener restart·assignment 대기의 순서를 사용해 이전 레코드와 in-flight 처리를 격리합니다.
- production 코드와 #112, DLT replay production 코드는 변경하지 않았습니다.

### Evaluate

- PASS. RED focused 실행은 helper 미구현으로 `clearKafkaTopics()` 심볼 2건의 compileTestJava 실패를 관찰했습니다.
- PASS. ASCII subst 경로 focused 실행은 `BUILD SUCCESSFUL in 1m 35s`, XML 2개, tests=2, failures=0, errors=0, skipped=0입니다.
- PASS. ASCII subst 경로 clean 묶음 실행은 `BUILD SUCCESSFUL in 2m 31s`, XML 20개, tests=58, failures=0, errors=0, skipped=0입니다.

### Failure Cause

- Attempt 1의 stale record는 공유 Kafka topic에 이전 테스트 레코드가 남고 listener가 다음 테스트 fixture 정리와 경쟁한 것이 원인이었습니다. 이번 Attempt는 해당 두 테스트에서 listener를 먼저 중지하고 topic을 purge한 뒤 재시작합니다.

### Change Scope

- 허용 범위는 `src/test/**`의 공유 Testcontainers Kafka 정리와 ranking consumer 테스트 격리입니다. `src/main/**`, migration, runtime 설정, DLT replay와 정상 consumer production 코드는 수정하지 않았습니다.

### Reverification

- `S:\gradlew.bat test --no-daemon --tests '*RankingEventConsumerKafkaRedisIntegrationTest*' --tests '*RankingEventConsumerDltIntegrationTest*'` — PASS, XML 2개, tests=2, failures=0, errors=0, skipped=0.
- `S:\gradlew.bat clean test --no-daemon --tests '*ControllerTest' --tests '*IntegrationTest' --tests '*LocalRuntimeConfigurationTest'` — PASS, XML 20개, tests=58, failures=0, errors=0, skipped=0.
- clean 실행 후 `docker ps`, Testcontainers label 조회, Java/Gradle process 조회 — 모두 0개.
- build test report에서 `connection refused`, `connection-refused`, `failed to connect`, scheduler exception, `CannotCreateTransactionException`, `Communications link failure` — 0건.
- `git diff --cached --check`와 production 변경 범위 확인 — PASS. 변경은 `src/test/**` 5개입니다.

### Next Attempt

없음. Dev 단계는 종료했으며 독립 Review·QA와 최신 CI는 GitHub 정본에서 후속 확인합니다.
