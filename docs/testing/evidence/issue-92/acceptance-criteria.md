# Issue #92 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/92

Execution mode: STRICT
Execution mode reason: `docs/ai/orchestration-policy.md`의 merge 거버넌스 절을 참조 보강하고, 팀 이전 시 활성화할 merge 조건(approval 수, stale dismissal, unresolved conversation 차단)을 정본화합니다. Issue 본문에 명시적 Execution mode 필드가 없어 직접 판단했습니다 — "Gate 판정 기준, 검증 소유권, 역할 권한, merge 조건, stale 규칙, 안전 불변조건을 바꾸면 STRICT"라는 `orchestration-policy.md` 자체 규칙에 해당하므로 STRICT를 선택했습니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP 계약을 변경하지 않습니다. 이 Issue는 GitHub 저장소 설정(branch protection)을 실제로 활성화하지 않고, 활성화 시점에 사람이 참고할 문서만 만듭니다.

## 완료 기준

- [x] 팀 이전 시 적용할 branch rule 항목(approval 1, stale, unresolved)이 문서로 확정된다. `docs/ai/team-merge-governance-baseline.md`의 "팀 이전 시 활성화할 branch rule" 절에 approval 최소 1명, stale approval dismissal, unresolved conversation 차단, CI required check 유지 4개 항목을 확정했습니다.
- [x] 개인 저장소 현행(PR + quality-gates 우선)과 팀 저장소 활성화 시점이 명확히 구분된다. 같은 문서의 "현재 개인 저장소(그대로 유지)" 절과 "팀 이전 시 활성화할 branch rule" 절을 별도 절로 분리했고, "활성화 방법과 소유권" 절에서 활성화가 사람의 명시적 행동이며 지금은 미실행임을 명시했습니다.
- [x] AI Review/QA check를 required로 만들지 않는 근거(#56)가 기록된다. "AI Review/QA를 required check로 만들지 않는 이유" 절에 Issue #56이 정의한 신뢰 경계(CI 실행만 ground truth, 로컬 evidence는 증명이 아님)를 인용해 기록했고, 조건부 재검토 대상인 Issue #93도 함께 링크했습니다.

이 Issue는 GitHub 저장소 설정(branch protection·ruleset)을 변경하지 않습니다 — 활성화는 사람이 직접 수행하는 별도 행동이며, 이 PR은 그 체크리스트만 만듭니다.

검증 실행 head는 아래 attempt-log.md와 verification.md를 참고합니다.
