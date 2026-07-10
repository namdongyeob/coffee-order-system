# Issue #25 Commands

## Execution Status

- PENDING: Issue #25 구현 검증 명령은 아직 실행하지 않았습니다. 이 파일은 evidence 초기화 시점의 계획만 기록합니다.

## Planned Commands

| Command | Purpose | Result |
| --- | --- | --- |
| `python -m unittest discover -s scripts/tests -p "test_*.py"` | 형식 및 교차 검사 단위 테스트 실행 | PENDING: not executed |
| `python scripts/harness_gate.py --issue 25 --base-ref origin/main --check-links` | Issue #25 harness gate와 링크 검사 실행 | PENDING: not executed |
| `git diff --check` | 공백 오류 검사 | PENDING: not executed as Issue #25 implementation evidence |

## Result Handling

- PENDING: 실제 실행 시 명령, 종료 코드, 결과 요약, 실패 원인을 기록합니다.
