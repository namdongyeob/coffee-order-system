# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `gh issue view 58 --json title,body,comments,state` | Issue #58 본문·합의 코멘트 확인 | 접근 확정: #57 ENFORCE 매핑만 hard fail 구현, OBSERVE는 선택 측정, DROP 미구현, exemption은 고정 code만 허용. |
| `python -c "import ast; ast.parse(...)"` | `scripts/harness_gate.py` 문법 검증 | 오류 없음. |
| `python -m pytest scripts/tests/test_harness_gate.py -q` (구현 전 baseline) | 회귀 여부 확인 | 107 passed, 110 subtests passed. |
| `python3 -c "..."`(스크립트 내 `required_path_levels` 직접 호출) | M1·M2·M3 매핑과 `event/domain` 이름 충돌 회피, M8 test-only 제외를 실제 경로로 sanity-check | 4건 모두 설계대로 동작(M1→{2:[...]}, M2→{4:[...]}, M3→{4:[...]}(event/domain 제외), M8→{}). |
| `python -m pytest scripts/tests/test_harness_gate.py -q` (구현 후) | 새 `LevelPathEnforcementTest`(17개), `Issue57ReplayFixtureRegressionTest`(5개) 포함 전체 회귀 | 130 passed, 115 subtests passed. |
| `git add -A && git status --short` | 변경 범위 확인 | `scripts/harness_gate.py`, `scripts/tests/test_harness_gate.py` 2개만 수정. |
| `python scripts/harness_gate.py --issue 58 --branch claude/issue-58-level-gate-enforce --base-ref e27bb76 --check-links --include-worktree` | PR 전 preflight | Review·QA 결과와 exemption 포맷 정합화 커밋 반영 뒤 `Harness gate PASSED.`를 확인했습니다. |
