# Issue Metrics

Issue: #45
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/45
Branch: codex/issue-45-local-operations-handoff
Measured at: 2026-07-12

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 4 | 미측정 | 0 | 0 | 0 | 0 | 0 | 6 |

## 측정 근거

- 최초 Generate부터 마지막 Reverification까지의 정확한 종료 시각은 아직 없으므로 작업 시간을 추정하지 않고 `미측정`으로 기록합니다.
- Agent 수 4는 Dev, fresh Reviewer, independent QA, Docs Agent를 뜻합니다. Review APPROVED, QA PASS, CI SUCCESS은 `9b7b554` head에만 적용되며, 이 Docs synchronization 뒤 새 head 기준 fresh Review와 최신 CI는 pending입니다.
- 비교 기준선은 Issue #45 본문의 #7 약 30분, #9 15분, #40 초기 두 Attempt active duration 21분입니다. #7의 현재 metrics는 미측정이므로 약 30분은 Issue 본문 관찰값으로만 사용합니다.
- #9의 15분은 metrics의 네 Attempt active duration 합이고, #40의 21분은 첫 두 Attempt active duration 합입니다. 향후 Issue는 실제 `metrics.md` 값으로만 비용을 비교합니다.
- 재시도, 정체, 범위 밖 변경 파일은 이 Attempt에서 0입니다.
- 핵심 문서 6개는 AGENTS, Issue loop, context router, orchestration policy, test strategy, evidence guide입니다.

## Evidence links

- Commands: `commands.md`.
- Attempts: `attempt-log.md`.
- Manual QA: `manual-qa.md`.
- Review/QA: `9b7b554` head에서 APPROVED/PASS. `quality-gates`: SUCCESS, https://github.com/namdongyeob/coffee-order-system/actions/runs/29178822969
- Fresh Review/CI: 이 Docs synchronization의 새 head 기준 pending입니다.
