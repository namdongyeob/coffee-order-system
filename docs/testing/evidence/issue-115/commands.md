# Issue #115 Commands

Issue: #115
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/115
Execution head: 96ae18340258a9bba09f591572806ce687f0347d

| Level | 명령 또는 확인 | 결과 |
| --- | --- | --- |
| Level 0 | `git fetch origin main`, `git status --short --branch`, `git rev-parse HEAD`, `git rev-parse origin/main` | PASS, clean base와 origin/main이 `b1e7732`로 일치 |
| Level 0 | Flyway V1~V7 SQL과 `docs/db/erd.md`의 table·column·constraint·index 수동 대조 | PASS, V1~V7과 최종 ERD 일치 |
| Level 0 | Controller·DTO·ErrorCode·OrderService·ranking/outbox/retention 직접 계약과 README·API·요구사항·범위 비교 | PASS, 과거 “별도 Issue/예정/Outbox 제외” 표현 제거 |
| Level 0 | `python scripts/harness_gate.py --links-only --base-ref origin/main --include-worktree` | PASS |
| Level 0 | `python -m unittest scripts.tests.test_harness_gate` | PASS, 130 tests in 0.999s |
| Level 0 | Notion TIL URL 5개 `Invoke-WebRequest -Method Head` | PASS, 모두 HTTP 200 |
| Level 0 | `gh repo view ... --json visibility,url`, non-merge commit count | PASS, PUBLIC, 235 commits로 제출 기준 10개 이상 |
| Level 0 | `git diff --check`, 변경 파일 allowlist | PASS, whitespace error 0, production/test 변경 0 |
| Level 0 | Review P1 ERD V6 check 제약·Redis rebuild 설명 수정 뒤 links-only | PASS |
| Level 0 | Review P1 수정 뒤 `python -m unittest scripts.tests.test_harness_gate` | PASS, 130 tests in 1.097s |

## 환경 메모

- Codex 실행 환경은 filesystem unrestricted, approval policy `never`였습니다.
- 문서 전용 변경이라 Level 1 전체 Gradle 회귀, Level 3~7 runtime 검증은 반복하지 않았습니다. 최신 main의 Level 5·6·7 근거는 Issue #114입니다.
- Attempt 2는 Review P1 두 건의 문서만 수정해 links-only와 문서 하네스만 재실행했습니다.
