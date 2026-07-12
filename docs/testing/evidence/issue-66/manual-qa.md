# Issue #66 Manual QA

- Issue #66의 7개 테스트 시나리오와 정책 절을 대조했습니다.
- 고정 allowlist는 PR 본문, `metrics.md`, 현재 Issue의 `verification-log.md` 행과 evidence 사이 동일 사실 동기화로 제한됩니다.
- production·애플리케이션 test·build·workflow·정책 의미 변경은 metadata-only 자동 복구에서 제외됩니다.
- Agent 수는 STRICT 역할 Dev, Review, QA, Docs의 4명이며 Main Coordinator와 CI를 제외하고 동일 역할 재시도를 중복 계산하지 않습니다.
- 범위 이탈, 정본 충돌, 두 번째 복구 실패, 코드 P0/P1이 metadata-only 경로로 우회되지 않음을 확인했습니다.

Level 5와 Level 6은 애플리케이션 런타임 및 HTTP 계약을 변경하지 않으므로 필요하지 않습니다.

