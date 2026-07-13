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

- 없음.
