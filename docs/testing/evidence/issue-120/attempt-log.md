# Issue Attempt Log

Issue: #120
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/120
Branch: codex/issue-120-hang-diagnosis
Current disposition: PASS
Current Attempt: 1
Current head: 37d410f71bcdd86ef1af02e7c807b4401bfcb927

## Attempt 1

### Generate

- Generate start: 2026-07-16T23:53:01+09:00.
- #113 evidence의 Controller·Integration·LocalRuntime clean 명령을 pre-fix `4d1b42d`와 current main `37d410f`에서 각각 ASCII subst 경로로 실행했습니다.
- 무출력 구간에서 Gradle daemon·Test Executor PID/CPU, JUnit 결과 생성 여부, Docker container 상태를 반복 관찰했습니다.
- pre-fix shutdown 대기가 시작된 후 `jcmd 20788 Thread.print -l`로 2026-07-16T23:57:29+09:00에 thread dump를 수집했습니다.

### Evaluate

- pre-fix는 `BUILD SUCCESSFUL in 4m 32s`, XML 19개, 57 tests, failures/errors/skipped 0으로 최종 성공했습니다.
- pre-fix는 실행 중 Ryuk을 포함해 container가 최대 15개까지 누적됐고, 종료 직전까지 XML 0·HTML 미생성 상태였습니다.
- pre-fix thread dump에서 `Test worker`는 `ApplicationShutdownHooks.runHooks()`의 thread join을 기다렸고, `SpringApplicationShutdownHook`은 `DefaultLifecycleProcessor$LifecycleGroup.stop()`의 latch를 기다렸습니다.
- 같은 dump의 `scheduling-1`은 `OutboxEventPublisher.publishPending()`에서 이미 종료된 Hikari/MySQL connection을 기다렸고, Kafka producer network thread는 종료된 broker로 재연결하고 있었습니다.
- pre-fix scheduler는 30.002초 connection timeout 후 `CommunicationsException`/`Connection refused`를 남겨야 shutdown을 끝냈습니다.
- current main은 `BUILD SUCCESSFUL in 2m 49s`, XML 22개, 87 tests, failures/errors/skipped 0으로 성공했습니다.
- current main은 Ryuk + Kafka/MySQL/Redis 1세트, 총 4개 container를 유지했고 shutdown에서 connection-refused, DB timeout, scheduled task 예외가 0건이었습니다.
- 두 실행 모두 종료 후 Java/Gradle 프로세스와 Testcontainers가 0개였습니다.

### Failure Cause

- 원인은 Gradle control connection, JDK 17, Windows 경로 또는 고아 worker가 아닙니다.
- pre-fix `TestcontainersConfiguration`이 Spring context마다 Kafka·MySQL·Redis를 새로 만들어 기동 비용과 container 수명이 누적됐습니다.
- 종료 순서에서 scheduled Outbox task와 Kafka producer가 이미 종료된 DB·broker에 접근해 Spring shutdown hook과 Gradle Test worker 종료를 최대 30초 지연했습니다.
- Gradle이 테스트 worker 종료 후 XML/HTML을 완성하므로, 이 구간이 사용자에게는 `결과 없음` hang으로 보였습니다.

### Change Scope

- #120의 Git 변경은 `docs/testing/evidence/issue-120/**` 6개 문서뿐입니다.
- #113은 `SharedTestcontainers`, bean `destroyMethod = ""`, `DisabledTaskSchedulerConfiguration`, listener context 격리로 원인을 이미 해결했습니다.
- current main은 #112에서 `clearSharedKafkaTopics()`의 불필요한 private `synchronized`도 제거해 fresh shared-container start의 모니터 재진입 위험을 막은 상태입니다.

### Reverification

- Reverification end: 2026-07-17T00:01:11+09:00.
- pre-fix 정확 명령: PASS, 57/0/0/0, 4m 32s, shutdown stall 재현.
- current main 동일 명령: PASS, 87/0/0/0, 2m 49s, shutdown stall 미재현.
- 양쪽 실행 종료 후 Java/Gradle·Testcontainers cleanup: PASS, 잔여 0개.
- PR #123 source head `c38e2e0` `quality-gates`: SUCCESS. 이 source head를 merge한 current main은 `37d410f`입니다.

### Next Attempt

없음. #120에서 추가 코드 수정이나 후속 Issue 분리가 필요하지 않습니다.
