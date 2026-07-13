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
| QA focused verification | 독립 QA의 Level 0 정적 계약·원문 보존 확인 | 21건 PASS. 원문 명령과 수행 시각은 QA 보고에 보존되지 않아 `미측정`입니다. head `b98b02e9c89b2d5f6a213de285338fcd7332e1f1`. |
| QA harness gate | 독립 QA의 Issue evidence·형식·변경 범위 확인 | PASS. 수행 시각은 `미측정`입니다. head `b98b02e9c89b2d5f6a213de285338fcd7332e1f1`. |
| QA 원본/재현 행 Counter 비교 및 rebuild | 독립 QA의 전역 뷰 원문 보존 재확인 | `base_rows=89`, `rebuilt_rows=90`, `missing=0`, rebuild PASS. 수행 시각은 `미측정`입니다. |
| QA `git diff --check` | 독립 QA의 공백 오류 확인 | PASS. 수행 시각은 `미측정`입니다. |
| `rg -n "docs/testing/verification-log\\.md" docs/operations/kafka-redis-runbook.md docs/product/github-issues.md` | P1 대상 문서의 삭제된 전역 로그 참조 검사 | 결과 0건. 두 문서는 Issue별 `verification.md` 정본과 Evidence Guide의 on-demand 전역 뷰 재현을 안내합니다. |
| `python scripts/harness_gate.py --issue 51 --branch codex/issue-51-verification-log-per-issue --base-ref 4b5fe36a0e875c6f0c9f2a3725de1ddeef2f0613 --check-links --include-worktree` | P1 수정 뒤 Issue evidence·모드·변경 Markdown 링크 검사 | PASS. |
| `git diff --check` | P1 수정 뒤 공백 오류 검사 | PASS. |
| `git show --stat f3979b0f1d595ed6ed6cc3bef1f0113ec7247126` | 사용자 승인 delta의 변경 범위 확인 | `README.md`만 2행 수정한 README-only commit임을 확인했습니다. |
| fresh Review at `f3979b0f1d595ed6ed6cc3bef1f0113ec7247126` | README-only delta의 독립 검토 | `APPROVED`. 수행 시각은 보존되지 않아 `미측정`입니다. |
| independent QA at `f3979b0f1d595ed6ed6cc3bef1f0113ec7247126` | README-only delta·정적 gate·링크의 독립 확인 | `PASS`. README-only delta verified, Issue harness gate PASS, `git diff --check` PASS, README Markdown 링크 존재를 확인했습니다. 수행 시각은 `미측정`입니다. |
| `python scripts/harness_gate.py --issue 51 --branch codex/issue-51-verification-log-per-issue --base-ref 4b5fe36a0e875c6f0c9f2a3725de1ddeef2f0613 --check-links --include-worktree` | Docs 동기화 전 Issue evidence·모드·변경 Markdown 링크 정적 검사 | PASS. Program/Gradle/runtime/API 테스트는 실행하지 않았습니다. |
| `git diff --check 4b5fe36a0e875c6f0c9f2a3725de1ddeef2f0613..HEAD` | Docs 동기화 전 base..head 공백 오류 검사 | PASS. |
| README Markdown 링크 확인 | 사용자 승인 README-only delta의 링크 대상 존재 확인 | `docs/testing/evidence-guide.md` 링크와 대상 파일이 존재합니다. |
