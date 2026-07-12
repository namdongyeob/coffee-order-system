# Issue #66 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/66

Execution mode: STRICT
Execution mode reason: 자율 큐의 remediation 정책, 역할 배정 규칙과 하네스 계약을 변경합니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임 동작을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP 계약을 변경하지 않습니다.

- [x] metadata-only 오류와 코드·정책 오류가 서로 다른 remediation budget을 사용합니다.
- [x] metadata-only 자동 복구는 고정 allowlist 안에서 Issue당 최대 2회만 허용합니다.
- [x] Main Coordinator는 저장소 파일을 수정하지 않고 원래 Dev 또는 Docs Agent에게 복구를 배정합니다.
- [x] STRICT Agent 수의 정본은 Dev, Review, QA, Docs 역할 4명이며 Main Coordinator와 CI를 제외합니다.
- [x] 범위 이탈, 정본 충돌, 반복 실패는 각각 명시적인 BLOCKED 경로로 안전 정지합니다.
- [x] 복구 뒤 새 HEAD에서 repository gate, fresh Review, fresh QA, 최신 CI를 다시 요구합니다.
- [x] 성공, PR 본문·verification log 동기화, 범위 이탈, 정본 충돌, 반복 실패, 코드 P1의 7개 시나리오를 계약 테스트로 고정합니다.
- [x] 기존 조건부 merge·close 안전 조건을 완화하지 않습니다.
- [x] official Reviewer 배정 전 pre-review metadata completeness를 필수화합니다.
- [x] Agent 수·테스트 수·HEAD·역할 링크의 일치, 누락 reference, 미실행 명령 주장, 범위 이탈, 정본 충돌의 9개 경로를 계약 테스트로 고정합니다.
- [x] 실행 순서를 Dev 검증, completeness/recovery, QA, Docs, fresh final Review, 최신 CI, merge로 고정합니다.
