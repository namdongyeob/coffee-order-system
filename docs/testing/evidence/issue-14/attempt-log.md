# Issue #14 Attempt Log

Issue: #14
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/14
Branch: codex/issue-14-ranking-rebuild

## Attempt 1

### Generate

- Generate start: 2026-07-13T10:30:00+09:00. 첫 RED 명령 이전에 확인 가능한 분 단위 시작 범위입니다.
- `RankingRebuildServiceIntegrationTest` RED 뒤 maintenance guard, active group 검사, Redis lock, earliest replay와 end boundary, DB 비교, temp/backup Lua swap, 정상 group offset 이동을 구현했습니다.
- live Issue의 `[snapshot-7d,snapshot)`가 교차하는 distinct date key 전체를 동일 temp/live set으로 교체합니다.

### Evaluate

- PASS. focused 6건, related ranking suite, 전체 57건, Level 5 성공과 DB mismatch fail-closed가 통과했습니다.
- 중간 RED는 service 부재 compile 실패였습니다. ObjectMapper bean 부재와 기존 Redis slice의 Kafka placeholder 로딩 실패도 실제 출력으로 확인했습니다.

### Failure Cause

- 초기 RED는 production service가 아직 없어서 발생했습니다.
- 첫 GREEN context 실패는 ObjectMapper bean 부재였고, related context 실패는 일반 slice에도 rebuild service가 로드돼 Kafka placeholder를 요구했기 때문입니다.
- 기존 DLT test의 단발 timing 실패는 focused fresh rerun과 마지막 full에서 재현되지 않았습니다.

### Change Scope

- ranking rebuild service·runner·consumer startup flag, 관련 unit/integration test, recovery ADR·strategy·runbook과 Issue #14 evidence만 변경했습니다.

### Reverification

- Reverification end: 2026-07-13T11:28:02+09:00.
- focused 6건 PASS, related ranking suite PASS, 전체 57건 failures/errors 0, Level 5 success/mismatch/cleanup PASS입니다.

### Next Attempt

- Fresh Review의 P1 remediation.

## Attempt 2

### Generate

- Fresh Review의 offset 부분 성공·timeout 보상과 lock lease 만료 P1 두 건을 현재 head와 대조했습니다.
- pre-swap offset snapshot, 보상·broker 재조회, 위험 단계 lock renewal을 추가했습니다.

### Evaluate

- PASS. 실제 2-partition Kafka에서 부분 offset 변경 뒤 timeout, 정상 offset 복원, Redis rollback을 검증했습니다.
- offset 보상 자체 실패는 완전 rollback을 주장하지 않는 명시적 fail-closed exception으로 검증했습니다.

### Failure Cause

- 기존 구현은 Redis만 rollback했고 offset의 부분 성공·불확실 completion을 보상하지 않았습니다.
- lock은 token unlock만 있었고 긴 replay 뒤 위험 변경 직전 lease 소유권을 재확인하지 않았습니다.

### Change Scope

- Issue #14 ranking rebuild service, 최소 lock·offset component, 관련 test와 Issue #14 evidence만 변경했습니다.

### Reverification

- Focused 10 tests PASS, `BUILD SUCCESSFUL in 1m 29s`.
- Related ranking suite PASS, `BUILD SUCCESSFUL in 3m 43s`.
- 전체 61 tests, failures/errors 0, Level 5 local maintenance success와 cleanup이 PASS했습니다.

### Role verification history

- 첫 Fresh Review는 offset 부분 성공·timeout 보상 누락과 lock lease 만료의 P1 두 건, 비자정 snapshot 8개 날짜 경계 테스트 누락의 P2 한 건을 보고하고 `REVISE`로 판정했습니다.
- 같은 Dev가 허용된 한 번의 remediation에서 두 P1을 수정하고 P2 경계 테스트를 함께 추가했습니다.
- remediation 뒤 fresh read-only Review는 신규 P0/P1/P2 없이 승인된 정책과 보상·lock·날짜 경계 계약을 충족한다고 `APPROVED`로 판정했습니다.
- independent QA는 focused Level 4 10건을 1분 41초에 PASS했고, Level 5 성공 rebuild·DB mismatch fail-closed·cleanup을 독립 재검증해 `PASS`로 판정했습니다.

### Next Attempt

- 없음.
