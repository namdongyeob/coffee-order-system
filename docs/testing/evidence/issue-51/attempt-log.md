# Issue Attempt Log

Issue: #51
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/51
Branch: codex/issue-51-verification-log-per-issue

## Attempt 1

### Generate

- 시작 시각은 기록하지 못해 `미측정`입니다.
- 기존 전역 검증 로그를 Issue별 `verification.md`와 legacy 정본으로 원문 행 단위 이관했습니다.
- harness가 대상 Issue의 정본만 검사하고 on-demand 전역 뷰를 재현하도록 변경했습니다.
- P1 재시도에서 삭제된 전역 로그를 가리키는 runbook과 Issue 안내 문구를 Issue별 정본 경로로 정정합니다.

### Evaluate

- 진행 중입니다. P1 문서 경로 정정 뒤 focused 문서·링크 검사와 Issue gate를 다시 실행합니다.

### Failure Cause

- RED: 기존 harness는 전역 `docs/testing/verification-log.md`만 읽어 Issue별 정본 경로와 전역 뷰 재현 API가 없었습니다.

### Change Scope

- `scripts/` harness, migration/rebuild 스크립트와 테스트.
- `docs/testing/` evidence 구조·이관·전역 뷰 문서, Issue #51 evidence.
- P1 재시도는 `docs/operations/kafka-redis-runbook.md`, `docs/product/github-issues.md`, Issue #51 evidence만 수정합니다.

### Reverification

- `python -m unittest scripts.tests.test_harness_gate.VerificationLogValidationTest`는 19건 GREEN입니다.
- `python -m unittest scripts.tests.test_harness_gate`는 94건 GREEN입니다.
- Issue #51 repository gate와 `git diff --check`를 통과했습니다.
- 기준 전역 로그 88행은 모두 재현됐고, 전역 뷰의 추가 행은 현재 Issue #51의 1행뿐입니다.
- 종료 시각은 2026-07-13 16:14:04 +09:00입니다.
- 독립 QA는 focused 21건 PASS, harness gate PASS, `base_rows=89`, `rebuilt_rows=90`, `missing=0`, rebuild PASS, `git diff --check` PASS를 같은 head에서 확인했습니다.
- P1 대상 두 문서의 `docs/testing/verification-log.md` 참조는 0건이며 Issue gate와 `git diff --check`를 통과했습니다.

### Next Attempt

- P1 수정의 검증을 기록했습니다. commit, push한 뒤 fresh Review와 stale QA를 다시 요청합니다.
