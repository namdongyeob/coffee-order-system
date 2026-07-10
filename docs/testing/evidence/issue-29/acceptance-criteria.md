# Issue #29 완료 기준

- [x] `e5b47bd` 이전 merge의 Legacy 인정과 재작업 backfill 경계를 정본에 기록합니다.
- [x] 실제 GitHub Issue 번호, `issue-N` 브랜치, evidence 경로·본문 번호의 동일성 규칙을 기록합니다.
- [x] 반복 가능한 실제 하네스 실패만 `agent-mistakes.md`에 기록하는 조건을 기록합니다.
- [x] Issue별 metrics 위치와 고정 template을 추가합니다.
- [x] 하네스 동결 원칙과 실제 실패·지표·보안·호환성·명시 요구사항 예외를 orchestration policy에 기록합니다.
- [x] Agent의 merge·Issue close 금지와 사람 승인 경계를 유지합니다.

Execution mode: STRICT
Execution mode reason: Legacy 호환성, 실제 Issue 번호 연결, 측정 지표와 하네스 변경 허용 조건을 포함한 orchestration과 workflow policy 변경입니다.
Level 5 required: NO
Level 5 reason: Java 애플리케이션 런타임을 변경하지 않는 저장소 운영 정책 작업입니다.
Level 6 required: NO
Level 6 reason: 실제 HTTP API 계약이나 요청 흐름을 변경하지 않습니다.
