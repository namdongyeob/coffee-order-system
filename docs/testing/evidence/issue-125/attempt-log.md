# Issue Attempt Log

Issue: #125
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/125
Branch: codex/issue-125-ranking-ledger-retention
Current disposition: PASS
Current Attempt: 4
Current head: d976fe6b56e95061382edddbc209e1405af14e62

## Attempt 1

### Generate

- Generate start: 2026-07-18T13:35:26+09:00.
- 고정 `Clock`, retention 계약 validator, 한 batch scheduler, MySQL `FOR UPDATE SKIP LOCKED` 후보 잠금과 mutation-time predicate 재확인을 TDD로 구현했습니다.
- V7에 `(state, committed_at)` 인덱스를 추가하고 Redis marker TTL을 같은 설정 계약으로 이동했습니다.
- Kafka·DLT effective retention은 배포 환경에서 확인해 명시 설정하는 경로를 선택하고 운영 runbook에 확인 절차를 기록했습니다.

### Evaluate

- 각 신규 기능은 production 코드 전에 type 부재, assertion 불일치 또는 트랜잭션 부재의 RED를 실제로 확인했습니다.
- 신규 retention 패키지 focused, ADR-008/#119 회귀, 실제 Compose Level 5와 전체 clean 139/139가 PASS했습니다.
- 실제 scheduler는 적격 3건을 `2 -> 1 -> 0`으로 삭제하고 `RESERVED`와 `PREPARED` rebuild 연결 행을 보존했습니다.

### Failure Cause

