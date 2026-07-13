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

- P1 remediation 1회로 최소 packet schema의 allowlist와 3~5 canonical 문서 경로를 기계 계약으로 보강합니다.

## Attempt 2

### Generate

- PR #80 review의 P1을 확인했습니다. 기존 금지 key 목록이 `source_body`, `conversation_history`, `prompt` 같은 우회 key를 통과시키고 문서 경로 수를 검사하지 않았습니다.

### Evaluate

- 최소 packet 검증은 명시적인 allowlist가 필요하고 `required_documents`는 canonical repository-relative 경로 3~5개만 허용해야 합니다.

### Failure Cause

- 알려진 금지 key만 거부하는 방식은 새로운 payload key를 일반적으로 차단하지 못했습니다.

### Change Scope

- `scripts/harness_gate.py`, 직접 harness unit test, role packet 관련 문서와 Issue #78 evidence만 수정합니다.

### Reverification

- P0 remediation RED는 중복 canonical 문서 경로와 기본 Docs dispatch가 각각 실패한 것으로 확인했습니다.
- focused orchestration contract 42 tests와 전체 harness 90 tests가 PASS했습니다.
- repository gate, `git diff --check`, actual PR body preflight는 최종 실행 뒤 같은 head에서 기록합니다.

### Next Attempt

- 없음. 이 remediation은 허용된 1회 수정이며 fresh Review·independent QA·최신 CI와 mergeable 상태는 GitHub 정본에서 확인합니다.
