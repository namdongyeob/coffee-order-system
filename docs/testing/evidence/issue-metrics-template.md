# Issue Metrics

Issue: #N
Issue URL: https://github.com/OWNER/REPOSITORY/issues/N
Branch: prefix/issue-N-summary
Measured at: YYYY-MM-DD

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| SOLO\|STANDARD\|STRICT | 정수 | 정수 또는 미측정 | 정수 | 정수 | 정수 | 정수 | 정수 | 정수 |

## 측정 근거

- 작업 시간은 최초 Generate부터 마지막 Reverification까지의 분 단위 경과 시간입니다. 시작 또는 종료 시각이 없으면 `미측정`으로 기록합니다.
- 재시도는 `attempt-log.md`의 Attempt 중 최초 Attempt 뒤에 실행한 Attempt 수입니다.
- 정체는 declared stalled 또는 `BLOCKED`로 기록된 횟수입니다.
- Review·QA 결함 수는 해당 역할이 반환한 수정 필요 항목만 셉니다.
- 범위 밖 변경 파일 수는 허용 범위 밖으로 확인되어 제거 또는 후속 Issue로 분리한 파일 수입니다.
- 읽은 핵심 문서 수는 Context Router 또는 작업 증빙에 명시한 정본 문서 수입니다.

## Evidence links

- Commands: `commands.md`
- Attempts: `attempt-log.md`
- Review/QA: PR 또는 역할 보고 링크
