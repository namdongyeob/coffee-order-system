# Issue #71 Commands

Issue: #71
Branch: codex/issue-71-workflow-rollback

실행하지 않은 명령은 기록하지 않습니다. GitHub의 현재 head, Review·QA·CI, merge 상태는 이 파일에 복제하지 않습니다.

## Dev Attempt 1

- 실행 head: `a6fac5d6bf9f72b986361065765d34524d5047a2` + 작업 diff.
- 명령: `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest`
  - 결과: RED FAIL, 26 tests 중 신규 helper 부재로 14 errors, exit 1.
  - 원문 위치: `attempt-log.md` Attempt 1.
- 명령: `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest`
  - 결과: GREEN PASS, 27 tests, exit 0.
  - 원문 위치: `attempt-log.md` Attempt 1.
- 명령: `python -m unittest scripts.tests.test_harness_gate`
  - 결과: PASS, 75 tests, exit 0.
  - 원문 위치: `attempt-log.md` Attempt 1.
- 명령: `python scripts/harness_gate.py --issue 71 --branch codex/issue-71-workflow-rollback --base-ref origin/main --check-links`
  - 결과: `Harness gate PASSED.`, exit 0.
  - 원문 위치: Dev 검증 기록.
- 명령: `git diff --check`
  - 결과: PASS, whitespace error 없음, exit 0.
  - 원문 위치: Dev 검증 기록.

## Dev Attempt 2

- 실행 head: `0bad6bc20bf0e1a1bf97977ecef46cebb81454e4` + remediation 작업 diff.
- 명령: `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest.test_qa_preservation_rejects_every_path_outside_fixed_markdown_allowlist`
  - 결과: RED FAIL, 1 test의 12 subcases 중 4 failures, exit 1.
  - 원문 위치: `attempt-log.md` Attempt 2.
- 명령: `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest`
  - 결과: GREEN PASS, 28 tests, exit 0.
  - 원문 위치: `attempt-log.md` Attempt 2.
- 명령: `python -m unittest scripts.tests.test_harness_gate`
  - 결과: PASS, 76 tests, exit 0.
  - 원문 위치: `attempt-log.md` Attempt 2.
- 명령: `python scripts/harness_gate.py --issue 71 --branch codex/issue-71-workflow-rollback --base-ref origin/main --check-links`
  - 결과: `Harness gate PASSED.`, exit 0.
  - 원문 위치: `attempt-log.md` Attempt 2.
- 명령: `git diff --check`
  - 결과: PASS, whitespace error 없음, exit 0.
  - 원문 위치: `attempt-log.md` Attempt 2.
- 명령: `python scripts/harness_gate.py --issue 71 --branch codex/issue-71-workflow-rollback --base-ref origin/main --check-links --pr-body-file C:\Users\user\AppData\Local\Temp\issue-71-pr-body.md`
  - 결과: 저장소 밖 UTF-8 no-BOM 한국어 PR body preflight PASS, exit 0.
  - 원문 위치: Dev 검증 기록.

## Independent QA

- 실행 head: `27944cdda9689240737edb39abfe32dac341128d`.
- 명령: `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest`
  - 결과: PASS, 28 tests, exit 0.
- 명령: `python -m unittest scripts.tests.test_harness_gate`
  - 결과: PASS, 76 tests, exit 0.
- 명령: `python scripts/harness_gate.py --issue 71 --branch codex/issue-71-workflow-rollback --base-ref origin/main --check-links`
  - 결과: repository gate와 링크 검사 PASS, exit 0.
- 명령: `git diff --check`
  - 결과: PASS, exit 0.
- 명령: `python scripts/harness_gate.py --issue 71 --branch codex/issue-71-workflow-rollback --base-ref origin/main --check-links --pr-body-file C:\Users\user\AppData\Local\Temp\issue-71-pr-body.md`
  - 결과: live 한국어 PR body의 저장소 밖 UTF-8 no-BOM preflight PASS, exit 0.
- 원문 위치: https://github.com/namdongyeob/coffee-order-system/pull/72#issuecomment-4950492060
