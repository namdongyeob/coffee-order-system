# 커피 주문 시스템 에이전트 진입점

## 시작 순서

1. 현재 GitHub Issue와 작업 브랜치, 미커밋 변경을 확인합니다.
2. `docs/ai/rule-source-map.md`에서 이번 작업의 정본 문서를 찾습니다.
3. Issue에 직접 연결된 문서만 읽고 목표, 제외 범위, 검증 계획을 고정합니다.
4. 구현·Review·QA 작업에는 `.codex/skills/coffee-order-issue-loop/SKILL.md`를 적용합니다.

## 반드시 지킬 경계

- 각 Dev Agent와 worktree는 한 번에 Issue 하나만 처리합니다. Main Coordinator는 쓰기 범위와 도메인 계약이 겹치지 않는 독립 Issue 여러 개를 병렬 조정할 수 있습니다.
- 정책이 불명확하면 구현하지 않고 질문 Issue 또는 ADR 초안으로 분리합니다.
- Main Coordinator는 저장소 파일을 수정하거나 코드리뷰·테스트를 수행하지 않습니다. Dev가 구현하고 Review가 검토하며 QA가 독립 검증합니다.
- 범위 밖 리팩터링과 Facade, Generic Manager, 공통 프레임워크성 추상화를 추가하지 않습니다.
- merge와 Issue close는 사람이 승인한 뒤 수행합니다.

## 정본 라우팅

- 요구사항과 범위: `docs/product/requirements.md`, `docs/product/scope.md`.
- 도메인 정책: `docs/domain/`.
- API 계약: `docs/api/api-spec.md`.
- 아키텍처와 코드 위치: `docs/architecture/`, `docs/adr/`.
- 역할, 권한, 오케스트레이션: `docs/ai/orchestration-policy.md`.
- 검증 Level과 실행 기준: `docs/testing/test-strategy.md`.
- evidence와 Attempt 기록 형식: `docs/testing/evidence-guide.md`.
- 완료 전 체크리스트: `docs/ai/issue-completion-checklist.md`.
- 실제 검증 결과: `docs/testing/verification-log.md`, `docs/testing/evidence/issue-{number}/`.

완료 주장은 실제 명령, 통과한 Level, 미검증 Level과 이유를 evidence에 남긴 뒤에만 합니다.
