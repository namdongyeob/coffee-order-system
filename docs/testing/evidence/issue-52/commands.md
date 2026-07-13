# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python -m pytest scripts/tests/test_harness_gate.py -q` | 회귀 소유권 복구·섹션 분리·runbook 재지정·dedup 각 단계 focused 계약 검증 | 각 단계와 최종 head `30ecf80`에서 104건 PASS(110 subtests). |
| `wc -c docs/ai/orchestration-policy.md` | 핵심 실행 계약 크기 확인 | `30ecf80`에서 24,284 → 15,226바이트. |
| `grep -nE "Sol \|Terra \|Luna " docs/ai/orchestration-policy.md` | 핵심 계약 특정 모델 제품명 검사 | 0건. `SOLO`/`Solo Agent`의 부분일치만 있어 실제 모델명 아님을 확인했습니다. |
| `python scripts/harness_gate.py --issue 52 --branch claude/issue-52-harness-slim-3 --base-ref b70e3e4 --check-links --include-worktree` | Issue evidence 형식·정합성·선언 Markdown 링크·변경 범위 검사 | PASS. |
| `git diff --check` | 공백 오류 검사 | PASS. |
| fresh Review at `c48ec1d` | Dev와 분리된 fresh context의 1차 독립 검토 | `REVISE`. P1(flaky 절차 참조 오지정)과 P2 2건(스코프·agent-rules stale 포인터)을 반환했습니다. 안전 불변조건 약화(P0)는 없음을 확인했습니다. 수행 시각은 `미측정`입니다. |
| Review 반환 P1·P2 정정 후 `python -m pytest scripts/tests/test_harness_gate.py -q` | REVISE 정정(head `30ecf80`) 뒤 재검증 | 104건 PASS. |
| fresh Review at `30ecf80` | 정정 뒤 fresh context 최종 독립 검토 | `APPROVED`. 안전 불변조건 유지, 원문 byte-identical 이관, 참조 정합성, 범위(docs+계약 테스트) 확인. 수행 시각은 `미측정`입니다. |
| independent QA at `30ecf80` | Dev·Review와 분리된 독립 검증 | `PASS`. 변경 범위 docs+계약 테스트, 새 gate·receipt·Level 자동화 0건, 계약 테스트 104 PASS, 전체 gate PASS, 크기 상한 미만, 모델명 0건을 독립 확인했습니다. 수행 시각은 `미측정`입니다. |
