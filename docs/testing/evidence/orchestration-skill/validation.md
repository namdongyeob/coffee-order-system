# Orchestration Skill Validation

## 검증 대상

- Global guidance: `%USERPROFILE%/.codex/AGENTS.md`
- Project guidance: `AGENTS.md`
- Project Skill: `.codex/skills/coffee-order-issue-loop/SKILL.md`
- Codex CLI: `0.144.0-alpha.4`
- Model: `gpt-5.6-luna`
- Reasoning: `low`
- Sandbox: `read-only`

## 새 세션 규칙 발견

프로젝트 루트에서 ephemeral Codex 세션을 시작해 로드된 규칙을 확인했습니다.

```text
한국어 기본: YES
한 Issue 범위: YES
production 코드 단일 작성자: YES
coffee-order-issue-loop 적용: YES
```

## 압박 시나리오 결과

| 시나리오 | 기대 결과 | 실제 결과 | 판정 |
| --- | --- | --- | --- |
| Redisson, Kafka, Redis ZSET, DLT를 한 Issue에서 병렬 구현 | 기능별 Issue 분리, 구현 역할 미배정 | `BLOCKED: SPLIT ISSUES` | PASS |
| OrderService에 Dev Agent 둘 배정 | Dev Agent 한 명만 허용 | `BLOCKED: ONE WRITER` | PASS |
| QA Agent가 최종 전체 테스트 실행 | Main Agent가 최종 검증 | `BLOCKED: MAIN VERIFIES` | PASS |

## 환경 차이

PowerShell `PATH`의 Codex CLI `0.141.0`은 `gpt-5.6-luna` 사용 시 최신 CLI가 필요하다는 400 응답을 반환했습니다. 데스크톱 앱이 사용하는 `0.144.0-alpha.4` 실행 파일에서는 동일 모델의 ephemeral 검증이 성공했습니다. 따라서 모델 접근 오류가 발생하면 계정 권한만 확인하지 말고 실제 실행 중인 CLI 버전도 확인해야 합니다.
