# Issue Metrics

Issue: #13
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/13
Branch: codex/issue-13-k6-scenarios
Measured at: 2026-07-13

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 37 | 3 | 2 | 4 | 0 | 0 | 11 |

## 측정 근거

- 작업 시간은 각 Attempt에 기록된 시작·종료 구간 합계 2,044.012초와 최종 Docs 동기화의 실제 관찰 구간 2026-07-13T09:36:38.9820605+09:00~2026-07-13T09:39:06.0268351+09:00, 147.045초만 합산한 36.52분입니다. canonical 정수 열에는 저장소 형식에 따라 37분으로 반올림했습니다. 역할 사이 대기 시간은 포함하지 않았고 추정값을 사용하지 않았습니다.
- STRICT Agent 수는 Dev, Review, QA, Docs 4개 역할이며 Main Coordinator와 CI를 제외합니다.
- Attempt는 최초 1회와 remediation 3회로 총 4회이고 재시도 수는 Attempt 2~4의 3회입니다. Review `BLOCKED` 안전 정지 2회 뒤 각각 사용자가 범위를 제한해 별도 remediation을 승인했습니다.
- Review 결함 수 4는 malformed JSON parse 누락 P1, actual `createOrder()`와 Rate recorder 연결 누락 P1, missing-field actual 경로 누락 P1, unknown profile 실행 계약 누락 P2입니다. P2는 비차단 권고이므로 수정하지 않고 남은 위험으로 유지합니다. QA 결함 수와 범위 밖 변경 파일 수는 각각 0개입니다.
- 읽은 핵심 문서는 Issue 본문, k6 계획·결과·README, 테스트 전략·evidence 안내, 요구사항·범위·주문·포인트·API 계약 11개입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Runtime: `manual-qa.md`
- Raw summaries: `*-output.txt`, `*-summary.json`
- Review/QA: PR 역할 보고 링크
