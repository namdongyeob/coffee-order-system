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
