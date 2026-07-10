# Issue #8 Acceptance Criteria

Issue: #8
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/8
Branch: codex/issue-8-kafka-order-event

Execution mode: STRICT
Execution mode reason: Kafka producer, 주문 완료 이벤트 계약, 런타임 인프라 연결을 변경하므로 독립 Dev, Review, QA, Docs 검증과 CI가 필요합니다.
Level 5 required: YES
Level 5 reason: Kafka producer 런타임 설정과 주문 완료 후 발행 연결이 바뀌므로 실제 애플리케이션 기동을 확인해야 합니다.
Level 6 required: YES
Level 6 reason: 실제 주문 API 요청이 DB 커밋 뒤 Kafka 이벤트 발행까지 연결되는지 재현 가능한 요청과 이벤트 관찰로 확인해야 합니다.

## 완료 조건

- [x] `OrderCompletedEvent`는 `eventId`, `orderId`, `userId`, `menuId`, `paidAmount`, `orderedAt`을 문서 계약의 타입과 의미로 제공합니다.
- [x] 성공한 주문 DB 트랜잭션이 커밋된 뒤 `order.completed` topic으로 이벤트를 발행합니다.
- [x] Kafka 메시지 key는 `userId`의 문자열 표현입니다.
- [x] 주문 트랜잭션 실패 시 이벤트를 발행하지 않습니다.
- [x] producer 직렬화 설정으로 이벤트 JSON payload를 전송합니다.
- [x] focused unit test에서 payload, topic, key, producer 호출과 커밋 이후 순서를 검증합니다.
- [x] Level 4 Kafka Testcontainers에서 실제 topic에 발행된 JSON 이벤트를 소비해 계약을 검증합니다.
- [x] Level 5 애플리케이션 기동 결과를 보존합니다.
- [ ] Level 6 실제 주문 요청, request JSON, response JSON, Kafka 이벤트 관찰 원문을 재현 가능한 evidence로 보존합니다.
- [x] 전체 `./gradlew.bat test --no-daemon` 회귀 테스트를 통과합니다.
- [x] Consumer 랭킹, Consumer 멱등성, DLT, Outbox, 공통 이벤트 framework는 구현하지 않습니다.
