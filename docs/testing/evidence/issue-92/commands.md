# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `gh issue list --repo namdongyeob/coffee-order-system --state open ...` | #91 완료 뒤 다음 열린 Issue 확인 | #92(P1, 병렬 도구와 독립 트랙), #93(P2, "현재 상태: 착수하지 않는다") 확인. #93은 본문이 보류 상태를 명시해 #92를 먼저 진행. |
| `gh issue view 92 --json title,body,comments` | Issue #92 본문 확인 | Execution mode 필드 없음 — 직접 STRICT로 판단(근거는 acceptance-criteria.md). |
| `gh issue view 56 --json body` | #92·#93이 인용하는 신뢰 경계 원문 확인 | "CI가 직접 실행한 명령·exit code = ground truth", "로컬 QA receipt = evidence이지 증명이 아님"을 그대로 인용. |
| `python -m pytest scripts/tests/ -q`(변경 전 baseline) | 회귀 여부 확인 | 160 passed, 121 subtests passed(#91 merge 후 상태와 동일). |
| `python -m pytest scripts/tests/ -q`(문서 변경 후) | 문서 전용 변경이 테스트에 영향 없는지 확인 | 160 passed, 121 subtests passed(변화 없음, 예상대로). |
| `python scripts/harness_gate.py --issue 92 --branch claude/issue-92-merge-governance-baseline --base-ref ad8dd22 --check-links --include-worktree` | PR 전 preflight(신규 문서의 링크 유효성 포함) | 결과는 `verification.md`에 기록. |
