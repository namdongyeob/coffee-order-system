# Issue Metrics

Issue: #60
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/60
Branch: codex/issue-60-autonomous-queue-bootstrap
Measured at: 2026-07-12

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 미측정 | 1 | 0 | 1 | 0 | 0 | 4 |

## 측정 근거

- 작업 시간은 최초 Generate부터 마지막 Reverification까지의 분 단위 경과 시간입니다. 정확한 시작·종료 시각이 제공되지 않아 추정하지 않고 `미측정`으로 기록했습니다.
- Agent 수 4는 Dev, Docs, 독립 Reviewer, 독립 QA입니다. Main Coordinator는 작업 Agent 수에 포함하지 않습니다.
- 재시도 1은 Reviewer의 P1 `REVISE`가 원래 Dev의 허용된 1회 remediation으로 반환된 횟수입니다. 최초 TDD RED와 #61-first scope correction RED는 Dev 반환이나 Review 결함이 아닙니다.
- Review 결함 수 1은 전역 무조건 merge·close 금지와 조건부 예외의 충돌 P1입니다. QA 결함 수 0은 이전 QA가 결함을 반환하지 않았다는 뜻일 뿐, P1 remediation 뒤 QA PASS를 뜻하지 않습니다.
- 범위 밖 변경 파일 0은 Dev diff와 Docs 허용 범위에서 확인된 이탈 파일이 없다는 뜻입니다.
- 직접 핵심 문서 4개는 `docs/ai/context-router.md`, `docs/testing/evidence-guide.md`, `docs/ai/rule-source-map.md`, `docs/ai/orchestration-policy.md`입니다.

## Evidence links

- Commands: `commands.md`.
- Attempts: `attempt-log.md`.
- Manual QA: `manual-qa.md`.
- Review/QA/CI: P1 remediation 뒤 final fresh Review·QA·CI가 pending입니다. 이전 QA는 stale입니다.
