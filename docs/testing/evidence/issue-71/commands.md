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
