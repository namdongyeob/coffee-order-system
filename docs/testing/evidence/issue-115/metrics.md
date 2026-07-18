# Issue Metrics

Issue: #115
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/115
Branch: codex/issue-115-troubleshooting-sync
Measured at: 2026-07-18

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STANDARD | 2 | 미측정 | 2 | 0 | 2 | 0 | 0 | 9 |

## 측정 근거

- Generate 시작 시각을 당시 기록하지 않아 작업 시간은 추정하지 않고 `미측정`으로 남깁니다.
- Agent 수 2는 STANDARD의 Dev와 독립 Combined Verifier입니다. Review P1 두 건을 원래 Dev가 Attempt 2에서 수정했습니다.
- 사용자 산출물 유형 정정까지 Attempt 3으로 기록해 재시도 2, 정체 0입니다.
- 범위 밖 production·test·build·workflow 변경은 0개입니다.
- 핵심 문서 수 9는 프로젝트·Issue loop·Router와 Docs/evidence hot path 4개, 직접 요구된 Issue #115를 셉니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Manual QA: `manual-qa.md`
- Verification: `verification.md`
- Review/QA: PR 생성 뒤 후속 역할 보고
