# Issue #99 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/99

Execution mode: STRICT
Execution mode reason: DB schema 추가와 Kafka 발행 경로를 변경하는 transaction·event contract 위험이 있는 변경입니다.
Level 5 required: NO
Level 5 reason: 새로운 HTTP 진입점 변경이 없고, Level 4 Testcontainers 통합 테스트로 발행·재시도 시나리오를 검증합니다.
Level 6 required: NO
Level 6 reason: 외부 API 계약이 바뀌지 않으므로 실제 HTTP 요청 검증 대상이 아닙니다.

## 완료 기준

- [x] `OutboxEvent`가 주문 트랜잭션과 같은 트랜잭션 경계에서 저장됩니다. `OrderService.payAndCreateOrder()`(TransactionTemplate 람다) 내부에서 `saveOutboxEvent()`를 호출하며, 이 메서드의 유일한 호출자입니다.
- [x] 별도 Publisher가 미발행 이벤트를 조회해 Kafka로 발행하고 상태를 갱신합니다. `OutboxEventPublisher.publishPending()`이 `@Scheduled(fixedDelayString = "${outbox.publish.fixed-delay-ms:2000}")`로 `findTop50ByPublishedAtIsNullOrderByIdAsc()`를 폴링하고, 발행 성공 시 `markPublished` 후 `save()`합니다.
- [x] Kafka 발행이 일시적으로 실패해도 주문 DB 커밋은 영향받지 않고, 이후 재시도로 이벤트가 발행됨을 테스트로 검증했습니다. `OutboxEventKafkaUnavailableIntegrationTest`(실제 DB, Kafka 항상 실패 mock)와 `OutboxEventPublisherTest.publishesOnSubsequentPollAfterTransientFailure`(1차 실패·2차 성공)로 검증했습니다.
- [x] `docs/testing/evidence-guide.md`의 기본 evidence 파일을 작성했습니다.

## 참고

- fresh 독립 Review Agent: `APPROVED`, P0/P1 없음. P2 1건(다중 인스턴스 환경에서 동시 poller 간 claim/lock이 없어 수평 확장 시 중복 발행 가능성) — 이 Issue의 범위(단일 인스턴스 MVP)를 벗어나는 후속 과제로 남깁니다.
- fresh 독립 QA Agent: `PASS`. focused 17/17, 전체 회귀 76/76, Python 하네스 160/160.
- `ObjectMapper`는 이 저장소에서 Spring 관리 빈이 아닙니다(기존 `DltReplayService`와 동일 관례로 클래스별 인스턴스 직접 생성). `OrderService`, `OutboxEventPublisher` 모두 `new ObjectMapper().registerModule(new JavaTimeModule())`을 사용합니다.
- 공유 Kafka topic에 백그라운드 스케줄러가 항상 떠 있게 되면서 기존 `OrderEventKafkaIntegrationTest`의 `KafkaTestUtils.getSingleRecord` 가정이 깨져 eventId 기준 필터 폴링으로 함께 수정했습니다(이 Issue의 diff로 인해 필요해진 최소 범위 수정).
- `Current head`는 이 커밋을 가리킵니다: `90ac69680edc620410bfef0c04deb5f76d29b6f5`.
