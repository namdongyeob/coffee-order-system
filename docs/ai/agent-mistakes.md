# 에이전트 실수 기록

AI가 잘못 제안한 내용, 잘못된 이유, 수정 방식을 기록합니다.

| 날짜 | 실수 | 수정 |
| --- | --- | --- |
| 2026-07-10 | PR #31을 생성할 때 `Execution mode: STRICT`와 `Execution mode reason`을 본문에 넣지 않아 quality-gates가 실패했습니다. 재현 조건은 누락된 본문으로 생성된 기존 `pull_request` event를 rerun하는 경우입니다. `gh run view 29086275802 --repo namdongyeob/coffee-order-system --log-failed`로 해당 run의 두 필드 누락과 종료 코드 1을 확인했습니다. 이후 PR 본문만 수정하고 기존 run을 rerun했지만, rerun은 최초 PR event payload snapshot을 재사용해 같은 실패가 반복됐습니다. | PR 생성 전 harness가 요구하는 PR body 필수 필드를 사전 점검합니다. PR 본문을 수정한 뒤에는 기존 run을 rerun하지 않고, 현재 PR 본문을 반영하는 새 `pull_request` event를 생성한 후 quality-gates 결과를 확인합니다. |
| 2026-07-11 | Issue #29의 STRICT 흐름에서 필수 Review·QA 후속 역할이 남아 있고 Agent 완료 보고도 있었지만, Main이 live 또는 pending Agent가 있는 상태에서 final을 보내고 다음 역할을 즉시 배정하지 않아 부모 turn이 비활성화됐습니다. 그 결과 Codex Desktop의 active work timer가 멈추고 subagent 목록과 include-message 안내가 보이지 않아 사용자는 작업이 끝난 것처럼 보였으며 필수 Review·QA 후속 처리가 지연됐습니다. 이 순서에서 CPU나 모델 병목은 확인되지 않았고, 모델 전환은 새 active turn을 시작했을 뿐 원인을 해결했다는 근거가 없습니다. | Main은 final 전에 `list_agents`로 상태를 확인합니다. live Agent 또는 필수 다음 역할이 남아 있으면 commentary 상태를 유지하며 기다리거나 즉시 배정하고, 완료 mailbox를 받으면 곧바로 다음 역할을 시작합니다. 작업 중 상태 질문은 commentary로 답하고, final은 PR-ready, `BLOCKED`, 사용자 입력 필수 중 하나의 terminal 상태에서만 보냅니다. |

## 기록 조건

반복 가능한 실제 하네스 실패만 이 표에 기록합니다. 각 항목에는 실제 Issue 또는 PR 번호, 재현 조건 또는 명령, 관찰된 영향, 재발 방지 조치를 포함합니다. 추측한 실패, 한 번의 일시적 환경 오류, 아직 재현하지 못한 우려는 Attempt log에만 남기고 이 표에 중복 등록하지 않습니다.
