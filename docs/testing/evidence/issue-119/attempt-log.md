# Issue Attempt Log

Issue: #119
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/119
Branch: codex/issue-119-ranking-ledger
Current disposition: PASS
Current Attempt: 2
Current head: 8506132df37034e31ee2e8037eb6a37dead2050f

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

- 독립 Review의 Redis marker 선기록 P1과 오래된 DLT 운영 문서 P2를 Attempt 2에서 처리합니다.

## Attempt 2

### Generate

- Review P1을 실제 Redis WRONGTYPE 회귀로 먼저 고정하고 Lua를 `ZINCRBY -> SET marker` 순서로 최소 수정했습니다.
- `scripts/README.md`와 Kafka·Redis runbook의 `processed_event`/`SKIPPED_ALREADY_PROCESSED` 설명을 공통 ledger 계약으로 갱신했습니다.

### Evaluate

- RED에서 Redis script runtime error 뒤 marker-only 잔존을 재현했습니다.
- GREEN에서 ledger `RESERVED`, marker 없음, 잘못된 key 제거 뒤 retry score 1·marker·`COMMITTED`를 확인했습니다.

### Failure Cause

- Lua가 marker `SET`을 `ZINCRBY`보다 먼저 실행해 Redis script의 비롤백 runtime error에서 marker-only 부분 적용이 가능했습니다.
- 운영 문서가 #119 이전의 `processed_event` 사전 판단 계약을 유지하고 있었습니다.

### Change Scope

- Lua 명령 순서 1줄, 실제 Redis 회귀 1건, 관련 운영 문서 2개만 변경했습니다.

### Reverification

- focused 12/12, 관련 clean 76/76, 전체 clean 126/126이 모두 failures/errors/skipped 0으로 통과했습니다.
- `V:\gradlew.bat clean test --no-daemon --max-workers=1 --console=plain`: BUILD SUCCESSFUL in 2m 46s.

### Next Attempt

- 없음. 최신 execution head에서 독립 Review·QA·CI를 다시 확인합니다.
