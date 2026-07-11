# Issue Metrics

Issue: #60
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/60
Branch: codex/issue-60-autonomous-queue-bootstrap
Measured at: 2026-07-12

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 3 | 2 | 1 | 2 | 0 | 0 | 4 |

## 측정 근거

- 작업 시간 표의 3분은 사용자 승인 최종 remediation Attempt의 실제 2분 55초를 분 단위 정수로 올림한 값입니다. 시작 `2026-07-12T08:52:56.7213632+09:00`, 종료 `2026-07-12T08:55:52.1026562+09:00`, 실제 소요 2분 55초입니다. 전체 Issue의 최초 Generate부터 마지막 Reverification까지의 총 경과 시간은 관찰하지 않아 추정하지 않습니다.
- Agent 수 4는 Dev, Docs, 독립 Reviewer, 독립 QA입니다. Main Coordinator는 작업 Agent 수에 포함하지 않습니다.
- 재시도 2는 첫 Reviewer P1 `REVISE`의 원래 Dev remediation 1회와, 두 번째 `REVISE` 안전 정지 뒤 사용자가 명시 승인한 propagation/metadata correction Attempt 1회입니다. 후자는 자동 재시도가 아닙니다. 최초 TDD RED와 #61-first scope correction RED는 Dev 반환이나 Review 결함이 아닙니다.
- Review 결함 수 2는 전역 무조건 merge·close 금지와 조건부 예외의 충돌 P1, 그리고 그 예외가 Skill에 전파되지 않은 P1입니다. QA 결함 수 0은 이전 QA가 결함을 반환하지 않았다는 뜻일 뿐, 최종 remediation 뒤 QA PASS를 뜻하지 않습니다.
- 범위 밖 변경 파일 0은 Dev diff와 Docs 허용 범위에서 확인된 이탈 파일이 없다는 뜻입니다.
- 직접 핵심 문서 4개는 `docs/ai/context-router.md`, `docs/testing/evidence-guide.md`, `docs/ai/rule-source-map.md`, `docs/ai/orchestration-policy.md`입니다.

## Evidence links

- Commands: `commands.md`.
- Attempts: `attempt-log.md`.
- Manual QA: `manual-qa.md`.
- Review/QA/CI: 두 번째 `REVISE` 안전 정지 뒤 사용자가 승인한 최종 remediation Attempt의 fresh Review·QA·CI가 pending입니다. 이전 QA는 stale입니다.
