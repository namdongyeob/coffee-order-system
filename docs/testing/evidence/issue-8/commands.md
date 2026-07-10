# Issue #8 Commands

## Baseline

- Command: `./gradlew.bat test --no-daemon`
- Purpose: `origin/main` 기준 전체 회귀 확인.
- Result: `BUILD SUCCESSFUL in 1m 44s`.

## TDD RED

- Command: `./gradlew.bat test --tests "*OrderCompletedEventTest" --tests "*OrderEventPublisherTest" --tests "*OrderServiceLockTest" --tests "*OrderEventKafkaIntegrationTest" --no-daemon`
- Result: `OrderCompletedEvent`, `OrderEventPublisher` 미존재로 `compileTestJava`가 6개 `cannot find symbol` 오류와 함께 예상대로 실패했습니다.

## Focused Unit and Level 4

- Command: `./gradlew.bat test --tests "*OrderCompletedEventTest" --tests "*OrderEventPublisherTest" --tests "*OrderServiceLockTest" --tests "*OrderEventKafkaIntegrationTest" --no-daemon`
- Intermediate failure: 실제 Kafka JSON에서 `orderedAt`이 배열로 직렬화되어 문서의 ISO 문자열 계약 assertion이 실패했습니다.
- Final result after self-review: `BUILD SUCCESSFUL in 1m 23s`. 실제 Kafka topic의 key와 JSON payload 6개 필드를 소비해 검증했습니다.

## Level 5 and Level 6

- Start command: `./gradlew.bat bootTestRun --no-daemon`
- Level 5 result: Tomcat 8080, MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2가 기동되고 `Started CoffeeOrderSystemApplication in 45.924 seconds`를 확인했습니다.
- Request command:

```powershell
$chargeBody='{"userId":808,"amount":10000}'
$orderBody='{"userId":808,"menuId":1}'
$charge=Invoke-WebRequest -Uri 'http://localhost:8080/api/points/charge' -Method Post -ContentType 'application/json' -Body $chargeBody
$order=Invoke-WebRequest -Uri 'http://localhost:8080/api/orders' -Method Post -ContentType 'application/json' -Body $orderBody
```

- Result: 포인트 충전 HTTP 200, 주문 생성 HTTP 201. 원문은 `http/issue-8-order-completed-event.http`와 `manual-qa.md`에 보존했습니다.
- Kafka observation attempt: `docker run --rm --network host edenhill/kcat:1.7.1 -b localhost:61751 -C -t order.completed -o beginning -c 1 -f '%k | %s\n'`.
- Kafka observation result: Testcontainers advertised listener가 container 내부의 `localhost:61751`을 가리켜 `Broker transport failure`. Level 6 payload 소비는 `PARTIAL`이며 Level 4 실제 Kafka PASS와 구분합니다.

## Full Regression

- Command: `./gradlew.bat test --no-daemon`
- Result after self-review: `BUILD SUCCESSFUL in 1m 26s`.

## Repository Gate and Push

- Command: `python scripts/harness_gate.py --issue 8 --branch codex/issue-8-kafka-order-event --base-ref origin/main --check-links --check-branch --include-worktree`.
- Result before commit: `Harness gate PASSED` for Dev evidence and changed links.
- Command: `git push -u origin codex/issue-8-kafka-order-event`.
- Result: pre-push hook FAIL. `verification-log.md`에 Issue #8 Level 5/6 PASS가 아직 없음을 거부했습니다.
- Handling: Dev는 Docs Agent 소유 verification-log를 수정하거나 hook을 우회하지 않습니다. 독립 QA 확정과 Docs 반영 뒤 push를 재실행해야 합니다.
