# 실행 명령과 결과

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python scripts/harness_gate.py --branch codex/issue-29-harness-baseline --check-branch` | 실제 Issue 번호 branch 허용 사례 | PASS. 종료 코드 0. |
| `python scripts/harness_gate.py --branch main --check-branch` | 보호 branch 거부 사례 | PASS. 의도대로 종료 코드 1과 protected branch 오류를 관찰했습니다. |
| policy duplicate heading PowerShell 검사 | orchestration policy 정본 제목 중복 검사 | PASS. 중복 `##` 제목이 없습니다. |
| metrics template PowerShell 검사 | 고정 열과 evidence-guide template 링크 검사 | PASS. 고정 9열과 상대 링크를 확인했습니다. |
| `python -m unittest scripts.tests.test_harness_gate` | 관련 전체 harness unit 검증 | PASS. 48건이 종료 코드 0으로 통과했습니다. |
| `python scripts/harness_gate.py --issue 29 --base-ref origin/main --check-links --include-worktree` | #29 evidence와 변경 Markdown 링크 검사 | PASS. 종료 코드 0. |
| `git diff --check` | 공백·패치 형식 정적 검사 | PASS. 오류 출력이 없었습니다. CRLF 변환 경고는 Git 작업 트리 경고이며 diff 오류가 아닙니다. |
| `gh run view 29086275802 --repo namdongyeob/coffee-order-system --log-failed` | PR #31 실제 harness 실패 재현 조건 확인 | PASS. 누락된 Execution mode·reason과 종료 코드 1을 확인했습니다. |
| `python -m unittest scripts.tests.test_harness_gate` | focused harness unit 재실행 | PASS. 48건이 종료 코드 0으로 통과했습니다. |
| `python scripts/harness_gate.py --issue 29 --base-ref origin/main --check-links --include-worktree` | Issue #29 repository gate 재실행 | PASS. 종료 코드 0. |
| metrics integer·Legacy canonical-link PowerShell 검사 | metrics count 정수와 Legacy/backfill 단일 정본 확인 | PASS. `STRICT | 2 | 미측정 | 1 | 0 | 3 | 0 | 0 | 5` 행과 Evidence Guide 링크를 확인했습니다. |
| `git diff --check` | Review FAIL 수정 후 공백·패치 형식 검사 | PASS. 오류 출력이 없었습니다. |
