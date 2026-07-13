# Issue #15 Acceptance Criteria

Issue: #15
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/15

Execution mode: STRICT
Execution mode reason: DLT 메시지 조회·선택 재발행·processed_event 확인은 Kafka 복구와 운영 계약을 변경하거나 검증하는 범위입니다.
Level 5 required: YES
Level 5 reason: 운영 스크립트가 로컬 애플리케이션 구성에서 안전하게 동작하는지 확인해야 합니다.
Level 4 required: YES
Level 4 reason: Kafka DLT 단건 재조회, header 검증, processed_event skip과 consumer 처리를 실제 인프라에서 검증합니다.
Level 6 required: NO
Level 6 reason: 공개 HTTP API를 추가하거나 변경하지 않습니다.

## 완료 기준

- [x] 승인 DLT를 topic·partition·offset 한 건으로 선택합니다.
- [x] 승인자와 사유를 필수 입력으로 검증합니다.
- [x] original topic·partition·offset header를 fail-closed로 검증하고 DLT·예외·stacktrace header는 복사하지 않습니다.
- [x] `processed_event` 존재 시 `SKIPPED_ALREADY_PROCESSED`로 종료하고 경쟁 위험을 결과에 기록합니다.
- [x] script, focused test, runbook과 scripts README를 구현했습니다.
- [x] Level 4와 Level 5를 실제 Kafka·Redis·MySQL·로컬 runtime에서 검증했습니다.

## 제외 범위

- 원인 분류 또는 운영자 승인 없는 자동 전체 재처리.
- 공개 운영 API.
- 현재 정본에 없는 재발행 정책을 임의로 도입하는 구현.

## 현재 판정

- PASS. 상세 검증 결과는 `verification.md`를 정본으로 확인합니다.
