# Issue #71 Commands

Issue: #71
Branch: codex/issue-71-workflow-rollback

## TDD RED

- 명령: `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest`
- 기준 head: `a6fac5d6bf9f72b986361065765d34524d5047a2` + 작업 diff
- 결과: FAIL. 26 tests에서 신규 helper 부재로 14 errors를 확인했습니다.

## 최종 검증

- 실행 기준: `a6fac5d6bf9f72b986361065765d34524d5047a2` + Issue #71 작업 diff.
- 명령: `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest`
  - 결과: PASS, 27 tests.
- 명령: `python -m unittest scripts.tests.test_harness_gate`
  - 결과: PASS, 75 tests.
- 명령: `python scripts/harness_gate.py --issue 71 --branch codex/issue-71-workflow-rollback --base-ref origin/main --check-links`
  - 결과: `Harness gate PASSED.`
- 명령: `git diff --check`
  - 결과: PASS, whitespace error 없음. 출력의 LF→CRLF 경고는 Windows working-tree 변환 안내입니다.
- 명령: `git status --short --branch`, `git diff --name-only`, `git ls-files --others --exclude-standard`
  - 결과: Issue #71 허용 범위만 변경됐고 `src/**`, application test, build/runtime/workflow 파일은 없습니다.

## Code remediation TDD RED

- 실행 기준: `0bad6bc20bf0e1a1bf97977ecef46cebb81454e4` + remediation 작업 diff.
- 명령: `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest.test_qa_preservation_rejects_every_path_outside_fixed_markdown_allowlist`
- 결과: FAIL, 1 test의 12개 subcase 중 screenshot, png, raw output, 임의 Markdown 4개 경로가 잘못 허용되어 4 failures.

## Code remediation GREEN

- 명령: `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest`
- 결과: PASS, 28 tests.
- 명령: `python -m unittest scripts.tests.test_harness_gate`
  - 결과: PASS, 76 tests.
- 명령: `python scripts/harness_gate.py --issue 71 --branch codex/issue-71-workflow-rollback --base-ref origin/main --check-links`
  - 결과: `Harness gate PASSED.`
- 명령: `git diff --check`
  - 결과: PASS, whitespace error 없음.
- 명령: `python scripts/harness_gate.py --issue 71 --branch codex/issue-71-workflow-rollback --base-ref origin/main --check-links --pr-body-file C:\Users\user\AppData\Local\Temp\issue-71-pr-body.md`
  - 결과: 저장소 밖 UTF-8 no-BOM 한국어 경량 body preflight PASS.
- 명령: `git diff --name-only`, `git status --short --branch`
  - 결과: 허용된 predicate·계약 테스트·직접 policy 문장과 Issue #71 evidence만 변경됐습니다.
