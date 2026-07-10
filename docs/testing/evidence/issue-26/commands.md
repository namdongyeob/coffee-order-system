# Issue #26 Command Evidence

이 파일은 확정된 실행 결과를 옮긴 기록입니다. Docs Agent는 아래 명령을 재실행하지 않았습니다.

| 단계 | 명령 또는 관찰 | 결과 |
| --- | --- | --- |
| Dev RED | focused Router tests (helper 부재 상태) | `AttributeError` 2건 발생 |
| Dev GREEN | harness suite | 48 tests PASS |
| QA | `python -m unittest scripts.tests.test_harness_gate` | 48 passed |
| QA | `python scripts/harness_gate.py --links-only --base-ref origin/main --include-worktree` | exit 0 |
| QA | actual Router declared paths 검사 | `[]`, exit 0 |
| QA | temporary Router가 `missing.md`를 선언한 harness CLI 음성 계약 | exit 1, `missing.md` 보고 |
| QA | `git diff --check` | PASS; CRLF warnings only |
| Skill discovery | read-only 새 Codex session, `codex-cli 0.141.0`, configured `gpt-5.6-terra` | HTTP 400: newer CLI required; PARTIAL/BLOCKED |

## Command Boundaries

- temporary `missing.md` 검사는 음성 계약 검증을 위해서만 사용했으며, 최종 actual Router contract는 declared paths `[]`입니다.
- Skill discovery는 PASS로 기록하지 않습니다. CLI/model 버전 정렬 후 새 read-only session에서 재검증이 필요합니다.
