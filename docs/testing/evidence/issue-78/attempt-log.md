# Issue #78 Attempt Log

Issue: #78
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/78
Branch: codex/issue-78-harness-lightweight

## Attempt 1

### Generate

- 시작: 2026-07-13T15:30:36.9501401+09:00.
- live Issue #78과 worktree 정책을 읽고, 허용된 harness·직접 단위 테스트·policy·role packet template·Issue evidence만 변경 범위로 고정했습니다.

### Evaluate

- 기존 harness는 QA stale allowlist와 pre-review만 검사했고, 최소 packet, post-QA 무변경, 검증 소유권과 flaky 안전 정지의 12개 계약은 없었습니다.

### Failure Cause

- 네 가지 경량화가 문서 서술에만 남으면 역할 입력 축소와 stale·flaky 경계가 재발할 수 있어 기계 계약으로 고정할 필요가 있었습니다.

### Change Scope

- `scripts/harness_gate.py`, 직접 harness 단위 테스트, orchestration/agent 규칙, role packet template와 Issue #78 evidence만 변경했습니다.
- production/runtime, Gradle/build/CI workflow와 애플리케이션 테스트 suite는 변경하지 않았습니다.

### Reverification

- focused orchestration contract 40 tests는 PASS했습니다.
- 전체 harness 88 tests, repository gate, `git diff --check`, 저장소 밖 UTF-8 no-BOM PR body preflight는 PASS했습니다.

### Next Attempt

- 없음. fresh Review·independent QA·최신 CI와 mergeable 상태는 GitHub 정본에서 후속 역할이 확인합니다.
