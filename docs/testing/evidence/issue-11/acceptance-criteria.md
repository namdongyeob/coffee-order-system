# Issue #11 Acceptance Criteria

Issue: #11
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/11
Branch: codex/issue-11-kafka-retry-dlt-v2

Execution mode: STRICT
Execution mode reason: Kafka consumer error handler, retry, DLT 발행과 runtime 설정을 변경하므로 독립 Review, QA, Docs와 CI가 필요합니다.
Level 5 required: YES
Level 5 reason: 변경된 Kafka listener error handler가 로컬 애플리케이션과 실제 broker에서 기동되는지 확인해야 합니다.
Level 6 required: NO
Level 6 reason: 외부 HTTP API 계약은 변경하지 않으며 실패 소비 경로는 Level 4 Kafka 관찰로 검증합니다.

## 완료 기준

- `DefaultErrorHandler`가 `FixedBackOff(1000L, 2L)`를 사용합니다.
- 처리 실패 메시지는 원본 partition을 유지한 `order.completed.DLT`로 이동합니다.
- 실제 Kafka Testcontainers 테스트가 최초 처리와 재시도 2회를 관찰하고 DLT의 key, payload, 원본 topic header를 검증합니다.
- 로컬 runbook이 DLT topic, header, 원인 분류와 cleanup 명령을 재현 가능하게 안내합니다.
- 자동 DLT replay API와 Issue #21 범위는 구현하지 않습니다.

