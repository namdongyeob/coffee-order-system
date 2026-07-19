# Issue #133 Acceptance Criteria

Issue: #133
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/133

Execution mode: STRICT
Execution mode reason: 이 작업은 recovery 및 exactly-once와 직접적으로 결합된 복구 계약(ADR-008) 변경으로 높은 위험도를 가집니다. 따라서 안전한 검증 수준을 확보하기 위해 STRICT 모드로 정의하여 증명합니다.
Level 5 required: NO
Level 5 reason: 이 이슈에서는 애플리케이션과 인프라 설정을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: 이 이슈에서는 HTTP API 계약을 변경하지 않습니다.

## 완료 기준

- [x] `RESERVED / REDIS_APPLIED / COMMITTED`와 marker 존재·만료 조합별 허용 동작을 상태 표로 작성합니다.
- [x] marker TTL, retry 시간, DLT/Kafka retention, segment 삭제 지연 사이의 안전 여유를 정의합니다.
- [x] 같은 fingerprint pending event가 marker 만료 뒤 들어왔을 때 reconcile, TTL 갱신 또는 fail-closed 중 하나를 결정합니다.
- [x] Redis 적용 여부를 증명할 수 없는 `RESERVED + marker 없음` 상태의 운영자 복구 절차를 결정합니다.
- [x] pending row 보존·cleanup·관측 지표 계약을 정합니다.
- [x] 후속 구현 이슈가 추측 없이 작업할 파일과 테스트 시나리오를 명시합니다.

## 후속 게이트

STRICT 모드이므로 로컬 문서 검사 및 링크 유효성 검사 외에, PR 생성 후 독립 Review/QA 및 CI의 최종 통과가 필요합니다.
