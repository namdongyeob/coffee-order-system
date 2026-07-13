# Issue #14 Acceptance Criteria

Issue: #14
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/14

Execution mode: STRICT
Execution mode reason: Kafka replay, consumer group offset, Redis 원자 교체와 MySQL 원천 비교를 함께 변경하므로 독립 Review·QA·Docs·CI가 필요합니다.
Level 5 required: YES
Level 5 reason: local MySQL·Redis·Kafka에서 maintenance runner의 성공·실패·cleanup을 관찰해야 합니다.
Level 6 required: NO
Level 6 reason: 공개 HTTP API 계약을 추가하거나 변경하지 않고 운영 runner로 실행합니다.

## 완료 기준

- [x] maintenance mode가 아니거나 일반 ranking consumer가 활성 상태이면 시작하지 않습니다.
- [x] Redis 분산 lock으로 동시 runner를 차단합니다.
- [x] snapshot과 partition별 exclusive end offset을 고정하고 현재 Kafka earliest부터 replay합니다.
- [x] `[snapshot-7일, snapshot)`의 `PAID` 주문과 replay를 날짜·menuId별로 정확히 비교합니다.
- [x] temp namespace에서 집계한 뒤 Lua로 대상 날짜 전체를 원자 교체합니다.
- [x] 실패 시 live key와 정상 group offset을 보존하고 temp를 삭제합니다.
- [x] 성공 시에만 정상 group offset을 캡처한 end offset으로 이동합니다.
- [x] 반복 실행은 새 namespace를 사용하며 일반 `processed_event`를 오염시키지 않습니다.
- [x] Level 4와 Level 5 검증 결과를 실제 관찰값으로 기록합니다.

현재 earliest가 `0`보다 큰 것만으로 실패하지 않습니다. 각 partition의 현재 earliest부터 캡처한 end 직전까지 replay하고, 최근 7일 retention 완전성은 DB exact comparison으로 fail-closed 검증합니다.
