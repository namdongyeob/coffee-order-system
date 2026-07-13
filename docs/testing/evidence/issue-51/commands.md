# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python -m unittest scripts.tests.test_harness_gate.VerificationLogValidationTest` | Issue별 정본 전환 RED 확인 | 구현 전 19건 중 12 FAIL, 3 ERROR. `verification_file_path`, `rebuild_verification_log`, Issue별 정본 검사가 없었습니다. |
| `python -m unittest scripts.tests.test_harness_gate.VerificationLogValidationTest` | Issue별 정본과 원문 이관 focused GREEN | 구현 후 19건 PASS. |
| `python -m unittest scripts.tests.test_harness_gate` | harness·문서 계약 전체 회귀 | 94건 PASS. 두 브랜치 fixture의 공통 verification source 경로 0건을 포함합니다. |
| `python scripts/harness_gate.py --issue 51 --branch codex/issue-51-verification-log-per-issue --base-ref 4b5fe36a0e875c6f0c9f2a3725de1ddeef2f0613 --check-links --include-worktree` | Issue evidence·모드·변경 Markdown 링크 검사 | PASS. |
| `git diff --check` | 공백 오류 검사 | PASS. |
| 원본/재현 행 Counter 비교 | 기존 전역 행 원문 보존 확인 | 기존 88행 중 누락 0행. 재현 뷰의 추가 1행은 Issue #51 정본 행입니다. |
| `python scripts/harness_gate.py ... --pr-body-file $env:TEMP\issue-51-pr-body.md` | 같은 UTF-8 no-BOM 임시 PR 본문 preflight | PASS. draft PR 생성에 같은 파일을 사용합니다. |
| `git push -u origin codex/issue-51-verification-log-per-issue` | 검증된 commit 원격 게시 | PASS. head `e2ec4cb6314d8530e7bea1fdc835cd74f31a62c7`을 게시했습니다. |
| `gh pr create --draft --body-file $env:TEMP\issue-51-pr-body.md` | preflight한 동일 본문으로 draft PR 생성 | PASS. [PR #81](https://github.com/namdongyeob/coffee-order-system/pull/81)를 생성했습니다. |
