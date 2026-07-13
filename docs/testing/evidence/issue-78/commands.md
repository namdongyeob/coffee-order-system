# Issue #78 Commands

## Dev focused contract

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest -v` | P0 remediation을 포함한 orchestration contract focused suite | PASS. 42 tests, failures 0, errors 0. |
| `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest.test_role_packet_rejects_duplicate_or_missing_canonical_documents scripts.tests.test_harness_gate.OrchestrationContractTest.test_policy_is_the_single_execution_mode_contract -v` | 중복·부재 canonical 문서 경로 거부와 metadata 불일치 시에만 Docs Agent를 호출하는 STRICT 계약 | RED에서 각각 실패를 확인한 뒤 PASS. 2 tests, failures 0, errors 0. |

## 전체 harness

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python -m unittest scripts.tests.test_harness_gate -v` | 전체 harness suite | PASS. 90 tests, failures 0, errors 0. |

## Repository 검증

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python scripts/harness_gate.py --issue 78 --branch codex/issue-78-harness-lightweight --base-ref origin/main --check-links --include-worktree` | Issue evidence, changed Markdown link와 STRICT path gate | PASS. remediation head에서 재실행. |
| `git diff --check` | whitespace와 patch 정적 검사 | PASS. remediation head에서 재실행했고 working-copy CRLF 변환 예고 warning만 출력되었습니다. |
| `python scripts/harness_gate.py --issue 78 --pr-body-file C:\\Users\\user\\Documents\\coffee-order-system-issue-78-pr-body.md` | 실제 게시할 PR body preflight | PASS. UTF-8 BOM 없음 확인. 같은 파일을 `gh pr create --body-file`에 사용합니다. |
