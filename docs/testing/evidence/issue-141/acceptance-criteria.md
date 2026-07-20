# Issue #141 Acceptance Criteria

Issue: #141
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/141

Execution mode: STRICT
Execution mode reason: Coordinator 상태·retry·검증 gate를 보강하는 harness/workflow policy 변경이며, 외부 runtime을 실행하지 않는 focused 회귀로 검증합니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 runtime과 외부 인프라 연결을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP API 계약과 실제 요청 경로를 변경하지 않습니다.

## 완료 기준

- [x] Java/Gradle 작업의 resolved physical non-ASCII worktree와 없는 worktree가 각각 `BLOCKED: NON_ASCII_WORKTREE_PATH`·`BLOCKED: WORKTREE_NOT_FOUND`로 분류됩니다.
- [x] register는 실제 worktree, 영향도, deadline, `--java-required` 또는 `--no-java-required` 중 정확히 하나를 요구합니다.
- [x] CLI register → heartbeat → lifecycle STALLED → snapshot-once → scoped retry → release 흐름이 state.json에 저장됩니다.
- [x] `STALLED`·`TIMEOUT`·`BLOCKED`·`COMPLETED` assignment는 active/writer slot을 소비하거나 heartbeat로 `RUNNING`에 부활하지 않습니다.
- [x] STALLED/TIMEOUT 상태 전이와 stall metric은 반복 lifecycle 호출로 중복 저장·증가하지 않습니다.
- [x] retry는 Issue 번호와 stable failure key별 첫 `RETRY_ONCE`만 소비하고 다음 동일 실패는 `BLOCKED: RETRY_LIMIT`입니다.
- [x] terminal·STALLED assignment는 slot뿐 아니라 owned-path overlap reservation에서도 제외되어 동일 범위 재배정이 가능합니다.
- [x] one-shot `block` CLI가 명시적 `BLOCKED` terminal 상태를 state.json에 저장합니다.
- [x] retry ledger가 없는 legacy `retries > 0` state는 특정 failure로 추정하지 않고 `BLOCKED: LEGACY_RETRY_LEDGER_REQUIRED`로 차단합니다.
- [x] deadline·heartbeat·lifecycle 시간 입력은 NaN·±Inf를 `BLOCKED: INVALID_TIME`으로 차단합니다.
- [x] reset/new-run은 비어 있지 않은 approval reference와 reset 시각을 audit history로 state.json에 보존합니다.
- [x] release 뒤 같은 agent id를 재등록하면 snapshot-once budget이 새로 시작됩니다.
- [x] Agent assignment가 `phase`, `status`, `started_at`, `last_heartbeat_at`, `deadline_at`을 보존합니다.
- [x] Issue #141 Python fixture가 changed-path allowlist에 포함되어 `requires_java_ci=false`로 분류됩니다.
- [x] 정책 문서와 Issue loop Skill이 새 runtime guard token과 상시 polling 제외 범위를 설명합니다.
- [x] Coordinator PR metadata가 UTF-8 `body-file`과 비목록 canonical execution mode line을 사용하도록 규정합니다.
- [x] focused 61 tests, 전체 Python harness 219 tests, repository gate와 `git diff --check`가 PASS입니다.

## 제외 확인

- [x] 상시 process polling, 외부 Agent 강제 종료, Docker/Kafka/Gradle 자동 실행, 새 CI job을 추가하지 않았습니다.
- [x] 애플리케이션 production/test, build, runtime 설정과 Issue #134를 변경하지 않았습니다.
