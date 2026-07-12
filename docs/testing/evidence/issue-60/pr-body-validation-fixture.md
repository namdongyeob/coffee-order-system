# Issue #60 PR body validation fixture

Related: #60

## 변경 내용

- `namdongyeob/coffee-order-system`에만 적용되는 고정 자율 Issue 큐와 조건부 merge 실험 정책을 정의했습니다.
- Issue #60은 bootstrap PR이므로 사람이 직접 merge하며 자동 merge·Issue close 대상이 아닙니다.
- 고정 큐는 `#61 -> #45 -> #55 -> #11 -> #21 -> #12 -> #13 -> #14 -> #15 -> #16 -> #51 -> #52 -> #53 -> #54 -> #56 -> #57 -> #58 -> #36`입니다. #61은 사람의 #60 merge 뒤에만, #45는 #61 완료 뒤에만 시작합니다.
- #61의 로컬 런타임·IntelliJ 구현은 이 PR 범위에 포함하지 않습니다.

## 최종 remediation 검증

- RED: `test_skill_keeps_default_coordinator_block_and_policy_merge_exception`은 기존 Skill에 정책 참조형 예외가 없어 FAIL했습니다.
- GREEN: 활성 정책 실험과 모든 정책 merge gate 입력을 참조하는 Main Coordinator 예외를 추가한 뒤 focused 계약 테스트가 PASS했습니다.
- `python -m unittest scripts.tests.test_harness_gate` 결과 62 tests OK.
- 실제 PR #62 본문과 일치하는 literal fixture를 포함한 Issue #60 gate, `git diff --check`, pre-push gate가 PASS했습니다.
- Level 5와 Level 6은 정책·하니스 작업이므로 실행하지 않았습니다.

## Evidence

- `docs/testing/evidence/issue-60/acceptance-criteria.md`
- `docs/testing/evidence/issue-60/attempt-log.md`
- `docs/testing/evidence/issue-60/commands.md`
- `docs/testing/evidence/issue-60/manual-qa.md`
- `docs/testing/evidence/issue-60/metrics.md`

## 남은 독립 Gate

- 두 번째 `REVISE` 안전 정지 뒤 사용자 승인 최종 remediation Attempt의 fresh Review pending.
- Independent QA pending.
- 최신 HEAD의 CI pending.
- Draft PR은 완료, ready 전환, merge 또는 Issue close를 의미하지 않습니다.

Execution mode: STRICT
Execution mode reason: Agent의 merge·Issue close 권한, 역할 격리, Review 재시도, Issue 큐 운영을 변경하는 workflow policy 작업입니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP API 계약과 요청 흐름을 변경하지 않습니다.
