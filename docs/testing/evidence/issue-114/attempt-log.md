# Issue Attempt Log

Issue: #114
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/114
Branch: codex/issue-114-final-verification
Current disposition: PASS
Current Attempt: 1
Current head: e9412ab3cc4ceb56de5b4ae9659a0e9e3a5d59ec

## Attempt 1

### Generate

- Generate start: 2026-07-18T15:54:54+09:00.
- clean production head에서 프로젝트 전용 compose volume을 초기화하고 MySQL·Redis·Kafka를 한 번 기동했습니다.
- local profile 앱을 한 번 기동한 뒤 실제 HTTP, DB·Kafka·Redis 관찰과 k6 safe Load·Stress·Spike를 순차 실행했습니다.
- 긴 앱 로그와 새 k6 summary JSON은 저장소 밖 `%TEMP%\coffee-order-issue-114\`에 저장하고 핵심 수치만 evidence에 옮겼습니다.

### Evaluate

- compose 3종은 모두 `healthy`, restart 0, exit 0, OOM false였습니다. 과거 `exit 255`는 이번 clean 실행에서 재현되지 않았습니다.
- Level 5 health, Level 6 성공 API 4종과 실패 응답 2종, Level 3·4 DB/Kafka/Redis 정합성이 모두 PASS했습니다.
- k6 세 시나리오는 모든 threshold를 통과했고 checks와 주문 성공률은 100%, HTTP·주문 오류율은 0%였습니다.
- 최종 drain에서 orders, Outbox, processed_event, `COMMITTED` ledger와 Redis score가 모두 517이고 Kafka lag는 0이었습니다.

### Failure Cause

- production, test 또는 runtime blocker는 없습니다.
- 첫 `docker compose up` wrapper는 PowerShell의 `$ErrorActionPreference=Stop`이 Docker stderr 진행 문구를 예외로 승격했지만 컨테이너는 정상 생성 중이었습니다. 재시작하지 않고 상태와 health를 확인했습니다.
- 첫 `curl.exe` POST 묶음은 Windows native argument quoting으로 JSON 따옴표가 손상돼 근거에서 제외했습니다. 새 synthetic user와 .NET `HttpClient`의 UTF-8 JSON body로 다시 실행해 계약 응답을 확인했습니다.
- 첫 ranking ledger 부가 조회는 존재하지 않는 `created_at` 정렬 컬럼으로 실패했습니다. 실제 스키마의 `reserved_at`으로 ledger 조회만 교정해 `COMMITTED` 상태를 확인했습니다.
- 초기 로그 진단은 PowerShell 비대소문자 매칭으로 INFO 본문의 소문자 `error`를 과다 집계했습니다. case-sensitive 로그 레벨 재검사 결과 실제 `ERROR`, `Caused by`, stack frame은 모두 0건이었습니다.

### Change Scope

- `docs/testing/evidence/issue-114/**` 6개 파일만 추가했습니다.
- production, test, build, runtime 설정, workflow, k6와 repository script는 수정하지 않았습니다.
- 발견 결과를 추측 수정하지 않았고 별도 Issue가 필요한 production 결함은 발견되지 않았습니다.

### Reverification

- Reverification end: 2026-07-18T16:03:47+09:00.
- actuator health는 최종 HTTP 200 `UP`, 앱 로그 actual ERROR level과 처리되지 않은 stack trace는 0건이었습니다.
- DB·Kafka·Redis 최종 drain은 `517 / 517 / 0 lag / score 517`로 일치했습니다.
- k6 Load·Stress·Spike는 각각 p95 `60.45ms / 59.74ms / 72.04ms`, error rate 0%, checks `158/158 / 591/591 / 815/815`로 PASS했습니다.
- cleanup 뒤 compose container·network·volume, 8080 listener와 worktree 앱 프로세스는 모두 0건이었습니다.
- 전체 Gradle 회귀는 요청과 테스트 전략에 따라 재실행하지 않았으며 최신 PR-head GitHub Actions `quality-gates`가 소유합니다.

### Next Attempt

없음. evidence commit과 PR body preflight 뒤 fresh Review·QA 및 최신 PR-head CI를 확인합니다.
