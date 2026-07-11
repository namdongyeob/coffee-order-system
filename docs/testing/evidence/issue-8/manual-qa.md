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
- 동기 `send()` 실패도 호출자에게 전파하지 않으므로 이미 완료된 DB 작업 뒤 주문 API가 Kafka 동기 예외 때문에 500으로 바뀌지 않습니다.
- DB commit 후 Kafka publish를 비동기로 요청합니다. 발행 실패는 로그로 관찰하지만 API 응답과 DB 작업을 rollback하지 않습니다. DB-Kafka 원자성 및 Outbox가 없으므로 이벤트 유실 가능성이 남습니다.
