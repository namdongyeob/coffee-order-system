# 실행 명령과 결과

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| focused Issue #35 계약 테스트 | 정책과 Router 계약의 변경 전 실패 확인 | RED. 정책 문구 부재 1건과 Router 섹션 부재 1건을 확인했습니다. |
| focused Issue #35 계약 테스트 | 정책과 Router 계약의 변경 후 확인 | PASS. 2건이 종료 코드 0으로 통과했습니다. |
| `python -m unittest scripts.tests.test_harness_gate` | 전체 저장소 하네스 회귀 | PASS. 50건이 종료 코드 0으로 통과했습니다. |
| `python scripts/harness_gate.py --issue 35 --base-ref origin/main --check-links --include-worktree` | Issue evidence와 변경 Markdown 링크 검사 | 최초 실행은 `verification-log.md`의 #35 행 누락을 정확히 거부했습니다. 행 추가 후 최종 재실행합니다. |
| `python scripts/harness_gate.py --branch codex/issue-35-verifier-routing --check-branch` | Issue branch guard 허용 경로 | PASS. 종료 코드 0입니다. |
| `python scripts/harness_gate.py --branch main --check-branch` | 보호 branch guard 거부 경로 | PASS. 의도한 종료 코드 1과 protected branch 오류를 확인했습니다. |
| policy heading 중복과 Gate 문구 `rg` 검사 | 정책 중복 제목과 새 완료 경계 확인 | PASS. 중복 제목이 없고 draft·독립 검증·CI 경계가 정본에 존재합니다. |
| Router PowerShell 링크 수 검사 | 필수 링크 4개와 조건 규칙 확인 | 명령 자체 FAIL. 한글 섹션 문자열 파싱이 깨져 대상 섹션을 찾지 못했으며 문서 결함으로 판정하지 않았습니다. Python UTF-8 검사로 교체합니다. |
| `git diff --check`와 변경 파일 목록 | 공백 오류와 허용 범위 확인 | PASS. diff 오류가 없고 앱·build·infra 변경이 없습니다. LF→CRLF 작업 트리 경고만 관찰했습니다. |
| Python UTF-8 Router 계약 검사 | 필수 링크 수와 조건부·제외·추가 탐색 규칙 확인 | PASS. `router_contract=PASS required_links=4`를 확인했습니다. |
| `python scripts/harness_gate.py --issue 35 --base-ref origin/main --check-links --include-worktree` | verification log 반영 후 최종 repository gate | PASS. `Harness gate PASSED`와 종료 코드 0을 확인했습니다. |
| `codex --version` | Dev 실행 환경 확인 | PASS. `codex-cli 0.141.0`입니다. |

독립 Review, QA, Docs Agent와 GitHub Actions CI 결과는 아직 pending이며 Dev 검증과 구분합니다.
