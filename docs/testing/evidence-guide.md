# Evidence Guide

LazyCodex 원본의 evidence-bound workflow를 이 프로젝트에 맞게 적용합니다. 완료 주장은 테스트 통과 문장만으로 하지 않고, 작업 종류에 맞는 관찰 가능한 evidence를 함께 남깁니다.

## 저장 위치

Issue별 evidence는 다음 위치에 둡니다.

```text
docs/testing/evidence/issue-{number}/
```

`{number}`는 실제 열린 GitHub Issue 번호입니다. 작업 브랜치의 `issue-{number}`, `attempt-log.md`의 `Issue: #{number}`, Issue URL, evidence 디렉터리 번호는 모두 같은 번호를 사용합니다.

하네스 의무화 commit `e5b47bd` 이전에 merge된 Issue는 Legacy로 인정합니다. Legacy Issue를 다시 열거나 코드·문서·workflow를 변경하는 새 PR을 만들면 현재 evidence 기본 파일과 `metrics.md`를 새 Issue 번호로 작성합니다. 당시 관찰하지 않은 명령이나 결과를 Legacy Issue에 소급해 만들지 않습니다.

## 기본 파일

| 파일 | 내용 |
| --- | --- |
| `acceptance-criteria.md` | Issue 완료 조건, Execution mode, Level 5/6 필요 여부·이유. |
| `attempt-log.md` | Generate, Evaluate, 실패 원인, 수정 범위, 재검증, 다음 Attempt 입력. |
| `commands.md` | 실행한 명령, 목적, 결과 요약. |
| `manual-qa.md` | 사람이 확인한 API 응답, DB query, CLI output, 스크린샷 위치. |
| `metrics.md` | Issue별 실행·품질 지표의 고정 형식 기록. |
| `test-output.txt` | 필요한 경우 테스트 출력 원문 또는 핵심 발췌. |

## 작업별 Evidence

| 작업 | Evidence 후보 |
| --- | --- |
| DB migration | Flyway migration 파일, JPA 통합 테스트 결과, DB table/seed 조회 결과. |
| API | `.http` 파일, curl output, Postman collection, 응답 JSON. |
| Kafka/Redis | topic/key 확인 명령, Testcontainers 통합 테스트 결과, DLT 확인 결과. |
| k6 | script, summary output, 관찰 결과. |
| UI/TUI | screenshot, terminal capture, visual QA 메모. |

## Attempt 연결 규칙

`docs/testing/evidence/attempt-log-template.md`를 복사해 Issue별 디렉터리에 둡니다. Evaluate가 FAIL이면 실패 원인과 허용된 수정 범위를 적고, 다음 Agent에는 마지막 `Next Attempt` 절만 전달합니다. PASS인 경우에도 재검증 명령과 결과를 남기고 `Next Attempt`를 `없음`으로 닫습니다.

`metrics.md`는 [Issue metrics template](evidence/issue-metrics-template.md)를 복사해 작성합니다. 이 파일의 표 제목·열 순서·값 형식은 바꾸지 않으며, 측정 근거는 `commands.md`, `attempt-log.md`, Review/QA 보고 또는 PR 링크로 연결합니다.

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

- Automated verification.
- Manual QA.
- Adversarial QA.
- Cleanup receipt.
- Evidence files.
- 읽은 문서, subagent 사용 여부와 이유, Execution mode와 reason, Level 5/6 결정과 이유.
- 실행한 검증별 Level, 명령 또는 확인, 결과와 미검증 항목·남은 위험.
- Codex를 사용했다면 CLI version, model, reasoning effort, 실제 관찰된 sandbox/approval.

완료 주장 템플릿을 별도 파일로 두지 않습니다. PR template과 이 절의 항목, Issue evidence가 완료 주장에 필요한 정본입니다.

## 주의

- 비밀값, access token, 개인 정보가 들어간 output은 저장하지 않습니다.
- 스크린샷은 UI/TUI처럼 시각 검증이 필요한 경우에 우선 사용합니다.
- 백엔드 작업에서는 테스트 로그, API 응답, DB query 결과가 더 적절한 evidence일 수 있습니다.
