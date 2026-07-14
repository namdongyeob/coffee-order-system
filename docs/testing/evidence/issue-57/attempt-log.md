# Issue Attempt Log

Issue: #57
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/57
Branch: claude/issue-57-level-mapping-replay

Current disposition: BLOCKED
Current Attempt: 1
Current head: 미확정(Review·QA 전 draft)

## Attempt 1

### Generate

- 시작 시각은 기록하지 못해 `미측정`입니다.
- `gh issue view 57`로 본문과 합의 코멘트를 확인하고 STRICT/Level 5·6 NO를 그대로 채택했습니다.
- `gh pr list --search "<N> in:body" --state merged`로 Issue #7·#8·#9·#40·#10의 구현 PR(#38·#39·#41·#42·#43)을 찾고 `gh pr diff --name-only`로 실제 변경 경로를 수집했습니다.
- 각 PR이 `docs/testing/verification-log.md`에 추가한 실측 Level PASS 행을 근거로 후보 매핑 M1~M8을 설계하고 ENFORCE/OBSERVE/DROP으로 분류했습니다.
- `**/event/**`를 넓게 매칭하면 Kafka와 무관한 `event/domain/ProcessedEvent.java`까지 걸리는 이름 충돌을 발견해 M3를 `order/event/**`로 좁혔습니다.
- M6(`**/service/**` → Level 4)은 5건 표본에서 4/4 일치했지만 표본 선택 편향 가능성이 있어 자동 ENFORCE로 승격하지 않고 사용자 보고 대상으로 남겼습니다.
- exemption code 5개와 역할별 승인 주체를 정의했습니다.
- `docs/testing/level-mapping-design.md`, `docs/testing/evidence/issue-57/{acceptance-criteria,commands,manual-qa}.md`를 작성했습니다.

### Evaluate

- Level 0 하네스 회귀: `python -m pytest scripts/tests/test_harness_gate.py -q`가 worktree base(`5859619`)에서 107건(110 subtests) PASS입니다.
- fresh Review Agent와 fresh QA Agent 결과는 아직 반영 전입니다. `Current disposition`은 두 결과가 확정되기 전까지 `BLOCKED`로 유지합니다.

### Failure Cause

- 없음(설계 산출물 초안 단계). 독립 Review·QA 완료 전이라 PASS로 표시하지 않습니다.

### Change Scope

- `docs/testing/level-mapping-design.md`: 신규 작성.
- `docs/testing/evidence/issue-57/`: 신규 evidence 6개 작성.
- 애플리케이션 코드, harness 스크립트(`scripts/harness_gate.py`)는 변경하지 않았습니다.

### Reverification

- 독립 Review·QA 완료 뒤 이 절과 disposition·head를 갱신합니다.

### Next Attempt

- fresh Review Agent, fresh QA Agent dispatch 대기 중입니다.
