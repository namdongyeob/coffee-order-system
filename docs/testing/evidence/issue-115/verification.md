# 검증 로그

Attempt: 3
Head: 9b0776208dcbc2aeb1e0fe9bc860699f51d031e2

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-18 | Issue #115 final docs sync | Level 0 | PASS | scope, Flyway V1~V7 대조, 내부·외부 링크, 공개 저장소·커밋, evidence | `commands.md`, `manual-qa.md` | production/test 변경 0 |
| 2026-07-18 | Issue #115 final docs sync | Level 0 | PASS | links-only repository gate | `python scripts/harness_gate.py --links-only --base-ref origin/main --include-worktree` | PASS |
| 2026-07-18 | Issue #115 final docs sync | Level 0 | PASS | 문서 하네스 unit | `python -m unittest scripts.tests.test_harness_gate` | 130/130 |
| 2026-07-18 | Issue #115 final docs sync Attempt 2 | Level 0 | PASS | Flyway V6 rebuild event type check와 Kafka replay→DB 집계 검증 설명 | `commands.md`, `manual-qa.md` | Review P1 2건 수정 |
| 2026-07-18 | Issue #115 troubleshooting correction Attempt 3 | Level 0 | PASS | README의 TIL 5개를 단일 통합 트러블슈팅 링크로 정정 | `commands.md`, `manual-qa.md` | URL HTTP 200, links-only·문서 하네스 134/134 PASS |

Level 5 required: NO, Level 6 required: NO. 문서 전용 변경이므로 최신 main에서 이미 PASS한 runtime 전체 회귀를 반복하지 않았습니다. 독립 Combined Verifier와 최신 evidence-only PR-head CI는 후속 gate입니다.
