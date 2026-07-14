# Issue Attempt Log

Issue: #92
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/92
Branch: claude/issue-92-merge-governance-baseline

Current disposition: BLOCKED
Current Attempt: 1
Current head: 미확정(Review·QA 전 draft)

## Attempt 1

### Generate

- 시작 시각은 기록하지 못해 `미측정`입니다.
- `gh issue list`로 #91 완료 뒤 열린 Issue를 확인했습니다. #93은 본문에 "현재 상태: 착수하지 않는다"고 명시돼 있어 제외하고, P1이자 독립 트랙인 #92를 착수했습니다.
- `gh issue view 92`로 본문을 확인했습니다. Execution mode 필드가 없어 `orchestration-policy.md`의 "merge 조건을 바꾸면 STRICT" 규칙에 따라 직접 STRICT로 판단했습니다.
- `docs/ai/harness-metrics-and-transfer.md`, `docs/ai/orchestration-policy.md`의 기존 "merge 거버넌스" 절을 읽어 중복을 피할 위치를 확인했습니다.
- `docs/ai/team-merge-governance-baseline.md`를 신규 작성했습니다: 현재 개인 저장소 현행, 팀 이전 시 branch rule 4항목(approval 1, stale dismissal, unresolved conversation 차단, CI required 유지), `gh issue view 56` 원문을 인용한 AI Review/QA required 미승격 근거, `gh issue view 93` 확인 뒤 #93과의 관계, 사람 전용 활성화 체크리스트(전부 미체크).
- `docs/ai/context-router.md`의 "조건부 참조 문서" 목록, `docs/ai/rule-source-map.md`의 정본 표, `docs/ai/orchestration-policy.md`의 merge 거버넌스 절에 각 1줄씩 새 문서로 향하는 참조 링크를 추가했습니다(정본 내용 복제 없음).

### Evaluate

- Level 0 하네스 회귀: `python -m pytest scripts/tests/ -q`가 문서 변경 전후 모두 160 passed, 121 subtests passed로 동일합니다(문서 전용 변경이라 예상대로 영향 없음).
- fresh Review Agent와 fresh QA Agent 결과는 아직 반영 전입니다. `Current disposition`은 두 결과가 확정되기 전까지 `BLOCKED`로 유지합니다.

### Failure Cause

- 없음(초안 단계). 독립 Review·QA 완료 전이라 PASS로 표시하지 않습니다.

### Change Scope

- `docs/ai/team-merge-governance-baseline.md`: 신규 작성.
- `docs/ai/context-router.md`, `docs/ai/rule-source-map.md`, `docs/ai/orchestration-policy.md`: 새 문서로 향하는 참조 링크 각 1줄 추가.
- `docs/testing/evidence/issue-92/`: 신규 evidence 6개 작성.
- 애플리케이션 코드, harness 스크립트, GitHub 저장소 설정은 변경하지 않았습니다.

### Reverification

- 독립 Review·QA 완료 뒤 이 절과 disposition·head를 갱신합니다.

### Next Attempt

- fresh Review Agent, fresh QA Agent dispatch 대기 중입니다.
