# Issue #110 Acceptance Criteria

Issue: #110
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/110
Branch: issue-110
Execution mode: STRICT
Execution mode reason: Kafka와 Redis replay 경로의 멱등성 및 fail-closed 동작을 변경합니다.
Level 5 required: YES
Level 5 reason: rebuild runner가 실제 maintenance 실행에서 지표를 남기는지 확인해야 합니다.
Level 6 required: NO
Level 6 reason: HTTP API 동작을 변경하지 않습니다.

- [ ] 동일 eventId와 동일 핵심 payload는 한 번만 집계됩니다.
- [ ] 동일 eventId와 다른 핵심 payload는 fail-closed 합니다.
- [ ] 입력, 고유 event, 충돌 수를 결과 또는 로그에서 관찰할 수 있습니다.
- [ ] Level 4 실제 Kafka와 Redis focused test가 PASS 입니다.
- [ ] Level 5 rebuild runner 실행이 PASS 입니다.

현재 disposition이 BLOCKED이므로 acceptance checkbox를 완료로 표시하지 않습니다.
