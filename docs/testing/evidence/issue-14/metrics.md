# Issue Metrics

Issue: #14
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/14
Branch: codex/issue-14-ranking-rebuild
Measured at: 2026-07-13

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 미측정 | 1 | 0 | 3 | 0 | 0 | 8 |

## 측정 근거

- STRICT Agent 수는 Dev, Review, QA, Docs 네 역할이며 Main Coordinator와 CI는 제외합니다.
- 최초 Generate는 2026-07-13T10:30+09:00부터 첫 Attempt Reverification 2026-07-13T11:28:02+09:00까지 58분 범위를 확인할 수 있습니다. 그러나 두 번째 Attempt의 Generate 시작과 마지막 Reverification 종료를 당시 기록하지 않아 최초 Generate부터 마지막 Reverification까지의 실제 경과 시간을 계산하지 않고 `미측정`으로 유지합니다.
- Attempt 1 뒤 Fresh Review의 P1 두 건을 같은 Dev가 한 번의 remediation으로 처리했으므로 재시도 수는 1입니다.
- Review 결함 수 3은 offset 부분 성공·timeout 보상 누락 P1, lock lease 만료 P1, 비자정 snapshot 8개 날짜 경계 테스트 누락 P2입니다. QA는 새 결함 없이 PASS해 QA 결함 수는 0입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Runtime: `manual-qa.md`
