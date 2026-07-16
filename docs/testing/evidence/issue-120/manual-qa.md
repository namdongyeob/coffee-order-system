# Issue #120 Manual QA

Issue: #120
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/120
Date: 2026-07-16~2026-07-17

## 관찰 환경

- Windows PowerShell에서 한글 worktree를 pre-fix `T:`와 current `W:` ASCII subst로 매핑했습니다.
- 두 실행 모두 같은 Gradle wrapper, JDK 17 test worker, Docker Desktop 환경을 사용했습니다.
- pre-fix는 commit `4d1b42d`, current는 `37d410f`이며 두 worktree는 source 수정 없이 clean 명령만 실행했습니다.

## Pre-fix 관찰

- `:test` 시작 후 JUnit XML과 HTML은 worker 종료 전까지 생성되지 않았습니다.
- Test Executor PID 20788의 CPU가 지속 증가하고 Kafka·MySQL·Redis container가 context 전환마다 추가돼 초반 무출력 구간은 진행 중으로 판정했습니다.
- container는 Ryuk을 포함해 최대 15개까지 관찰됐습니다.
- 23:57:02에 application shutdown이 시작되고 application container가 종료됐지만 Test Executor는 계속 살아 있었습니다.
- 23:57:29 thread dump에서 `Test worker` → `SpringApplicationShutdownHook` → `scheduling-1` 연결을 확인했습니다.
- `scheduling-1`은 `OutboxEventPublisher.publishPending()`의 Hikari connection을 기다렸고, 23:57:32에 30.002초 timeout·`Communications link failure`·`Connection refused`를 남겼습니다.
- 이후 Kafka producer와 Hikari shutdown이 끝나고 `BUILD SUCCESSFUL in 4m 32s`, 57/57 PASS로 종료했습니다.

## Current main 관찰

- 전체 실행 동안 Ryuk + 공유 Kafka/MySQL/Redis 1세트, 총 4개 container만 유지됐습니다.
- Test Executor PID 28116의 CPU는 실행 중 지속 증가했고 context별 container 누적은 없었습니다.
- 00:01:07.643~00:01:07.737 사이에 Hikari context·Kafka producer 종료 로그가 순차적으로 끝났습니다.
- connection refused, DB connection timeout, unexpected scheduled task error, communications failure는 0건이었습니다.
- `BUILD SUCCESSFUL in 2m 49s`, 87/87 PASS로 종료했고 XML 22개와 HTML report가 생성됐습니다.

## Cleanup receipt

- pre-fix 종료 후 Java/Gradle 프로세스 0, 실행 Docker container 0, `org.testcontainers=true` container 0을 확인했습니다.
- current 종료 후에도 같은 항목이 모두 0이었습니다.
- 실험 PID를 수동 종료하거나 Docker container를 강제 정리하지 않았습니다.

## 판정

pre-fix 증상은 무한 hang이 아니라 container 누적으로 느려진 실행 뒤, 종료된 DB에 접근하는 scheduler의 30초 timeout이 worker shutdown을 붙잡는 유한 shutdown stall입니다. #113 변경이 current main에서 해당 경계를 제거했으므로 #120의 추가 코드 수정은 필요하지 않습니다.
