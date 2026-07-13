# 검증 로그

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-11 | Issue #29 harness baseline policy | Level 0 | PASS | 문서·정적·하네스·도구 | branch guard 허용·거부, policy duplicate heading, metrics template link 검사 | 실제 `codex/issue-29-harness-baseline` branch는 허용되고 `main`은 종료 코드 1로 거부됐습니다. Legacy 경계, metrics 형식, 동결 예외와 사람 승인 경계를 정적 대조했습니다. Repository gate와 CI는 pending입니다. |
| 2026-07-11 | Issue #29 Review FAIL remediation | Level 0 | PASS | 문서·정적·하네스·도구 | Review 재검토, `python -m unittest scripts.tests.test_harness_gate`, `python scripts/harness_gate.py --issue 29 --base-ref origin/main --check-links --include-worktree`, branch guard 허용·거부, `git diff --check`, `origin/main...HEAD` 범위 검사 | 최종 Review가 수정 3건을 재검토해 PASS했고 테스트는 실행하지 않았습니다. QA는 HEAD `f3b8e03`에서 harness unit 48건과 repository·branch·diff 검사를 PASS했으며 결함은 0건입니다. 애플리케이션, build, 인프라 경로는 변경되지 않았고 CI는 pending입니다. |
| 2026-07-11 | Issue #29 external Review 보완 | Level 0 | PASS | 문서·정적·하네스·링크 | `python -m unittest scripts.tests.test_harness_gate`, `python scripts/harness_gate.py --issue 29 --base-ref origin/main --check-links --include-worktree`, Issue #29 stale PR 본문 snapshot 참조 검색, `git diff --check` | Dev가 harness unit 48건, snapshot 삭제 후 repository link gate, stale 직접 참조 0건과 diff 정적 검사를 종료 코드 0으로 확인했습니다. 애플리케이션, build, 인프라 경로는 변경하지 않았고 독립 재검토와 CI는 pending입니다. |
