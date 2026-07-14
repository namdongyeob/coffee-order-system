# Issue Attempt Log

Issue: #91
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/91
Branch: claude/issue-91-parallel-orchestration-tool

Current disposition: BLOCKED
Current Attempt: 1
Current head: 미확정(Review·QA 전 draft)

## Attempt 1

### Generate

- 시작 시각은 기록하지 못해 `미측정`입니다.
- `gh issue view 91`로 본문을 확인했습니다(합의 코멘트 없음). Issue가 이미 1차/2차 범위를 명확히 나눠뒀지만 규모가 커서, 사용자에게 "1차 범위 그대로 한 PR"과 "설계 문서 먼저 분리" 중 선택을 물었고 "1차 범위 그대로 한 PR"로 확정받았습니다.
- `scripts/team_orchestration.py`를 신규 작성했습니다: `TeamOrchestrationConfig`(max_active_agents=3, max_writer_agents=2, ceiling 2로 3-Dev 예외 생성 시점 차단, `resolved()`로 CPU 자동 축소), `AgentAssignment`, `owned_paths_overlap`(디렉터리 포함 관계 휴리스틱), `TeamState`(register_agent/release_agent/send_message/metrics_snapshot/JSON 직렬화), CLI(`register`/`release`/`message`/`status`/`reset`).
- `.gitignore`에 `.team-orchestration-state/` 섹션을 추가해 runtime team-state가 커밋되지 않도록 했습니다.
- 콘솔 인코딩 크래시 방지를 위해 `harness_gate.py`의 `harden_console_encoding()` 패턴을 그대로 복제했습니다(import는 하지 않음 — 재사용성을 위해 두 스크립트를 서로 독립시킴).
- CLI로 menu/point 비중첩 등록, 3번째 writer BLOCKED, 겹치는 경로 SCOPE_CONFLICT, reader 슬롯 미소비를 수동 확인했습니다.
- `scripts/tests/test_team_orchestration.py`를 신규 작성했습니다: `ConfigLimitTest`, `OwnedPathOverlapTest`, `RegistrationTest`, `MessageProtocolTest`, `StatePersistenceTest`, `NoAutoMergeInvariantTest`, `SyntheticSmokeTest`(합성 독립 작업 2개 end-to-end + 겹치는 합성 작업 2개 반례).

### Evaluate

- Level 0 하네스 회귀: `python -m pytest scripts/tests/test_team_orchestration.py -q`가 30 passed, 6 subtests passed입니다. `python -m pytest scripts/tests/ -q`(harness_gate + team_orchestration 전체)는 160 passed, 121 subtests passed로 기존 130건에 회귀가 없습니다.
- fresh Review Agent와 fresh QA Agent 결과는 아직 반영 전입니다. `Current disposition`은 두 결과가 확정되기 전까지 `BLOCKED`로 유지합니다.

### Failure Cause

- 없음(초안 단계). 독립 Review·QA 완료 전이라 PASS로 표시하지 않습니다.

### Change Scope

- `scripts/team_orchestration.py`: 신규 작성.
- `scripts/tests/test_team_orchestration.py`: 신규 작성.
- `.gitignore`: `.team-orchestration-state/` 1개 섹션 추가.
- `docs/testing/evidence/issue-91/`: 신규 evidence 6개 작성.
- 애플리케이션 코드, `docs/` 정본, 기존 `scripts/harness_gate.py`는 변경하지 않았습니다.

### Reverification

- 독립 Review·QA 완료 뒤 이 절과 disposition·head를 갱신합니다.

### Next Attempt

- fresh Review Agent, fresh QA Agent dispatch 대기 중입니다.
