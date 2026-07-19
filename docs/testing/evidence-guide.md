# Evidence Guide

LazyCodex 원본의 evidence-bound workflow를 이 프로젝트에 맞게 적용합니다. 완료 주장은 테스트 통과 문장만으로 하지 않고, 작업 종류에 맞는 관찰 가능한 evidence를 함께 남깁니다.

## 저장 위치

Issue별 evidence는 다음 위치에 둡니다.

```text
docs/testing/evidence/issue-{number}/
```

`{number}`는 실제 열린 GitHub Issue 번호입니다. 작업 브랜치의 `issue-{number}`, `attempt-log.md`의 `Issue: #{number}`, Issue URL, evidence 디렉터리 번호는 모두 같은 번호를 사용합니다.

하네스 의무화 commit `e5b47bd` 이전에 merge된 Issue는 Legacy로 인정합니다. Legacy Issue를 다시 열거나 새 PR을 만들면 새 Issue 번호의 현재 evidence 규칙을 적용하고 과거 관찰을 소급 생성하지 않습니다.

## 기본 파일과 #137 bootstrap

| 파일 | 내용 |
| --- | --- |
| `acceptance-criteria.md` | Issue 완료 조건, Execution mode, Level 5/6 필요 여부·이유. |
| `attempt-log.md` | 실제 FAIL, BLOCKED 또는 재시도가 있을 때 Generate, Evaluate, 실패 원인과 Next Attempt. |
| `commands.md` | `verification.md`로 설명하기 어려운 별도 상세 명령 관찰이 있을 때만 작성. |
| `manual-qa.md` | 별도 API·DB·CLI·시각 관찰이 있을 때만 작성. |
| `metrics.md` | 완료 gate가 아니며 성능 pilot·회고의 on-demand 집계에서만 선택 작성. |
| `verification.md` | 이 Issue의 최종 repository 검증 결과 행 정본. |
| `test-output.txt` | 필요한 경우 테스트 출력 원문 또는 핵심 발췌. |

기본 완료 정본은 `acceptance-criteria.md`와 `verification.md`입니다. 상세 파일이 없으면 `verification.md`에 명령, 결과, 미검증 Level과 남은 위험을 기록합니다. #137 자체는 전환 전 계약을 따라 `acceptance-criteria.md`, `attempt-log.md`, `commands.md`, `manual-qa.md`, `metrics.md`, `verification.md` 6종과 기존 preflight를 모두 유지하며 새 기본값은 #137 merge 뒤 Issue부터 적용합니다.

## Issue별 verification 정본과 전역 뷰

검증 결과 행은 `docs/testing/evidence/issue-{number}/verification.md`에만 기록합니다. 기존 전역 `docs/testing/verification-log.md`는 커밋하지 않는 생성 뷰이며, 일반 Issue PR은 이를 만들거나 수정하지 않습니다. Issue에 속하지 않는 과거 행은 `docs/testing/evidence/legacy/verification.md`에 원문 그대로 보관합니다.

전역 뷰가 필요하면 저장소 루트에서 다음 명령으로 재현합니다.

```powershell
python scripts/rebuild_verification_log.py
```

파일로 확인해야 하면 저장소 밖의 임시 경로를 지정합니다.

```powershell
python scripts/rebuild_verification_log.py --output $env:TEMP\verification-log.md
```

초기 이관은 `python scripts/migrate_verification_log.py --delete-source`로 수행했으며, 각 행의 날짜·Issue·Level·결과·검증 범위·명령/Evidence·비고 원문을 변경하지 않습니다.

## 작업별 Evidence

| 작업 | Evidence 후보 |
| --- | --- |
| DB migration | Flyway migration 파일, JPA 통합 테스트 결과, DB table/seed 조회 결과. |
| API | `.http` 파일, curl output, Postman collection, 응답 JSON. |
| Kafka/Redis | topic/key 확인 명령, Testcontainers 통합 테스트 결과, DLT 확인 결과. |
| k6 | script, summary output, 관찰 결과. |
| UI/TUI | screenshot, terminal capture, visual QA 메모. |

## Attempt 연결 규칙

FAIL, BLOCKED 또는 재시도가 실제로 생기면 `docs/testing/evidence/attempt-log-template.md`를 복사합니다. `Current disposition`, `Current Attempt`, `Current head`와 `verification.md`의 Attempt·Head를 일치시키고, 선택 metrics가 있으면 재시도 수도 일치시킵니다. FAIL은 정확한 원인·허용 수정 범위·마지막 `Next Attempt`만 다음 역할에 전달합니다. 실패가 없는 Issue는 attempt-log를 만들지 않고 verification PASS와 모두 체크된 Acceptance Criteria로 완료를 증명합니다.

