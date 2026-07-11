# Issue Attempt Log

Issue: #44
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/44
Branch: codex/issue-44-harness-self-report-gates

## Attempt 1

### Generate

- Issue #44의 metrics 형식과 존재 여부, diff 기반 실행 모드 제약, PR 본문 3자 실행 모드 일치, workflow `edited` 트리거 계약을 하네스 코드와 단위 테스트에 추가했습니다.

### Evaluate

- Dev TDD RED에서 새 계약이 없던 상태를 확인했습니다. `python -m unittest scripts.tests.test_harness_gate`는 59 tests 중 14 errors와 2 failures였고, metrics/path/mode/workflow 계약이 누락된 결과였습니다.
- GREEN에서 `python -m py_compile scripts/harness_gate.py; python -m unittest scripts.tests.test_harness_gate`가 59 tests PASS했습니다.

### Failure Cause

- 없음. RED는 구현 전 계약 부재를 확인하기 위한 TDD 단계이며, GREEN 이후 동일 범위 재검증이 PASS했습니다.

### Change Scope

- `scripts/harness_gate.py`, `scripts/tests/test_harness_gate.py`, `.github/workflows/harness-quality.yml`과 Issue #44 evidence 및 verification log만 변경했습니다.

### Reverification

- Dev GREEN 59 tests, `python scripts/harness_gate.py --links-only --base-ref origin/main`, `git diff --check`가 PASS했습니다.
- Docs는 literal execution mode 필드가 포함된 임시 PR-body fixture로 `--pr-body-file` 모드 검사를 추가 실행해 PASS했습니다. CI, 독립 Review, 독립 QA는 아직 pending입니다.

### Next Attempt

- W2/W3 edited-trigger 증빙을 반영한 뒤 independent Review 재검토와 QA가 final HEAD에서 검증합니다.

## Attempt 2

### Generate

- Review FAIL P1에 따라 GitHub Actions의 실제 `pull_request.edited` 이벤트로 무효 PR 본문 FAIL과 유효 PR 본문 PASS를 관찰했습니다.

### Evaluate

- 기준 유효 PR 생성 run `29171462263`은 2m 40s PASS였습니다.
- W2 edited PR-body run `29171551064`은 Harness evidence gate에서 FAILURE였습니다. 의도한 변경은 `STRICT`에서 `SOLO`였지만, PowerShell 본문 문자열 갱신이 Markdown 줄바꿈도 평탄화했습니다. 따라서 CI 로그는 clean SOLO mismatch가 아니라 `Execution mode: SOLO|STANDARD|STRICT` 선언과 reason 누락을 보고했습니다.
- W3는 full multiline 유효 본문을 복원했고, 동일 HEAD `32e9510`에서 edited run `29171567906`이 1m 30s SUCCESS였습니다. 로컬 temp-file harness preflight도 PASS였습니다.

### Failure Cause

- W2의 실패 원인은 intended SOLO mismatch만이 아니라 PowerShell 문자열 갱신으로 인한 multiline Markdown 손상입니다. 이 run은 edited trigger와 invalid body FAIL은 증명하지만, clean SOLO mismatch assertion을 증명하지 않습니다.

### Change Scope

- 코드와 workflow는 변경하지 않았습니다. PR 본문은 W2에서 무효화했다가 W3에서 full multiline 유효 본문으로 복원했고, 이 evidence에는 관찰 결과와 한계만 추가합니다.

### Reverification

- W2/W3는 edited `pull_request` 이벤트에서 invalid-to-valid body FAIL→PASS를 확인했습니다. W3는 same code HEAD `32e9510`의 SUCCESS입니다.
- 이전 Review FAIL P1은 W2/W3 증빙으로 해결됐지만, final Review PASS는 재리뷰 전까지 주장하지 않습니다. QA와 CI 최종 상태도 별도 확인이 필요합니다.

### Next Attempt

- independent Review 재검토, QA, final CI 상태를 final HEAD 기준으로 확인합니다.
