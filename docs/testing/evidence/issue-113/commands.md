# Issue #113 Commands

## ASCII 경로 clean 묶음

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `S:\gradlew.bat clean test --no-daemon --tests '*ControllerTest' --tests '*IntegrationTest' --tests '*LocalRuntimeConfigurationTest'` | Controller·Integration·LocalRuntime 묶음에서 context 수명과 scheduler 종료 동작 검증 | PASS. `BUILD SUCCESSFUL in 3m 11s`; JUnit XML 19개, tests=57, failures=0, errors=0, skipped=0; HTML report 생성 |

실행은 `S:` ASCII subst worktree의 단일 Gradle 프로세스에서 수행했습니다. 컴파일 중 KafkaTestUtils deprecated warning 4건 외 오류는 없었습니다.

## Ranking Rebuild 단독

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `S:\gradlew.bat clean test --no-daemon --tests '*RankingRebuildServiceIntegrationTest'` | 완료 기준의 단독 Testcontainers lifecycle 검증 | PASS. `BUILD SUCCESSFUL in 1m 42s`; JUnit XML 1개, tests=10, failures=0, errors=0, skipped=0; HTML report 생성 |

## 종료·cleanup 관찰

| 확인 | 목적 | 결과 |
| --- | --- | --- |
| `docker ps --format '{{.ID}} {{.Image}} {{.Names}} {{.Status}}'` | 테스트 종료 후 실행 컨테이너 확인 | 두 실행 모두 0개 |
| `docker ps -a --filter label=org.testcontainers=true ...` | Testcontainers 고아 컨테이너 확인 | 두 실행 모두 0개 |
| `Get-Process java,gradle` | Gradle/Test Executor 잔존 확인 | 두 실행 모두 0개 |
| build test report 문자열 검색 | 종료 후 connection-refused/scheduler 예외 확인 | `connection refused`, `Connection refused`, `scheduler`, `TaskScheduler`, `CannotCreateTransactionException`, `Communications link failure` 모두 0건 |

## Repository 정적 확인

| 명령 또는 확인 | 목적 | 결과 |
| --- | --- | --- |
| `git diff --name-only`와 `git status --short` | production 변경 여부와 범위 확인 | `src/test/**` 7개만 변경, `src/main/**`·migration·runtime 설정 변경 없음 |
| `git diff --check` | whitespace 정적 검사 | PASS. LF→CRLF 변환 예고 warning만 관찰 |

독립 Review·QA와 최신 CI 결과는 아직 실행 전이며 PR과 GitHub checks에서 별도로 확인합니다.
