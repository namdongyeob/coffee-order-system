# Issue Metrics

Issue: #61
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/61
Branch: codex/issue-61-local-runtime
Measured at: 2026-07-12

| 실행 모드 | Agent 수 | 작업 시간(분) | 재시도 수 | 정체 수 | Review 결함 수 | QA 결함 수 | 범위 밖 변경 파일 수 | 읽은 핵심 문서 수 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| STRICT | 2 | 미측정 | 1 | 1 | 2 | 0 | 0 | 4 |

## 측정 근거

- 최초 Generate의 정확한 시각을 이 Dev 실행에서 관찰하지 못해 작업 시간을 추정하지 않았습니다. 관찰 가능한 runtime API 시각은 2026-07-12T11:18:45+09:00이며, 마지막 Dev reverification 시각은 PR 생성 전 기록합니다.
- RedisInsight invalid tag correction은 구현 중 발견한 같은 Attempt의 명확한 configuration error이며 Review 반환 재시도가 아닙니다.
- 원래 Dev의 허용된 Review 반환 1회 뒤 두 번째 REVISE가 안전 정지를 만들었습니다. 이후 사람 승인으로 Docs Agent 1명이 Runbook/manual-QA artifact recovery만 수행했습니다.
- Review 결함 수 2는 첫 Review P1과 remediation head의 두 번째 P1이며, QA 결함은 아직 없습니다. 사용자 수동 시각 QA screenshot 3개는 수집됐고 fresh Review, QA, CI는 pending입니다.
- 이번 recovery의 시작·종료 시각은 최초 Generate부터 마지막 reverification까지의 동일 측정 구간을 재구성할 수 없어 작업 시간에 추정치를 기록하지 않았습니다.
- 핵심 문서 4개는 context router, orchestration policy, test strategy, evidence guide입니다.

## Evidence links

- Commands: `commands.md`.
- Attempts: `attempt-log.md`.
- Manual QA: `manual-qa.md`.
- User visual QA: `screenshots/`의 IntelliJ, Kafka UI, RedisInsight 3개 artifact로 수집했습니다.
- Review/QA/CI: fresh Review와 independent QA, 최신 CI 확인이 pending입니다.
