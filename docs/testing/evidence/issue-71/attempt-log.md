# Issue #71 Attempt Log

Issue: #71
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/71
Branch: codex/issue-71-workflow-rollback

## Attempt 1

### Generate

- 역할: Dev
- 시작 시각: 2026-07-12T16:55:28.2765399+09:00
- 기준 head: `a6fac5d6bf9f72b986361065765d34524d5047a2`
- #66의 metadata recovery budget, pre-review 미래 링크 의존성과 문자열 계약을 제거하고 경량 workflow 행위 계약을 작성했습니다.

### Evaluate

- TDD RED: 신규 helper가 없는 상태에서 focused suite가 26 tests 중 신규 helper 부재 14 errors로 실패했습니다.
- GREEN: 행위 계약 focused 27 tests와 전체 harness 75 tests가 PASS했습니다.

### Failure Cause

- 경량 pre-review, 역할 수, QA head 유효성, merge·next Issue 판정 helper가 아직 구현되지 않았습니다.

### Change Scope

- #66 직접 연결 policy·Skill·agent rules·evidence guide, harness helper와 계약 테스트, Issue #71 evidence만 변경합니다.

### Reverification

- 종료 시각: 2026-07-12T17:01:07.9032630+09:00
- `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest`: 27 tests PASS.
- `python -m unittest scripts.tests.test_harness_gate`: 75 tests PASS.
- Issue #71 repository gate와 `git diff --check`: PASS.

### Next Attempt

- fresh Review와 independent QA를 위한 draft PR 생성.

## Attempt 2

### Generate

- 역할: 원래 Dev의 허용된 유일 code remediation
- 시작 시각: 2026-07-12T17:06:29.8802961+09:00
- 기준 head: `0bad6bc20bf0e1a1bf97977ecef46cebb81454e4`
- Review P1에 따라 QA 보존 경로를 고정 Markdown evidence 파일과 verification-log로 제한했습니다.

### Evaluate

- TDD RED: screenshot, png, raw output와 임의 Markdown 4개 경로가 기존 prefix 판정에서 잘못 `True`가 되어 4 failures를 확인했습니다.
- GREEN: 고정 파일 allowlist 판정과 명시 우회 행위 테스트가 focused suite에서 PASS했습니다.

### Failure Cause

- `qa_remains_valid()`가 Issue evidence 디렉터리 prefix 전체를 허용해 비문서 artifact도 QA 보존 delta로 오인했습니다.

### Change Scope

- `scripts/harness_gate.py`의 QA 보존 predicate, 직접 계약 테스트, 같은 정책 문장과 Issue #71 evidence만 수정합니다. P2 CLI 연결은 제외합니다.

### Reverification

- 종료 시각: 2026-07-12T17:08:17.9396837+09:00
- focused 28 tests, full harness 76 tests, repository gate, diff check와 저장소 밖 UTF-8 no-BOM PR body preflight가 모두 PASS했습니다.

### Next Attempt

- 같은 PR에 push한 뒤 fresh Review와 independent QA를 요청합니다. 추가 code remediation은 없습니다.

## Attempt 3

### Generate

- 역할: Docs
- 시작 시각: 2026-07-12T17:16:14.2428925+09:00
- 기준 head: `27944cdda9689240737edb39abfe32dac341128d`
- 입력: live Issue #71, PR #72 전체 diff, Dev evidence, 지정 Review와 independent QA 원문.
- 허용 범위: `docs/testing/evidence/issue-71/`의 고정 Markdown 5개와 `docs/testing/verification-log.md`만.

### Evaluate

- 단일 정본에 맞춰 acceptance criteria, 실제 Dev·QA 명령, Level 5/6 경계, 9열 metrics와 최종 repository 검증 행을 한 번 동기화합니다.
- GitHub가 소유하는 현재 head, Review·QA·CI, merge 상태는 repository snapshot으로 복제하지 않습니다.

### Reverification

- 종료 시각: 2026-07-12T17:16:14.2428925+09:00
- 결과: Docs final sync 대상은 고정 evidence 5개와 verification log 1개로 제한했습니다.

### Next Attempt

- final Reviewer가 QA head 이후 docs-only delta를 검토합니다. repository evidence는 이후 다시 수정하지 않습니다.
