# Issue #35 완료 기준

- [x] `STANDARD` Combined Verifier의 PR 전후 수행 시점과 외부·내부 독립 형태를 정본에 기록합니다.
- [x] 독립 검증이 pending인 draft PR을 완료나 ready 상태로 판단하지 않는 Gate를 기록합니다.
- [x] 하네스·스크립트 Context Router hot path를 필수 문서 4개와 조건부·제외·추가 탐색 규칙으로 추가합니다.
- [x] Review Gate와 QA Gate의 판정 기준 자체를 바꾸는 작업을 `STRICT`로 분류합니다.
- [x] 정책 결정과 PR #32·#33의 반복 관찰을 Issue evidence에 연결합니다.

Execution mode: STRICT
Execution mode reason: Combined Verifier 시점, Context Router와 Gate 변경 모드를 다루는 workflow policy 변경입니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임을 변경하지 않는 문서·저장소 하네스 계약 작업입니다.
Level 6 required: NO
Level 6 reason: HTTP API 계약과 실제 요청 흐름을 변경하지 않습니다.
