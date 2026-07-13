# Issue #15 Acceptance Criteria

Issue: #15
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/15

Execution mode: STRICT
Execution mode reason: DLT 메시지 조회·선택 재발행·processed_event 확인은 Kafka 복구와 운영 계약을 변경하거나 검증하는 범위입니다.
Level 5 required: YES
Level 5 reason: 운영 스크립트가 로컬 애플리케이션 구성에서 안전하게 동작하는지 확인해야 합니다.
Level 6 required: NO
Level 6 reason: 공개 HTTP API를 추가하거나 변경하지 않습니다.

## 완료 기준

- [ ] 운영자가 승인한 DLT 메시지를 안정적으로 식별하고 선택할 계약이 확정되었습니다.
- [ ] 승인 증적과 원인 분류의 입력·보존 규칙이 확정되었습니다.
- [ ] DLT 원본·예외 header의 검증과 재발행 시 보존·제거 정책이 확정되었습니다.
- [ ] `processed_event` 확인의 조회 기준, 경쟁 조건 및 재발행 결과 정책이 확정되었습니다.
- [ ] 확정 계약에 맞는 script, focused test, runbook과 scripts README를 구현합니다.
- [ ] Level 4와 Level 5를 실제 Kafka·Redis·MySQL·로컬 runtime에서 검증합니다.

## 제외 범위

- 원인 분류 또는 운영자 승인 없는 자동 전체 재처리.
- 공개 운영 API.
- 현재 정본에 없는 재발행 정책을 임의로 도입하는 구현.

## 현재 판정

- BLOCKED. `docs/ai/agent-rules.md`의 정책 미결정 Clarify 정지 규칙에 따라 구현을 시작하지 않았습니다.