- 기능 blocker는 없습니다.
- 한글 실제 경로에서 Gradle test worker가 새 테스트와 기존 테스트 모두 `ClassNotFoundException`을 낸 환경 경계를 확인했고, 같은 worktree를 ASCII `W:\`로 매핑한 뒤 정상 실행했습니다.
- 첫 Level 5 launcher는 Windows 인자 quoting 때문에 앱 속성을 Gradle 옵션으로 해석해 8초 만에 실패했습니다. 같은 값을 Spring 환경변수로 전달해 앱 기동과 검증을 완료했습니다.
- MySQL `datetime(6)` 경계 테스트의 1ns 값이 cutoff로 정규화된 테스트 오류는 DB 정밀도와 같은 1μs 경계로 수정했습니다.

### Change Scope

- ranking ledger retention production/test, 설정, V7 migration, Kafka·Redis 운영 문서와 `docs/testing/evidence/issue-125/**`만 변경했습니다.
- normal consumer/DLT/rebuild 상태 전이, event payload/topic, 최근 7일·Top 3 정책과 Redis marker `SCAN` cleanup은 변경하지 않았습니다.

### Reverification

- Reverification end: 2026-07-18T14:31:20+09:00.
- `W:\gradlew.bat test --tests "com.example.coffeeordersystem.ranking.retention.*" --no-daemon`: PASS.
- ADR-008/#119 bilateral·normal ledger·DLT 회귀 9건: PASS.
- 실제 Compose Level 5 bounded scheduler, pending 보존, marker 보존, invalid startup fail-closed: PASS.
- `W:\gradlew.bat clean test --no-daemon --max-workers=1 --console=plain`: PASS, 139/139, failures/errors/skipped 0, 3m 12s.

### Next Attempt

- Review P1/P2를 같은 Dev의 허용된 단 한 번 수정 반환으로 처리합니다.

## Attempt 2

### Generate

- Generate start: 2026-07-18T14:44:36+09:00.
- Review P1에 따라 cleanup 기본값을 비활성으로 바꾸고 Kafka·DLT·최대 rebuild recovery window의 안전 가정 기본값을 제거했습니다.
- 활성화된 cleanup은 세 외부 protection window가 명시되지 않으면 application context 생성 단계에서 fail-closed하도록 정책 경계를 수정했습니다.
- Review P2에 따라 production 후보 SQL에 cleanup 전용 인덱스 `FORCE INDEX`를 적용하고 테스트가 같은 `CANDIDATE_SQL` 문자열을 그대로 `EXPLAIN`하도록 변경했습니다.

### Evaluate

- RED에서 disabled cleanup도 null `kafka-retention` 때문에 거부되는 기존 정책을 재현했고, GREEN에서 disabled는 기동 가능하지만 enabled는 같은 누락을 거부했습니다.
- RED에서 테스트가 production 후보 SQL 상수를 찾지 못하는 컴파일 실패를 확인했고, GREEN에서 production SQL과 동일한 EXPLAIN이 cleanup 인덱스 `range` 접근을 확인했습니다.
- targeted 설정/SQL 실행계획 6건이 PASS했습니다. 전체 139 회귀는 요청대로 재실행하지 않았고 새 GitHub CI가 소유합니다.

### Failure Cause

- P1 원인은 외부 effective retention에 `30d` 기본값을 제공하고 cleanup도 기본 활성화해 미확인 값을 확인된 값처럼 취급한 것입니다.
- P2 원인은 테스트의 `FORCE INDEX`가 production SQL에 없어 실제 실행계획 근거가 아니었던 것입니다.

### Change Scope

- `application.properties`, retention policy/repository와 직접 테스트, Kafka·Redis runbook, Issue #125 evidence만 수정했습니다.
- cleanup predicate, scheduler batch, Redis marker 적용, normal/DLT/rebuild 상태 전이와 다른 테스트는 변경하지 않았습니다.

### Reverification

- Reverification end: 2026-07-18T14:51:37+09:00.
- 설정 fail-closed unit/context와 production SQL EXPLAIN targeted 6건: PASS, `BUILD SUCCESSFUL in 1m`.
- verified production head는 `da96594416d5286ea9a7e2675c5f5d316a2e5470`입니다.
- evidence-only commit 뒤 PR head는 이 SHA와 달라지며, 그 차이는 `docs/testing/evidence/issue-125/**`뿐입니다. full regression은 새 PR head CI가 판정합니다.

### Next Attempt

- Review가 반환한 disabled core 검증과 sub-second Redis `EX 0` P1 두 건을 사용자 승인 추가 Attempt에서 처리합니다.

## Attempt 3

### Generate

- Generate start: 2026-07-18T15:13:56+09:00.
- cleanup enabled 여부와 무관한 ledger/marker/fixed delay/batch core 안전성은 항상 검증하고, Kafka·DLT·최대 rebuild recovery window는 enabled일 때만 필수 검증하도록 경계를 분리했습니다.
- 초 단위 Redis `EX` 계약에 맞춰 양수이지만 1초 미만인 marker TTL은 application startup에서 fail-closed하도록 최소값을 추가했습니다.

### Evaluate

- `gh run view 29633092590 --log-failed`에서 141건 중 cleanup 동시성 1건이 `kafka-retention must be a known positive duration`으로 실패한 것을 확인했습니다.
- 같은 동시성 테스트 하나를 로컬에서 동일 stack과 message로 재현했습니다. disabled 설정의 `cleanupOneBatch()`가 외부 window까지 무조건 검증한 것이 직접 원인이었습니다.
- 두 P1 테스트는 production 수정 전 5건 중 신규 2건이 assertion RED였고, 수정 후 properties·configuration·동시성 focused 8건이 PASS했습니다.

### Failure Cause

- Policy가 `enabled=false`이면 `validate()` 전체를 건너뛰어 marker TTL 0·ledger보다 짧은 값·sub-second 값도 기동할 수 있었습니다.
- 반대로 `validate()` 내부는 외부 window를 무조건 요구해 disabled cleanup을 직접 호출하는 동시성 테스트와 core-only 설정이 실패했습니다.
- `Duration.toSeconds()`는 양수 sub-second marker를 0으로 내림해 Lua `SET ... EX 0`이 실패할 수 있으므로, 기존 초 단위 계약의 최소 1초를 core 경계에서 보장해야 했습니다.

### Change Scope

- `RankingLedgerRetentionPolicy`, `RankingLedgerRetentionProperties`, 직접 properties 테스트와 Issue #125 evidence만 수정했습니다.
- cleanup SQL/transaction/predicate, scheduler, Redis Lua, normal/DLT/rebuild 상태 전이와 다른 production/test/docs는 변경하지 않았습니다.

### Reverification

- Reverification end: 2026-07-18T15:18:03+09:00.
- properties 5건, configuration context 2건, CI 실패 동시성 1건의 focused 8건: PASS, `BUILD SUCCESSFUL in 1m 1s`.
- verified production head는 `2c1c378dc50380119f2728daa4859f982a5cae62`입니다.
- 전체 141 회귀는 로컬에서 재실행하지 않았고 evidence-only commit 뒤 최신 PR head GitHub CI가 소유합니다.

### Next Attempt

- 설정 Duration 비교와 Redis `EX` 정수 초 실효 TTL이 달라지는 마지막 P1을 사용자 승인 Attempt 4에서 처리합니다.

## Attempt 4

### Generate

- Generate start: 2026-07-18T15:30:00+09:00 (minute precision).
- Redis marker의 설정 Duration 대신 production `toSeconds()`와 같은 정수 초 실효 Duration을 ledger retention과 비교하도록 core 검증을 수정했습니다.
- `ledger-retention=1500ms`, `redis-marker-ttl=1500ms`처럼 설정상 같지만 Redis `EX`에서는 1초가 되는 값을 fail-closed합니다.

### Evaluate

- production 수정 전 properties 6건 중 신규 1500ms/1500ms 회귀 1건만 assertion RED였고 기존 core 5건은 유지됐습니다.
- 최소 수정 후 신규 회귀를 포함한 properties focused 6건이 failures/errors/skipped 0으로 PASS했습니다.

### Failure Cause

- 이전 검증은 원래 `Duration`끼리 비교했지만 Redis 적용 경로는 `toSeconds()`로 소수 초를 내림하므로 설정상 marker와 실제 marker 보호 시간이 달랐습니다.
- 1초 미만 차단만으로는 1500ms처럼 1초 이상이면서 fractional second인 값을 막지 못했습니다.

### Change Scope

- `RankingLedgerRetentionProperties`, 직접 properties 테스트와 Issue #125 evidence만 수정했습니다.
- `PopularMenuRankingService`, Redis Lua, Policy, cleanup, 다른 production/test/docs는 변경하지 않았습니다.

### Reverification

- Reverification end: 2026-07-18T15:32:56+09:00.
- `RankingLedgerRetentionPropertiesTest` focused 6건: PASS, failures/errors/skipped 0, `BUILD SUCCESSFUL in 25s`.
- verified production head는 `d976fe6b56e95061382edddbc209e1405af14e62`입니다.
- 전체 141 회귀와 Level 5는 로컬에서 재실행하지 않았고 evidence-only commit 뒤 최신 PR head GitHub CI가 소유합니다.

### Next Attempt

없음. Attempt 4 evidence/preflight 뒤 fresh Review·QA와 최신 PR-head CI를 다시 확인합니다.
