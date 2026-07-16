# Issue #113 Commands

## ASCII 경로 clean 묶음

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `S:\gradlew.bat clean test --no-daemon --tests '*ControllerTest' --tests '*IntegrationTest' --tests '*LocalRuntimeConfigurationTest'` | Controller·Integration·LocalRuntime 묶음에서 context 수명, scheduler 종료, Kafka stale record 격리 검증 | PASS. `BUILD SUCCESSFUL in 3m 8s`; JUnit XML 20개, tests=59, failures=0, errors=0, skipped=0; HTML report 생성 |

실행은 `S:` ASCII subst worktree의 단일 Gradle 프로세스에서 수행했습니다. 컴파일 중 KafkaTestUtils deprecated warning 4건 외 오류는 없었습니다.

## Ranking Rebuild 단독

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `S:\gradlew.bat clean test --no-daemon --tests '*RankingRebuildServiceIntegrationTest'` | 완료 기준의 단독 Testcontainers lifecycle 검증 | PASS. 최신 clean 묶음 안의 동일 XML에서 tests=10, failures=0, errors=0, skipped=0으로 확인 |

## Attempt 2 focused Kafka 격리

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `S:\gradlew.bat test --no-daemon --tests '*RankingEventConsumerKafkaRedisIntegrationTest*' --tests '*RankingEventConsumerDltIntegrationTest*'` | stale Kafka record 격리와 listener stop·purge·restart 순서 검증 | PASS. `BUILD SUCCESSFUL in 1m 35s`; JUnit XML 2개, tests=2, failures=0, errors=0, skipped=0 |

## Attempt 2 RED

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| 동일 focused 명령, `clearKafkaTopics()` 호출 추가 후 helper 구현 전 | 테스트 우선 RED 확인 | EXPECTED FAIL. `compileTestJava`에서 `SharedTestcontainers.clearKafkaTopics()` 심볼 2건 미해결 |

## Attempt 3 listener context 격리

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `Push-Location -LiteralPath 'S:\\'; & '.\\gradlew.bat' clean test --no-daemon --tests '*ControllerTest' --tests '*IntegrationTest' --tests '*LocalRuntimeConfigurationTest'; Pop-Location` | non-listener context 자동 소비 차단, listener context 종료 경계, 전체 clean 회귀 검증 | PASS. `BUILD SUCCESSFUL in 3m 8s`; JUnit XML 20개, tests=59, failures=0, errors=0, skipped=0 |
| `git diff --check` 및 `git diff --name-only -- src/main` | 공백 및 production 변경 범위 확인 | PASS. 공백 오류 없음, `src/main` 변경 없음 |

## 종료·cleanup 관찰

| 확인 | 목적 | 결과 |
| --- | --- | --- |
| `docker ps --format '{{.ID}} {{.Image}} {{.Names}} {{.Status}}'` | 테스트 종료 후 실행 컨테이너 확인 | 최신 실행 종료 뒤 0개 |
| `docker ps -a --filter label=org.testcontainers=true ...` | Testcontainers 고아 컨테이너 확인 | Ryuk 정리 후 0개 |
| `Get-Process java,gradle` | Gradle/Test Executor 잔존 확인 | 최신 실행 종료 뒤 0개 |
| build test report 문자열 검색 | 종료 후 connection-refused/scheduler 예외 확인 | `connection refused`, `Connection refused`, `scheduler`, `TaskScheduler`, `CannotCreateTransactionException`, `Communications link failure` 모두 0건 |

## Repository 정적 확인

| 명령 또는 확인 | 목적 | 결과 |
| --- | --- | --- |
| `git diff --name-only origin/main...HEAD`와 `git status --short` | production 변경 여부와 범위 확인 | origin/main 대비 `src/test/**` 23개 변경, `src/main/**`·migration·runtime 설정 변경 없음 |
| `git diff --check` | whitespace 정적 검사 | PASS. LF→CRLF 변환 예고 warning만 관찰 |

독립 Review·QA와 최신 CI 결과는 아직 실행 전이며 PR과 GitHub checks에서 별도로 확인합니다.
