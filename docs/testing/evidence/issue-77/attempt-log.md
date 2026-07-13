# Issue #77 Attempt Log

Issue: #77
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/77
Branch: codex/issue-77-dlt-flaky

## Attempt 1

### Generate

- 시작: 2026-07-13T13:13:39.3244606+09:00.
- live Issue #77의 기존 RED와 현재 테스트의 Kafka 처리 순서를 먼저 확인했습니다.
- `RankingEventConsumerDltIntegrationTest`에서 input 전송 전에 실제 `ranking-consumer-group` listener가 partition을 assignment받을 때까지 기다리는 최소 test-only 변경을 적용했습니다.

### Evaluate

- 기존 RED: Issue #77의 깨끗한 격리 실행은 1 test failure, `IllegalStateException: No records found for topic`, 외부 Stopwatch 127.8초였습니다.
- 현재 수정 전 exact 명령 2회는 109.033초와 91.929초에 PASS해 failure가 결정적이지 않은 flaky임을 다시 확인했습니다.
- 첫 수정 전 `--info` 추적에서 DLT observer subscribe 뒤 assignment 전 input send가 시작됐고, producer ack 뒤 main listener assignment/reset, DLT observer assignment/reset, listener seek와 retry 2회, DLT 관찰 순서가 나타났습니다.
- 원인은 Spring context 시작 완료와 Kafka listener partition assignment가 같지 않은데 테스트가 input을 즉시 전송해 consumer readiness와 producer ack가 경합한 것입니다.

### Failure Cause

- DLT 결과 관찰 자체는 bounded poll이었지만 input을 처리할 main listener의 partition assignment를 기다리지 않았습니다.
- 같은 저장소의 정상 Kafka/Redis 통합 테스트는 `ContainerTestUtils.waitForAssignment` 뒤 publish하므로 이 차이를 단일 가설로 선택했습니다.

### Change Scope

- `src/test/java/com/example/coffeeordersystem/RankingEventConsumerDltIntegrationTest.java` 한 파일만 변경했습니다.
- production, build, workflow, DLT 정책과 assertion은 변경하지 않았습니다.

### Reverification

- 종료: 2026-07-13T13:30:28.3746642+09:00.
- 같은 격리 명령을 매번 새 `--no-daemon --max-workers=1` Gradle 프로세스로 3회 실행해 91.755초, 77.557초, 73.945초에 모두 PASS했습니다.
- 관련 Kafka/DLT 3개 통합 테스트는 150.966초에 PASS했습니다.
- 전체 `cleanTest test`는 187.834초, 51 tests, failures 0, errors 0, skipped 0으로 PASS했습니다.
- repository gate, `git diff --check`, 저장소 밖 UTF-8 no-BOM 한국어 PR body preflight가 모두 PASS했습니다.

### Next Attempt

- 없음. 저장소에는 이 Attempt의 불변 실행 결과만 유지하고 이후 역할 판정과 CI는 GitHub를 정본으로 확인합니다.

## 독립 역할 검증 근거

- 독립 QA가 같은 격리 테스트를 각각 새 Gradle 프로세스에서 95.913초, 78.913초, 78.979초에 실행해 모두 PASS했습니다.
- 독립 QA의 관련 Kafka/DLT 3개 통합 테스트는 115.216초, 3 tests, failures 0, errors 0, skipped 0으로 PASS했습니다.
- 독립 QA는 첫 상세 로그에서 main listener assignment가 producer 동작보다 먼저 완료되고, 이후 retry 2회와 DLT record 반환이 이어지는 순서를 관찰했습니다.
- 역할 판정, 현재 head, CI와 merge 가능 상태는 GitHub PR comments와 checks에서 확인하며 저장소 evidence에 현재 상태 snapshot을 복제하지 않습니다.
