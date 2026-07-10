# 에이전트 실수 기록

AI가 잘못 제안한 내용, 잘못된 이유, 수정 방식을 기록합니다.

| 날짜 | 실수 | 수정 |
| --- | --- | --- |
| 2026-07-10 | PR #31을 생성할 때 `Execution mode: STRICT`와 `Execution mode reason`을 본문에 넣지 않아 quality-gates가 실패했습니다. 이후 PR 본문만 수정하고 기존 GitHub Actions run을 rerun했지만, rerun은 최초 PR event payload snapshot을 재사용해 같은 실패가 반복됐습니다. | PR 생성 전 harness가 요구하는 PR body 필수 필드를 사전 점검합니다. PR 본문을 수정한 뒤에는 기존 run을 rerun하지 않고, 현재 PR 본문을 반영하는 새 `pull_request` event를 생성한 후 quality-gates 결과를 확인합니다. |
