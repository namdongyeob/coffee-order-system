# Issue #120 Commands

Issue: #120
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/120
Execution head: 37d410f71bcdd86ef1af02e7c807b4401bfcb927

| 구분 | 명령 또는 확인 | 결과 |
| --- | --- | --- |
| pre-fix exact bundle | `T:\gradlew.bat clean test --no-daemon --tests '*ControllerTest' --tests '*IntegrationTest' --tests '*LocalRuntimeConfigurationTest'` | PASS, `BUILD SUCCESSFUL in 4m 32s`, XML 19개, 57/0/0/0 |
| current exact bundle | `W:\gradlew.bat clean test --no-daemon --tests '*ControllerTest' --tests '*IntegrationTest' --tests '*LocalRuntimeConfigurationTest'` | PASS, `BUILD SUCCESSFUL in 2m 49s`, XML 22개, 87/0/0/0 |
| worker 관찰 | `Get-CimInstance Win32_Process`, `Get-Process java` | pre-fix daemon PID 15852, Test Executor PID 20788. current daemon PID 28700, Test Executor PID 28116 |
| 결과 생성 관찰 | `Get-ChildItem <drive>:\build\test-results\test -Filter '*.xml'`, HTML 존재 확인 | 실행 중 XML 0/HTML 없음, worker 종료 후 모두 생성 |
| Docker 관찰 | `docker ps --format ...`, `docker ps -a --filter label=org.testcontainers=true` | pre-fix 최대 15개, current 4개 고정, 양쪽 최종 0개 |
| pre-fix dump | `jcmd 20788 Thread.print -l` | PASS, 115,619 bytes, 90 threads. shutdown hook/scheduler/Hikari/Kafka 대기 경계 확인 |
| current 종료 문자열 | build report에서 connection refused, DB timeout, scheduled task error, communications failure 검색 | 0건 |
| recent change | `git diff 4d1b42d..159e643 -- src/test/**`, `git diff 159e643..37d410f -- SharedTestcontainers.java` | #113 shared lifecycle/scheduler 변경과 #112 private monitor 제거 확인 |
| current main CI | PR #123 `statusCheckRollup` | `quality-gates` SUCCESS, source `c38e2e0`, merge commit `37d410f` |

## 무출력 구간 판정

- pre-fix 151초 시점 Test Executor CPU는 235.1초였고 최신 container가 3~7초 전에 생성되어 있었으므로 이 시점은 hang이 아닌 context 전환으로 판정했습니다.
- container가 Ryuk 1개로 줄고 shutdown 로그가 시작됐지만 worker가 종료되지 않은 시점에만 thread dump를 수집했습니다.
- dump 후 3초 내에 scheduler의 30.002초 DB connection timeout이 발생했고 바로 worker가 종료되어 고착 경계를 확정했습니다.

## Raw artifact 정책

raw thread dump는 `%TEMP%\issue-120-prefx-20788-thread-dump.txt`에서만 검토했습니다. 필요 stack과 수치는 `attempt-log.md`와 `manual-qa.md`에 요약했고 raw 프로세스 자료는 Git에 커밋하지 않습니다.
