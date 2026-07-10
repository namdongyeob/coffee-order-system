# Issue Attempt Log

Issue: #29
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/29
Branch: codex/issue-29-harness-baseline

## Attempt 1

### Generate

- Legacy 경계, Issue 번호 연결, metrics template, 실제 하네스 실패 기록 조건, 동결 예외와 사람 승인 경계를 정본 네 곳에 반영했습니다.

### Evaluate

- PASS. #29의 문서 완료 기준을 변경 파일과 대조했고, 하네스와 정적 검증 결과는 `commands.md`에 기록합니다.

### Failure Cause

- 없음.

### Change Scope

- `docs/ai/orchestration-policy.md`, `docs/ai/agent-mistakes.md`, `docs/testing/evidence-guide.md`, `docs/testing/test-strategy.md`, metrics template, Issue #29 evidence와 verification log만 변경합니다.

### Reverification

- `commands.md`의 branch 성공·실패, metrics 링크, 정책 중복, harness gate, diff 검사를 실행합니다.

### Next Attempt

- Review FAIL의 허용된 1회 Dev 재시도에서 재현 조건, metrics 정수, Legacy 정본 중복만 수정합니다.

## Attempt 2

### Generate

- PR #31 failure log 확인 명령을 기록하고, metrics의 count 값을 0 이상의 정수로 정규화했으며, Legacy/backfill 설명은 Evidence Guide 정본만 유지했습니다.

### Evaluate

- Review FAIL의 세 지적사항만 수정했습니다. Review 재검토와 QA는 다음 역할의 독립 검증 대상입니다.

### Failure Cause

- Attempt 1은 PR #31 실패 기록에 재현 확인 명령이 없었고 metrics count가 정수 형식을 따르지 않았으며, Legacy/backfill 규칙을 test-strategy에 복제했습니다.

### Change Scope

- `agent-mistakes.md`, `test-strategy.md`, metrics template, Issue #29 metrics·attempt·commands·verification log만 수정합니다.

### Reverification

- `gh run view 29086275802 --repo namdongyeob/coffee-order-system --log-failed`, focused harness unit, repository gate, diff 검사를 실행합니다.

### Next Attempt

- 없음.
