# Issue Metrics

Issue: #60
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/60
Branch: codex/issue-60-autonomous-queue-bootstrap
Measured at: 2026-07-12

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 2 | 미측정 | 0 | 0 | 0 | 0 | 0 | 4 |

## 측정 근거

- 작업 시간은 최초 Generate부터 마지막 Reverification까지의 분 단위 경과 시간입니다. 정확한 시작·종료 시각이 제공되지 않아 추정하지 않고 `미측정`으로 기록했습니다.
- Agent 수 2는 Dev와 Docs입니다. 독립 Review·QA는 아직 실행 전이므로 포함하지 않았고, Main Coordinator는 작업 Agent 수에 포함하지 않습니다.
- 재시도 0은 구현 전 TDD RED가 정책 계약 부재를 확인한 단계이고 Dev 반환 후 수정 Attempt가 아니기 때문입니다.
- Review·QA 결함 수 0은 각 역할이 아직 실행 전임을 뜻하며 PASS 판정이 아닙니다.
- 범위 밖 변경 파일 0은 Dev diff와 Docs 허용 범위에서 확인된 이탈 파일이 없다는 뜻입니다.
- 직접 핵심 문서 4개는 `docs/ai/context-router.md`, `docs/testing/evidence-guide.md`, `docs/ai/rule-source-map.md`, `docs/ai/orchestration-policy.md`입니다.

## Evidence links

- Commands: `commands.md`.
- Attempts: `attempt-log.md`.
- Manual QA: `manual-qa.md`.
- Review/QA/CI: 별도 역할 보고와 PR 상태가 pending입니다.
