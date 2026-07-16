# Issue Attempt Log

Issue: #119
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/119
Branch: codex/issue-119-ranking-ledger
Current disposition: PASS
Current Attempt: 1
Current head: 45b3a3f8686e2e469e029d6bb0846c8910bcfc28

## Attempt 1

### Generate

- Generate start: 2026-07-16T22:03:49+09:00.
- ADR-008과 merged #112 상태 전이를 기준으로 Redis applied-event marker, normal ledger 적용, DLT recovery guard, 양방향 통합 테스트를 TDD로 구현했습니다.

### Evaluate

- 관련 clean 75/75와 ASCII 경로 전체 clean 125/125를 통과했습니다.
- 실제 Compose에서 normal, DLT→Rebuild, Rebuild→DLT, pending retryable 차단을 확인했습니다.

### Failure Cause

- 기능 결함은 남지 않았습니다.
- 한글 cwd에서 Gradle test worker가 모든 테스트 class를 찾지 못한 환경성 실패가 있었고, ASCII subst `V:\`에서 동일 전체 clean을 재실행해 125/125 PASS로 판별했습니다.

### Change Scope

- normal ranking consumer, 공통 ranking ledger 단계 적용, Redis applied-event Lua, DLT replay recovery guard와 관련 테스트·evidence만 변경했습니다.
- #112 Rebuild bulk backfill·swap 알고리즘, payload/topic, ranking 조회 정책은 변경하지 않았습니다.

### Reverification

- `V:\gradlew.bat clean test --no-daemon --max-workers=1 --console=plain`: PASS, 125/125, failures/errors/skipped 0, BUILD SUCCESSFUL in 2m 45s.
- Level 5 actual Compose: 양방향 모두 menu별 score 1, pending runner는 retryable fail-closed, 원본 offset과 score 불변, lock cleanup PASS.

### Next Attempt

- Dev 재시도는 없습니다. Ready PR에서 독립 Review·QA·CI를 수행합니다.
