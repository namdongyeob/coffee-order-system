# Issue Metrics

Issue: #21
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/21
Branch: codex/issue-21-point-concurrency
Measured at: 2026-07-12

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 37 | 1 | 0 | 1 | 1 | 0 | 11 |

## 측정 근거

- 작업 시간은 최초 Generate `2026-07-12T18:58:41.6639074+09:00`부터 마지막 Reverification `2026-07-12T19:35:16.4749098+09:00`까지 실제 36분 34.811초이며 분 단위 표에는 올림해 37분으로 기록했습니다.
- STRICT Agent 수는 Dev, Review, QA, Docs 역할 수 4이며 Main Coordinator와 CI는 제외합니다.
- 재시도 1은 `attempt-log.md`의 최초 Attempt 뒤 실행한 Attempt 2입니다. 첫 Attempt 안에서 폐기한 `insert ignore` 설계와 테스트 격리 수정은 별도 recovery Attempt로 세지 않았습니다.
- Review 결함 1은 fresh code Review가 발견한 `REQUIRES_NEW` 누락 P1입니다.
- QA 결함 1은 실제 PR 본문의 자동 close 문구를 발견한 metadata QA FAIL입니다. 기능 테스트 재실행이 필요한 코드 결함으로 계산하지 않았습니다.
- 정체와 범위 밖 변경 파일은 0입니다.
- 읽은 핵심 문서 11개는 요구사항, 범위, 주문 정책, 포인트 정책, API 명세, 동시성 전략, DB 잠금 ADR, 도메인 규칙, 테스트 전략, evidence 안내, orchestration policy입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Review: https://github.com/namdongyeob/coffee-order-system/pull/73#issuecomment-4950874265
- QA: https://github.com/namdongyeob/coffee-order-system/pull/73#issuecomment-4950897839
