# Issue Metrics

Issue: #29
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/29
Branch: codex/issue-29-harness-baseline
Measured at: 2026-07-11

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 미측정 | 1 | 0 | 3 | 0 | 0 | 5 |

## 측정 근거

- Dev, Review, QA, Docs Agent의 실행 기록입니다. 최초 Review FAIL에서 수정 필요 3건이 반환됐고 최종 재검토는 PASS했습니다. QA는 HEAD `f3b8e03`에서 결함 0건으로 PASS했습니다.
- 재시도는 Review FAIL 뒤 허용된 1회 Dev 재시도이며, 정체는 없습니다.
- 읽은 핵심 문서는 `rule-source-map.md`와 정본 네 파일입니다. Issue 본문은 수에 포함하지 않습니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Review/QA: 최종 역할 보고는 `attempt-log.md`, 실행 명령과 결과는 `commands.md`에 기록
