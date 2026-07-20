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

- [x] Java/Gradle 작업의 non-ASCII worktree가 `BLOCKED: NON_ASCII_WORKTREE_PATH`로 분류됩니다.
- [x] Python-only 작업의 non-ASCII 경로는 차단하지 않습니다.
- [x] 동일 실패의 첫 retry는 `RETRY_ONCE`, 다음 실패는 `BLOCKED: RETRY_LIMIT`으로 분류됩니다.
- [x] 사용자 승인 new run은 기존 retry count를 조용히 초기화하지 않고 `START_APPROVED_NEW_RUN`으로 구분됩니다.
- [x] Agent assignment가 `phase`, `status`, `started_at`, `last_heartbeat_at`, `deadline_at`을 보존합니다.
- [x] heartbeat timeout은 `STALLED`, deadline 초과는 `TIMEOUT`으로 상태에 저장됩니다.
- [x] `TIMEOUT`·`BLOCKED` assignment는 heartbeat로 다시 `RUNNING`이 되지 않습니다.
- [x] Issue #141 Python fixture가 changed-path allowlist에 포함되어 `requires_java_ci=false`로 분류됩니다.
- [x] 정책 문서와 Issue loop Skill이 새 runtime guard token과 상시 polling 제외 범위를 설명합니다.
- [x] 전체 Python harness suite와 `git diff --check`가 PASS입니다.

## 제외 확인

- [x] 상시 process polling, 외부 Agent 강제 종료, Docker/Kafka/Gradle 자동 실행, 새 CI job을 추가하지 않았습니다.
- [x] 애플리케이션 production/test, build, runtime 설정과 Issue #134를 변경하지 않았습니다.
