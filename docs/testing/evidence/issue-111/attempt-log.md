# Issue Attempt Log

Issue: #111
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/111
Branch: issue-111
Current disposition: PASS
Current Attempt: 1
Current head: fb4fdd3eabf5e506b1e08071fbf08b81451f3f85

## Attempt 1

### Generate

- `ADR-008`에 기존 `processed_event` 재사용을 제외하고, popularity ranking 전용 DB ledger와 Redis 원자 marker를 함께 사용하는 결정을 기록했습니다.
- normal consumer, DLT replay, rebuild swap, pending rebuild recovery와 구현 acceptance criteria를 한 ADR에 기록했습니다.

### Evaluate

- PASS. execution head `fb4fdd3eabf5e506b1e08071fbf08b81451f3f85`에서 새 ADR의 모든 Markdown 링크가 유효함을 확인했습니다.

### Failure Cause

- 없음.

### Change Scope

- `docs/adr/ADR-008-ranking-recovery-ledger.md`와 Issue #111 evidence만 변경합니다.
- Java production/test, migration, Kafka·Redis·DLT runtime 설정과 event payload는 변경하지 않습니다.

### Reverification

- `python scripts/harness_gate.py --links-only --base-ref origin/main --include-worktree`가 PASS했습니다.

### Next Attempt

- 없음.
