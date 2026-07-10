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

## Review FAIL 수정 Attempt 2

- RED command: `./gradlew.bat test --tests "*OrderEventPublisherTest.logsBrokerAcknowledgementFailureWithEventContext" --no-daemon`.
- RED result: failed future에서 기대 오류 로그가 없어 assertion 실패, `BUILD FAILED in 24s`.
- GREEN command: 동일 focused regression command.
- GREEN result: `BUILD SUCCESSFUL in 29s`.
- Focused + Level 4 command: `./gradlew.bat test --tests "*OrderCompletedEventTest" --tests "*OrderEventPublisherTest" --tests "*OrderServiceLockTest" --tests "*OrderEventKafkaIntegrationTest" --no-daemon`.
- Focused + Level 4 result: `BUILD SUCCESSFUL in 1m 18s`.
- Full regression command: `./gradlew.bat test --no-daemon`.
- Full regression result: `BUILD SUCCESSFUL in 1m 21s`.

## 독립 Review와 QA 최종 결과

- Review result: 수정 필요 항목 없음, PASS. Review는 테스트를 실행하지 않았습니다.
- QA Level 4 command scope: `OrderCompletedEventTest`, `OrderEventPublisherTest`, `OrderServiceLockTest`, `OrderEventKafkaIntegrationTest` focused suite.
- QA Level 4 result: 7 tests, failures 0, `BUILD SUCCESSFUL in 1m 17s`.
- QA Level 1 command: `./gradlew.bat test --no-daemon`.
- QA Level 1 result: 29 tests, failures 0, `BUILD SUCCESSFUL in 1m 23s`.
- QA Level 5 result: 앱과 MySQL/Kafka/Redis가 기동됐고 `Started CoffeeOrderSystemApplication in 43.23 seconds`, health HTTP 200 `UP`을 확인했습니다.
- QA Level 6 HTTP result: userId 808 충전 HTTP 200, 주문 HTTP 201. 실제 request/status/response는 `http/issue-8-order-completed-event.http`와 `manual-qa.md`에 보존했습니다.
- QA Kafka consumer method: Kafka container bridge IP에 연결하고 `--add-host`로 container ID를 매핑해 내부 BROKER listener `:9093`을 kcat에서 사용했습니다.
- QA Kafka observation result: key `808`과 eventId `13247f60-c5a7-4a7c-a771-39b225d191a4`인 JSON value를 소비했습니다. QA 리소스는 정리했습니다.

## Metrics Timestamp Evidence

- Start timestamp command: `(Get-Item docs/testing/evidence/issue-8/acceptance-criteria.md).CreationTime.ToString('yyyy-MM-dd HH:mm:ss')`.
- Start timestamp result: `2026-07-11 08:06:56`.
- Commit timestamp command: `git show -s --format=%cI 8576010`.
- Commit timestamp result: `2026-07-11T08:33:24+09:00`. 이 값은 독립 재현 가능한 commit 시각이지만 마지막 Reverification 종료 시각으로 사용하지 않습니다.
- Exact duration decision: 마지막 Reverification 종료 시각을 독립적으로 재구성할 수 없어 최초 Generate부터 마지막 Reverification까지의 작업 시간은 추정하지 않고 `미측정`으로 기록합니다.
