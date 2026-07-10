# GPT-5.6 Orchestration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans` when this plan is executed in another session.

**Goal:** 새 Codex 작업에서도 동일한 범위 통제, 역할 분리, 검증 증거 기준이 자동 적용되도록 전역 규칙과 프로젝트 실행 절차를 정리합니다.

**Architecture:** 개인 공통 습관은 전역 `AGENTS.md`, 커피 주문 시스템의 정책은 저장소 `AGENTS.md`, 반복 실행 절차는 프로젝트 Skill에 둡니다. GitHub Issue는 작업 범위의 정본이며, GPT-5.6 일반 모드와 Ultra, LazyCodex식 반복 루프는 작업 위험도에 따라 하나를 선택합니다.

**Tech Stack:** Codex `AGENTS.md`, Agent Skill, Markdown, GitHub Issue, Gradle 검증 기록.

## Global Constraints

- 기존 production/test 코드와 사용자의 미커밋 변경을 수정하지 않습니다.
- 한 Issue에서 production 코드 작성자는 한 명으로 제한합니다.
- Mock 테스트는 실제 서버, DB, Kafka, Redis, Postman 검증을 대체하지 않습니다.
- 공식적으로 확인되지 않은 자동 모델 라우팅이나 LazyCodex 명령을 가정하지 않습니다.

---

### Task 1: 전역과 프로젝트 규칙의 책임 분리

**Files:**

- Modify: `%USERPROFILE%/.codex/AGENTS.md`
- Modify: `AGENTS.md`
- Create: `docs/ai/orchestration-policy.md`

- [x] 전역 파일에는 저장소 확인, 범위 통제, 검증 수준 표시 같은 개인 공통 규칙만 작성합니다.
- [x] 프로젝트 파일에는 오케스트레이션 정책과 프로젝트 Skill 링크만 추가합니다.
- [x] 일반 모드, Ultra, LazyCodex식 반복 루프의 선택 기준과 금지되는 중첩 실행을 문서화합니다.

### Task 2: 재사용 가능한 Issue 품질 루프 Skill

**Files:**

- Create: `.codex/skills/coffee-order-issue-loop/SKILL.md`
- Create: `.codex/skills/coffee-order-issue-loop/agents/openai.yaml`

- [x] Skill 없이 범위 확장, Mock-only 완료 주장, 복수 작성자 충돌 시나리오를 실행합니다.
- [x] 실패 양상을 막는 최소 실행 계약을 Skill에 작성합니다.
- [x] 같은 시나리오를 Skill과 함께 다시 실행해 행동 차이를 확인합니다.

### Task 3: 오래된 실행 정보 정리와 검증

**Files:**

- Modify: `docs/ai/lazycodex-runbook.md`

- [x] 현재 프로젝트 루트를 실행 시점에 확인하도록 수정합니다.
- [x] 전역·프로젝트 `AGENTS.md` 탐색 결과를 확인합니다.
- [x] Skill frontmatter와 `agents/openai.yaml` 형식을 검증합니다.
- [x] `git diff --check`와 범위 검사를 실행하고 기존 Controller 변경이 보존됐는지 확인합니다.
