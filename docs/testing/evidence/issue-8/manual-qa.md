# Issue #8 Manual QA

## Level 5 PASS

- 독립 QA가 실제 애플리케이션과 MySQL, Kafka, Redis를 기동했습니다.
- `Started CoffeeOrderSystemApplication in 43.23 seconds`와 health HTTP 200 `UP`을 확인했습니다.

## Level 6 HTTP PASS

```text
charge request: {"userId":808,"amount":10000}
charge status: 200
charge response: {"userId":808,"balance":10000}
order request: {"userId":808,"menuId":1}
order status: 201
order response: {"orderId":2,"userId":808,"menuId":1,"menuName":"아메리카노","paidAmount":4500,"status":"PAID","orderedAt":"2026-07-11T08:41:11.7107765"}
```

재현 가능한 HTTP 원문은 `http/issue-8-order-completed-event.http`에 있습니다.

## Level 6 Kafka PASS

- 독립 QA가 Kafka container bridge IP에 연결하고 `--add-host`로 container ID를 매핑해 내부 BROKER listener `:9093`을 kcat에서 사용했습니다.
- Kafka key: `808`.
- Kafka value: `{"eventId":"13247f60-c5a7-4a7c-a771-39b225d191a4","orderId":2,"userId":808,"menuId":1,"paidAmount":4500,"orderedAt":"2026-07-11T08:41:11.7107765"}`.
- QA가 사용한 애플리케이션과 인프라 리소스는 정리했습니다.

## 발행 실패 의미

- `TransactionTemplate.execute` 반환 뒤 Kafka 발행을 요청하며, unit test는 이 호출 순서만 검증합니다. 실제 DB commit 순간 자체를 Mock 테스트가 증명한다고 주장하지 않습니다.
- Kafka `send()` 동기 실패와 broker ack 비동기 실패는 각각 `order_completed_event_publish_failed` 오류 로그 한 건과 eventId, orderId, userId, topic으로 관찰합니다.
- unit regression에서는 동기 `send()` RuntimeException을 호출자에게 전파하지 않음을 확인했습니다. 실제 broker-down runtime의 최종 HTTP status는 아래 bounded QA에서 관찰하지 못했습니다.
- DB commit 후 Kafka publish를 비동기로 요청합니다. 발행 실패는 로그로 관찰하지만 API 응답과 DB 작업을 rollback하지 않습니다. DB-Kafka 원자성 및 Outbox가 없으므로 이벤트 유실 가능성이 남습니다.

## Broker-down bounded QA PARTIAL

- 앱 health 200과 userId 909 포인트 충전 200을 확인한 뒤 첫 주문 send 전에 Kafka를 중지했습니다.
- 주문 HTTP client는 15,048ms에 timeout되어 최종 HTTP status를 관찰하지 못했습니다. 실제 runtime 응답을 201 또는 500으로 주장하지 않습니다.
- DB에는 order id 1, userId 909, status `PAID` 주문과 잔액 5500이 남아 DB 작업 완료는 관찰했습니다.
- producer 기본 `max.block.ms=60000`으로 metadata retry가 계속되어 15초 제한 안에 sync throw/log를 결정적으로 관찰하지 못했습니다.
- unit regression은 Kafka `send()`가 동기 RuntimeException을 던지는 경우 catch하고 호출자에게 전파하지 않으며 sync marker를 1회 기록함을 PASS했습니다. 이 단위 결과는 실제 broker-down 관찰의 PARTIAL을 실제 runtime PASS로 바꾸지 않습니다.
- outage latency가 최대 `max.block.ms` 경계까지 늘어날 수 있는 위험이 남습니다. timeout 정책은 후속 Issue 후보이며 Consumer retry/DLT는 이 관찰과 무관합니다.
- QA는 8080과 자신이 만든 container를 정리했고 기존 pgvector는 건드리지 않았습니다.
