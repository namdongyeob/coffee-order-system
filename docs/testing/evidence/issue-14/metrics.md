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
- Review 결함 수 4는 실제 수정을 요구한 offset 부분 성공·timeout 보상 누락 P1, lock lease 만료 P1, 비자정 snapshot 8개 날짜 경계 테스트 누락 P2, offset 0 전제 P1입니다. 마지막 `APPROVED` Review의 비차단 P2 세 건은 완료 기준을 뒤집거나 코드 수정을 요구하지 않은 권고이므로 template의 `수정 필요 항목`에 포함하지 않습니다.
- QA 결함 수 0은 코드·기능 결함이 없다는 뜻입니다. 첫 focused 결과 폐기와 두 번째 focused 중단은 실행 환경·사용자 승인 경량화 이력이며 결함으로 집계하지 않습니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Runtime: `manual-qa.md`
- Review: https://github.com/namdongyeob/coffee-order-system/pull/76#issuecomment-4954809228
- QA: https://github.com/namdongyeob/coffee-order-system/pull/76#issuecomment-4954888271
