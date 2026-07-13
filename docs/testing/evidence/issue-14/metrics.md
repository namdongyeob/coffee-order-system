# Issue Metrics

Issue: #14
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/14
Branch: codex/issue-14-ranking-rebuild
Measured at: 2026-07-13

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 249 | 2 | 1 | 4 | 0 | 0 | 8 |

## 측정 근거

- STRICT Agent 수는 Dev, Review, QA, Docs 네 역할이며 Main Coordinator와 CI는 제외합니다.
- 최초 Generate start `2026-07-13T10:30:00+09:00`부터 마지막 Dev Reverification end `2026-07-13T14:39:13+09:00`까지 분 단위 내림 249분을 기록했습니다. 중간 안전 정지와 Issue #77 대기·merge 재개 시간도 template의 경과 시간 정의에 따라 포함합니다.
- Attempt 1 뒤 첫 Review remediation과 두 번째 Review의 마지막 retention remediation을 각각 1회로 계산해 재시도 수는 2입니다.
- 정체 수 1은 범위 밖 DLT timing failure가 전체와 격리에서 반복돼 Issue #77 merge까지 안전 정지한 건입니다.
- Review 결함 수 4는 offset 부분 성공·timeout 보상 누락 P1, lock lease 만료 P1, 비자정 snapshot 8개 날짜 경계 테스트 누락 P2, offset 0 전제 P1입니다. QA는 현재까지 새 결함 없이 PASS해 QA 결함 수는 0입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Runtime: `manual-qa.md`
