# Issue Attempt Log

Issue: #8
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/8
Branch: codex/issue-8-kafka-order-event

## Attempt 1

### Generate

- payload record, Kafka publisher, 주문 커밋 이후 발행 연결, JSON producer 설정과 테스트를 TDD로 구현했습니다.

### Evaluate

- PASS. focused unit, Level 4 Kafka, Level 5 기동, Level 6 HTTP, 전체 회귀가 통과했습니다.
- Level 6의 별도 Kafka payload CLI 관찰은 PARTIAL입니다.

### Failure Cause

- 첫 Level 4 실행에서 Java time module 누락으로 serialization이 실패했습니다.
- module 추가 뒤 `orderedAt`이 배열로 직렬화되어 문서의 문자열 계약과 달랐습니다.

### Change Scope

- Jackson Java time 지원과 `orderedAt` 문자열 shape만 production 범위에서 보완했습니다.
- 테스트 harness API 컴파일 오류는 테스트 파일 안에서만 수정했습니다.

### Reverification

- focused + Level 4 `BUILD SUCCESSFUL in 1m 23s`.
- Level 5 앱 기동 PASS, Level 6 HTTP 200/201 PASS, Kafka CLI payload PARTIAL.
- 전체 회귀 `BUILD SUCCESSFUL in 1m 26s`.

### Next Attempt

- 독립 Review와 QA를 실행합니다. QA가 Level 5/6 결과를 확정하면 Docs Agent가 `verification-log.md`에 옮긴 뒤 pre-push hook, push, draft PR 생성을 재실행합니다.

## Attempt 2 - Review FAIL 수정

### Generate

- broker ack 비동기 실패에 event/order/user/topic context를 포함한 오류 callback 로그를 추가했습니다.
- Mock 테스트와 evidence 표현을 실제 검증 수준인 `TransactionTemplate.execute` 반환 뒤 publish 호출로 낮췄습니다.

### Evaluate

- callback regression RED는 실패 future에서 기대 로그가 없어 assertion 실패했습니다.
- 최소 callback 구현 뒤 focused regression은 `BUILD SUCCESSFUL in 29s`였습니다.

### Failure Cause

- publisher가 `KafkaTemplate.send()` future를 반환했지만 호출자가 반환값을 무시해 ack 실패를 처리하는 주체가 없었습니다.

### Change Scope

- `OrderEventPublisher`의 failure callback과 해당 테스트, Issue #8 evidence 표현만 수정합니다.

### Reverification

- callback regression `BUILD SUCCESSFUL in 29s`.
- focused + Level 4 `BUILD SUCCESSFUL in 1m 18s`.
- 전체 회귀 `BUILD SUCCESSFUL in 1m 21s`.

### Next Attempt

- 독립 Review와 QA에 수정 SHA를 전달합니다. Level 6 Kafka payload 원문은 Dev 환경에서 Testcontainers advertised `localhost` 재연결 문제로 계속 PARTIAL이므로 QA에 전달합니다.

## Final Review, QA, Docs 확정

### Evaluate

- 최종 Review는 수정 필요 항목 없이 PASS했습니다.
- 독립 QA는 Level 4 focused Kafka 7 tests/0 failure, Level 1 전체 회귀 29 tests/0 failure, Level 5 앱과 MySQL/Kafka/Redis 기동 및 health 200 `UP`, Level 6 실제 HTTP와 Kafka raw record를 모두 PASS로 확정했습니다.
- QA 결함은 0건이며 추가 Dev 재시도는 없습니다.

### Reverification

- Level 4 focused Kafka는 `BUILD SUCCESSFUL in 1m 17s`였습니다.
- Level 1 전체 회귀는 `BUILD SUCCESSFUL in 1m 23s`였습니다.
- Level 5는 `Started CoffeeOrderSystemApplication in 43.23 seconds`와 health 200 `UP`을 확인했습니다.
- Level 6은 userId 808의 충전 200, 주문 201과 Kafka key `808`, 실제 JSON value를 확인했습니다. Kafka consumer는 bridge IP, `--add-host` container ID mapping, 내부 BROKER listener `:9093`, kcat으로 연결했습니다.
- QA가 사용한 리소스는 정리했습니다.

### Next Attempt

- CI 확인, push와 draft PR 생성은 Main Coordinator가 다음 역할에 전달합니다. Docs Agent는 production/test/build 수정, Gradle·인프라 재실행, push를 수행하지 않습니다.

## Attempt 3 - Claude MAJOR 동기 send 실패 수정

Attempt started at: 2026-07-11T10:30:07.190+09:00
Start source: Main Coordinator가 현재 턴 시작 시 실측해 전달한 시각.

### Generate

- KafkaTemplate.send 자체의 동기 RuntimeException이 주문 API로 전파되지 않도록 회귀 테스트부터 작성했습니다.
- 동기 send 실패와 비동기 ack 실패가 각각 정확히 한 번 기록되도록 검증했습니다.

### Evaluate

- RED에서 동기 예외가 호출자에게 전파되어 `doesNotThrowAnyException` assertion이 실패했습니다.
- 최소 try-catch 구현 뒤 publisher focused test가 PASS했습니다.

### Failure Cause

- `send()`가 future를 반환하기 전에 던지는 동기 예외는 기존 `whenComplete` callback에 도달하지 않아 OrderService로 전파됩니다.

### Change Scope

- OrderEventPublisher의 동기 예외 경계, 관련 publisher/service 테스트와 Issue #8 evidence만 수정합니다.

### Reverification

- RED: `BUILD FAILED in 28s`.
- Publisher GREEN: `BUILD SUCCESSFUL in 25s`.
- Focused + Level 4: `BUILD SUCCESSFUL in 1m 29s`.
- Fresh 전체 회귀: `BUILD SUCCESSFUL in 1m 30s`.
- Reverification ended at: `2026-07-11T10:35:48.7319732+09:00`.
- End source: fresh 전체 회귀 종료 직후 같은 worktree에서 실행한 `Get-Date -Format o` 원문.
- Attempt 3 duration: `00:05:41.5419732` (`2026-07-11T10:30:07.190+09:00`부터 위 종료 시각까지).

### Next Attempt

- Issue harness, semantic commit, push와 PR actual body 갱신 뒤 CI를 확인합니다. Level 6 실제 API 응답 계약 보존은 후속 QA가 확인합니다.
