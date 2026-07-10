# LazyCodex 적용 Runbook

## 현재 방식

이 저장소는 원본 LazyCodex의 `.omo` 상태를 사용하지 않습니다. GitHub Issue, PR, `attempt-log.md`, 검증 로그, evidence로 같은 피드백 원칙을 적용하는 하이브리드 방식입니다.

Issue 실행 순서는 `docs/ai/agent-rules.md`, 역할과 실행 모드는 `docs/ai/orchestration-policy.md`, 검증은 `docs/testing/test-strategy.md`, evidence는 `docs/testing/evidence-guide.md`를 따릅니다.

## 실제 LazyCodex를 사용할 조건

- `$ulw-plan`, `$start-work`, `$ulw-loop` 명령의 설치와 동작을 현재 환경에서 확인했습니다.
- 같은 실패가 반복되어 상태를 보존한 장기 루프가 필요합니다.
- GPT-5.6 Ultra 등 다른 오케스트레이터와 중첩하지 않습니다.
- Issue 범위와 사람 승인 지점이 먼저 결정됐습니다.

설치가 확인되지 않은 명령을 문서만 보고 실행하지 않습니다. 새 작업마다 `<repository-root>`의 실제 worktree 경로와 branch를 다시 확인합니다.

## 실패 재시도

실패한 Generate/Evaluate 결과는 해당 Issue의 `attempt-log.md`에 기록합니다. 다음 Dev Agent는 마지막 `Next Attempt`만 입력으로 받아 실패 원인 밖의 범위를 수정하지 않습니다. 자동 재시도 엔진은 현재 범위에 포함하지 않습니다.
