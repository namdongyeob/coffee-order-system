# Issue Attempt Log

Issue: #92
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/92
Branch: claude/issue-92-merge-governance-baseline

Current disposition: PASS
Current Attempt: 1
Current head: 4a2ef42bb9985e4e4bb24ad29fccd0089311c28c

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
- fresh Review Agent: **CHANGES_REQUESTED**(P1 1건). 내용 검증(정본 복제 없음, #56/#93 인용 정확, GitHub 설정 미변경, 체크리스트 전부 미체크, 테스트 회귀 없음)은 전부 통과했으나, evidence의 `Current head`/`Head`가 실제 SHA가 아니라 "미확정" placeholder로 남아 `harness_gate.py --check-links`가 재현 가능하게 FAIL한다는 점을 지적했습니다. STRICT 흐름상 Review 진입 전 Dev preflight가 PASS해야 한다는 근거로 반려했습니다.
- fresh QA Agent: **PASS**, P0/P1/P2 없음. 같은 시점(placeholder 상태)에서 독립 검증했고 문서 내용·인용 정확성·scope 전부 확인했습니다. QA는 placeholder를 "Review·QA 서명 전 의도된 상태"로 판단해 결함으로 세지 않았습니다.
- 두 역할의 판정 차이는 같은 사실(placeholder 상태)에 대한 STRICT 진입 시점 해석 차이였습니다. Review의 지적이 기계적으로 정확했으므로(실제 `harness_gate.py` FAIL 재현) 이 Attempt에서 `Current head`/`Head`를 실제 SHA로, `metrics.md`의 Review/QA 결함 수를 확정값으로 채워 반영했습니다(Bounded Retry 1회, 같은 Dev가 직접 수정).

### Failure Cause

- RED(경미, evidence 형식만): Review가 지적한 head/결함 수 placeholder. 코드나 문서 내용의 결함이 아니라 evidence 필드 미기입이었습니다. 이 Attempt에서 수정했습니다.

### Change Scope

- `docs/ai/team-merge-governance-baseline.md`: 신규 작성.
- `docs/ai/context-router.md`, `docs/ai/rule-source-map.md`, `docs/ai/orchestration-policy.md`: 새 문서로 향하는 참조 링크 각 1줄 추가.
- `docs/testing/evidence/issue-92/`: 신규 evidence 6개 작성 + head/결함 수 최종화.
- 애플리케이션 코드, harness 스크립트, GitHub 저장소 설정은 변경하지 않았습니다.

### Reverification

- `python -m pytest scripts/tests/ -q`는 head `4a2ef42`에서 160 passed, 121 subtests passed입니다.
- `python scripts/harness_gate.py --issue 92 --branch claude/issue-92-merge-governance-baseline --base-ref ad8dd22 --check-links --include-worktree`는 결과를 `commands.md`에 기록합니다.
- Level 5/6은 NO입니다. GitHub 저장소 설정은 변경하지 않았습니다.
- 종료 시각은 기록하지 못해 `미측정`입니다.

### Next Attempt

- 없음. fresh 독립 QA는 이번 Attempt 전체를 이미 PASS로 확인했습니다(내용 기준). Review가 지적한 evidence 형식 결함을 이 Attempt에서 수정했으므로 재확인이 필요하면 fresh Review를 다시 배정합니다.
