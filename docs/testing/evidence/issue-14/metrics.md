# Issue Metrics

Issue: #14
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/14
Branch: codex/issue-14-ranking-rebuild
Measured at: 2026-07-13

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 미측정 | 1 | 0 | 2 | 0 | 0 | 8 |

## 측정 근거

- STRICT Agent 수는 Dev, Review, QA, Docs 네 역할이며 Main Coordinator와 CI는 제외합니다.
- 최초 Generate의 초 단위 시작 시각을 그때 기록하지 않아 추정하지 않습니다. 확인 가능한 범위는 Attempt log의 첫 RED 명령 시각부터 마지막 Reverification 시각까지이며 종료 뒤 함께 기록합니다.
- 현재 수치는 첫 Fresh Review와 승인된 Dev remediation까지 반영했으며 QA 결함은 QA 수행 전이므로 0입니다.
- Fresh Review P1 두 건을 한 번의 승인된 Dev remediation으로 처리해 재시도 1회, Review 결함 2건으로 기록했습니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Runtime: `manual-qa.md`
