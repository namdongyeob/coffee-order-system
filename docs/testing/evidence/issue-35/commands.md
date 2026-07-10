# 실행 명령과 결과

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| focused Issue #35 계약 테스트 | 정책과 Router 계약의 변경 전 실패 확인 | RED. 정책 문구 부재 1건과 Router 섹션 부재 1건, 총 2개의 누락 계약을 확인했습니다. |
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
| QA 최초 focused 계약 테스트 명령 | 독립 QA focused 검증 | 명령 오류. 존재하지 않는 클래스명을 지정해 unittest loader error가 발생했으며 저장소 결함으로 판정하지 않았습니다. |
| `python -m unittest scripts.tests.test_harness_gate.OrchestrationContractTest` | 실제 계약 테스트 클래스로 QA focused 재검증 | PASS. 2건이 종료 코드 0으로 통과했습니다. |
| 독립 Review diff 검토 | Issue 요구사항, 변경 범위와 정책 계약 검토 | PASS. 수정 필요 결함 0건입니다. |
| 독립 QA 최종 판정 | focused 계약과 Dev 검증 evidence 대조 | PASS. QA 결함 0건입니다. |

독립 Review와 QA는 PASS했습니다. Level 5와 Level 6은 애플리케이션·DB·외부 인프라를 변경하지 않아 수행 대상이 아닙니다. GitHub Actions CI는 pending이며 로컬 검증과 구분합니다.
