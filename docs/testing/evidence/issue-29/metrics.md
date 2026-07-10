# Issue Metrics

Issue: #29
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/29
Branch: codex/issue-29-harness-baseline
Measured at: 2026-07-11

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 2 | 미측정 | 1 | 0 | 3 | 0 | 0 | 5 |

## 측정 근거

- Dev와 Review Agent의 실행 기록 snapshot입니다. Review FAIL에서 수정 필요 3건이 반환됐고, QA는 아직 실행되지 않아 QA 결함 수는 정수 `0`이며 pending 상태를 추정하지 않았습니다.
- 재시도는 Review FAIL 뒤 허용된 1회 Dev 재시도이며, 정체는 없습니다.
- 읽은 핵심 문서는 `rule-source-map.md`와 정본 네 파일입니다. Issue 본문은 수에 포함하지 않습니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Review/QA: draft PR 생성 뒤 역할 보고 및 CI 상태를 본문에 기록
