# Issue #8 Manual QA

## Level 5 PASS

- `bootTestRun`으로 실제 애플리케이션을 기동했습니다.
- Tomcat 8080과 MySQL, Kafka, Redis Testcontainers 연결을 확인했습니다.

## Level 6 HTTP PASS

```text
charge request: {"userId":808,"amount":10000}
charge status: 200
charge response: {"userId":808,"balance":10000}
order request: {"userId":808,"menuId":1}
order status: 201
order response: {"orderId":1,"userId":808,"menuId":1,"menuName":"아메리카노","paidAmount":4500,"status":"PAID","orderedAt":"2026-07-11T08:17:00.722201"}
```

재현 가능한 HTTP 원문은 `http/issue-8-order-completed-event.http`에 있습니다.

## Level 6 Kafka PARTIAL

- 주문 요청 뒤 애플리케이션 로그에서 `value.serializer = JsonSerializer`, `order.completed=UNKNOWN_TOPIC_OR_PARTITION` 자동 생성 과정, cluster 연결과 ProducerId 할당을 관찰했습니다.
- 별도 kcat consumer는 Testcontainers advertised listener 접근 문제로 payload를 읽지 못했습니다.
- 따라서 실제 요청에서 발생한 Kafka payload 원문 관찰은 미완료입니다. payload, key, topic의 실제 Kafka 검증은 Level 4 Testcontainers 테스트가 PASS했습니다.
