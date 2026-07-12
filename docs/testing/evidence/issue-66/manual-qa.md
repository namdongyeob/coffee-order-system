# Issue #66 Manual QA

- Issue #66의 7개 테스트 시나리오와 정책 절을 대조했습니다.
- 고정 allowlist는 PR 본문, `metrics.md`, 현재 Issue의 `verification-log.md` 행과 evidence 사이 동일 사실 동기화로 제한됩니다.
- production·애플리케이션 test·build·workflow·정책 의미 변경은 metadata-only 자동 복구에서 제외됩니다.
- Agent 수는 STRICT 역할 Dev, Review, QA, Docs의 4명이며 Main Coordinator와 CI를 제외하고 동일 역할 재시도를 중복 계산하지 않습니다.
- 범위 이탈, 정본 충돌, 두 번째 복구 실패, 코드 P0/P1이 metadata-only 경로로 우회되지 않음을 확인했습니다.

Level 5와 Level 6은 애플리케이션 런타임 및 HTTP 계약을 변경하지 않으므로 필요하지 않습니다.

## Pre-review completeness observation

- official Reviewer 배정 전에 Execution mode·Level 5/6, 한국어 PR 본문, HEAD, 테스트 수, evidence 존재, 정확한 9열 metrics, STRICT 역할 수와 실제 PR comment 링크, verification log와 명령 원문을 대조하도록 정책을 확인했습니다.
- 불일치는 고정 allowlist와 확정된 정본 안에서만 metadata recovery를 사용하며 코드 Review remediation budget과 분리됩니다.
- P2 finding의 등급 변경은 Issue #66에 포함하지 않으며 후속 Issue 후보로만 기록합니다.
- 필수 evidence인 `acceptance-criteria.md`, `attempt-log.md`, `commands.md`, `manual-qa.md`, `metrics.md`가 모두 존재합니다.
- full harness 정본은 실제 실행한 79 tests PASS이며 PR 본문, commands와 verification log의 현재 Issue 행이 같은 수를 사용합니다.
- remediation 이전 Review 역할 보고는 https://github.com/namdongyeob/coffee-order-system/pull/67#issuecomment-4950147743, QA 역할 보고는 https://github.com/namdongyeob/coffee-order-system/pull/67#issuecomment-4950147800이며 둘 다 현재 PR #67의 실제 conversation comment입니다.
- 위 두 링크는 이전 head의 역사적 감사 기록입니다. metadata recovery 뒤 official fresh Review·QA 판정으로 재사용하지 않습니다.