metrics는 완료 gate가 아니며 pilot을 명시적으로 수행할 때만 [Issue metrics template](evidence/issue-metrics-template.md)을 사용합니다. Agent 수, 읽은 문서 수, `미측정` 시간과 대부분 0인 수동 카운터를 완료 근거로 사용하지 않습니다.

Execution mode와 Level 5/6 결정은 다음 고정 형식을 사용합니다. `scripts/harness_gate.py`가 이 필드를 검사합니다.

```text
Execution mode: SOLO, STANDARD, STRICT 중 하나
Execution mode reason: 비어 있지 않은 선택 근거
Level 5 required: YES 또는 NO
Level 5 reason: 비어 있지 않은 이유
Level 6 required: YES 또는 NO
Level 6 reason: 비어 있지 않은 이유
```

## PR 작성 기준

PR 본문에는 `Related: #번호`로 Issue를 연결하고 다음을 남깁니다. PR이 Issue를 자동 종료하면 안 되므로 `Closes`는 사용하지 않습니다.

### Minimum PR body and GitHub metadata

GitHub UI already provides CI run IDs, commit SHAs, and diff statistics. Do not restate those values in PR body prose. Keep the minimum body focused on observed results, decisions, and remaining risks. Link detailed commands, raw output, and role verdicts from Issue evidence or GitHub comments.

### PR body preflight and publishing

Before creating or editing a PR body, create a temporary Markdown file outside the repository and run `python scripts/harness_gate.py --issue <number> --pr-body-file <temporary file>`. The preflight fails closed when Acceptance Criteria, verification PASS, declared mode and any optional Attempt·metrics evidence disagree. Use that same passing file with `gh pr create --body-file <temporary file>` or `gh pr edit <PR number> --body-file <temporary file>`. Do not use an inline shell body. These rules apply to PRs created or edited after Issue #55.

고정 자율 큐의 PR 본문은 한국어로 작성하고 저장소 밖 UTF-8 no-BOM 임시 파일을 preflight한 뒤 같은 파일만 `--body-file`로 게시합니다. 아직 실행되지 않은 Review·QA 링크나 가변 head·CI·Gate 상태, Agent·retry 수, diff 통계, 파일 목록과 테스트 수를 본문에 복제하지 않습니다.

Manual QA, Adversarial QA와 cleanup receipt는 변경 위험상 실제 관찰이 있을 때 linked evidence 또는 `verification.md`에 둡니다. PR 본문은 material decision과 남은 위험·gate 상태를 적고 GitHub 소유 metadata를 복제하지 않습니다.

- Automated verification.
- Manual QA.
- Adversarial QA.
- Cleanup receipt.
- Evidence files.
- 읽은 문서, subagent 사용 여부와 이유, Execution mode와 reason, Level 5/6 결정과 이유.
- 실행한 검증별 Level, 명령 또는 확인, 결과와 미검증 항목·남은 위험.
- Codex를 사용했다면 CLI version, model, reasoning effort, 실제 관찰된 sandbox/approval.

Level 3~7 행은 검증한 source-tree SHA를 기록합니다. 같은 source-tree에서 evidence·PR metadata·raw artifact만 추가한 commit은 runtime evidence를 무효화하지 않으며 source/test/build/runtime 또는 실제 계약 의미 변경은 단일 영향도 분류기 판정에 따라 stale 처리합니다.

`STANDARD`는 독립 검증 전 draft PR을 만들 수 있지만 draft 생성만으로 완료를 주장하지 않습니다. Combined Verifier가 외부 독립 리뷰인지 내부 독립 역할인지, PASS 또는 FAIL 판정, 실행한 focused verification과 결과를 PR 본문 또는 연결 evidence에 기록합니다. Combined Verifier와 CI가 pending이면 ready 전환, 완료 또는 merge 권고도 pending으로 명시합니다.

완료 주장 템플릿을 별도 파일로 두지 않습니다. PR template과 이 절의 항목, Issue evidence가 완료 주장에 필요한 정본입니다.

## 주의

- 비밀값, access token, 개인 정보가 들어간 output은 저장하지 않습니다.
- 스크린샷은 UI/TUI처럼 시각 검증이 필요한 경우에 우선 사용합니다.
- 백엔드 작업에서는 테스트 로그, API 응답, DB query 결과가 더 적절한 evidence일 수 있습니다.
