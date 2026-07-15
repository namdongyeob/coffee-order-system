# Issue #111 Acceptance Criteria

Issue: #111
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/111
Branch: issue-111

Execution mode: SOLO
Execution mode reason: 애플리케이션 동작을 변경하지 않고 공통 ledger 구현 전 복구 계약을 결정하는 ADR 문서 전용 작업입니다.
Level 5 required: NO
Level 5 reason: 런타임이나 인프라 설정을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP API 계약을 변경하지 않습니다.

- [x] `processed_event` 재사용과 별도 ledger 선택지를 비교하고 최종 결정을 ADR에 기록했습니다.
- [x] key, 필드, unique 제약, retention과 normal consumer·rebuild·DLT replay의 처리 시점을 기록했습니다.
- [x] Redis 반영, rebuild swap, DB ledger 기록의 crash recovery 상태 전이를 표로 기록했습니다.
- [x] 어떤 복구 순서에서도 같은 `eventId` 점수가 최대 한 번 반영된다는 불변조건을 기록했습니다.
- [x] 후속 구현 Issue가 사용할 acceptance criteria를 ADR에 기록했습니다.
- [x] ADR 링크 검사를 PASS했습니다.
