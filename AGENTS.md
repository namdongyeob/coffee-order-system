# 커피 주문 시스템 에이전트 진입점

1. 현재 Issue, 작업 브랜치, 미커밋 변경을 확인합니다.
2. 구현·Review·QA 작업이면 `.codex/skills/coffee-order-issue-loop/SKILL.md`를 적용합니다.
3. [Context Router](docs/ai/context-router.md)에서 역할·도메인에 맞는 hot path를 선택하고, 해당 경로의 직접 관련 정본 문서 1~5개만 읽습니다.
4. 규칙 충돌이나 추가 탐색이 필요할 때만 [규칙 정본 지도](docs/ai/rule-source-map.md)를 따라 한 단계 확장합니다.

`docs/` 전체를 기본 입력으로 재귀 탐색하지 않습니다. 실행 모드·역할·검증·evidence의 정본은 Router가 가리키는 문서만 읽고, 완료 주장은 실제 명령과 검증 Level 근거가 있을 때만 합니다.
