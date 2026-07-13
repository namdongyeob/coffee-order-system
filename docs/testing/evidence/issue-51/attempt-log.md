# Issue Attempt Log

Issue: #51
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/51
Branch: codex/issue-51-verification-log-per-issue

## Attempt 1

### Generate

- 시작 시각은 기록하지 못해 `미측정`입니다.
- 기존 전역 검증 로그를 Issue별 `verification.md`와 legacy 정본으로 원문 행 단위 이관했습니다.
- harness가 대상 Issue의 정본만 검사하고 on-demand 전역 뷰를 재현하도록 변경했습니다.

### Evaluate

- PASS. focused RED를 재현한 뒤 Issue별 정본, 원문 보존 이관, 전역 뷰 재현을 구현했고 전체 harness 회귀를 통과했습니다.

### Failure Cause

- RED: 기존 harness는 전역 `docs/testing/verification-log.md`만 읽어 Issue별 정본 경로와 전역 뷰 재현 API가 없었습니다.

### Change Scope

- `scripts/` harness, migration/rebuild 스크립트와 테스트.
- `docs/testing/` evidence 구조·이관·전역 뷰 문서, Issue #51 evidence.

### Reverification

- `python -m unittest scripts.tests.test_harness_gate.VerificationLogValidationTest`는 19건 GREEN입니다.
- `python -m unittest scripts.tests.test_harness_gate`는 94건 GREEN입니다.
- Issue #51 repository gate와 `git diff --check`를 통과했습니다.
- 기준 전역 로그 88행은 모두 재현됐고, 전역 뷰의 추가 행은 현재 Issue #51의 1행뿐입니다.

### Next Attempt

- 동일한 UTF-8 no-BOM 임시 PR 본문 preflight를 통과했습니다. commit, push, draft PR을 생성합니다.
