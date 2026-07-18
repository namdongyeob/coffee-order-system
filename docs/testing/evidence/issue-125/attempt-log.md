# Issue Attempt Log

Issue: #125
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/125
Branch: codex/issue-125-ranking-ledger-retention
Current disposition: PASS
Current Attempt: 1
Current head: bd7f6e279f4746783546b73e70a7a5a92e40d7c3

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

없음. 최신 head에서 독립 Review·QA·CI를 후속 확인합니다.
