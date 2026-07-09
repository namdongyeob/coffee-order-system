# LazyCodex 운영 절차

## 기본 원칙

LazyCodex는 한 번에 큰 기능을 통째로 구현하는 방식이 아니라, 작은 Issue를 반복 루프로 처리하는 방식으로 사용합니다. 이 프로젝트에서는 원본 LazyCodex 런타임의 `.omo` 상태 관리를 직접 쓰지 않고, GitHub Issue, PR 본문, 검증 로그, evidence 파일로 같은 운영 원칙을 적용합니다.

```text
Explore -> Plan -> Implement -> Verify -> Manually QA -> Review -> Document -> Next Issue
```

이 루프의 목적은 AI가 정책을 추측하지 않게 하고, 각 작업의 요구사항, 구현 범위, 검증 결과, 실제 evidence를 남기는 것입니다.

## Issue 루프

1. GitHub Issue를 만들거나 선택합니다.
2. Issue에 목표, 범위, 제외 범위, 완료 기준, 검증 레벨을 적습니다.
3. 격리된 worktree 또는 브랜치를 사용합니다.
4. Issue 본문과 관련 문서를 에이전트에게 제공합니다.
5. PR 본문에 읽은 문서 목록을 남깁니다.
6. Dev Agent에게 해당 Issue만 구현하게 합니다.
7. 자동 검증과 Manual QA evidence를 남깁니다.
8. Review Agent에게 diff 리뷰를 요청합니다.
9. QA Agent에게 검증 누락과 재현 절차를 확인하게 합니다.
10. 필요한 문서를 갱신합니다.
11. 사람이 merge와 close를 결정합니다.

## 서브에이전트 역할

| 역할 | 책임 | 금지 사항 |
| --- | --- | --- |
| Spec Agent | 요구사항, 정책 빈칸, Issue 분해를 정리합니다. | 구현 코드를 작성하지 않습니다. |
| Dev Agent | 하나의 Issue를 구현하고 테스트를 작성합니다. | 다른 Issue 범위를 건드리지 않습니다. |
| Review Agent | 요구사항 누락, 테스트 누락, 회귀 가능성, 과한 추상화를 검토합니다. | 명시 요청 없이 직접 수정하지 않습니다. |
| QA Agent | 검증 레벨, 재현 절차, 누락된 인프라 검증을 확인합니다. | 구현을 대신 고치지 않습니다. |
| Docs Agent | README, ADR, API 명세, runbook을 갱신합니다. | 코드 동작을 추측해서 문서화하지 않습니다. |

## 병렬 서브에이전트 사용 기준

서브에이전트는 작업 간 파일 충돌이 없고 결정 순서가 독립적일 때만 병렬로 사용합니다.

병렬 처리해도 되는 예시는 다음과 같습니다.

- Review Agent가 현재 diff를 검토하는 동안 Docs Agent가 변경된 API 문서 초안을 정리합니다.
- QA Agent가 검증 누락을 확인하는 동안 Spec Agent가 후속 Issue 후보를 정리합니다.
- 서로 다른 문서 파일을 대상으로 하는 docs 작업을 분리합니다.

병렬 처리하지 않는 예시는 다음과 같습니다.

- 같은 서비스 코드를 두 Dev Agent가 동시에 수정하는 경우.
- 도메인 정책이 확정되지 않았는데 구현과 문서를 동시에 진행하는 경우.
- DB 스키마 변경과 API 구현처럼 순서 의존성이 큰 경우.

## 루프별 산출물

| 단계 | 산출물 |
| --- | --- |
| Explore | 읽은 문서 목록, 영향 파일, 현재 코드 상태. |
| Plan | 구현 순서, 제외 범위, 검증 계획. |
| Implement | 코드, 테스트, 최소 문서 갱신. |
| Verify | 자동 테스트 결과, `docs/testing/verification-log.md` 기록. |
| Manually QA | API 응답, DB query, CLI output, screenshot 등 작업 종류에 맞는 evidence. |
| Review | 리뷰 코멘트, 회귀 위험, 후속 Issue 후보. |
| Document | README, API 명세, ADR, runbook 갱신. |

## Evidence 기준

완료 주장은 말이 아니라 관찰 가능한 evidence로 뒷받침합니다.

| 작업 종류 | Evidence 후보 |
| --- | --- |
| DB migration | migration 파일, JPA schema 테스트, DB query 결과. |
| API | `.http` 요청, curl output, Postman collection, 응답 JSON. |
| Kafka/Redis | CLI output, Testcontainers 통합 테스트 로그, topic/key 확인 결과. |
| 성능 | k6 script, 실행 결과, 관찰된 병목. |
| UI/TUI | screenshot 또는 terminal capture. |

Evidence 위치는 `docs/testing/evidence-guide.md`를 따릅니다.

## Parent ownership

서브에이전트의 self-report만으로 완료 처리하지 않습니다. 최종 완료 판단은 부모 세션이 직접 diff, 테스트, evidence를 확인한 뒤 내립니다.

## 에이전트에게 줄 기본 프롬프트 형식

```text
AGENTS.md를 먼저 읽어라.
대상 Issue 하나만 처리해라.
관련 문서는 다음이다.
- docs/...

범위 밖 작업은 하지 마라.
정책이 불명확하면 구현하지 말고 질문으로 남겨라.
완료 주장은 검증 로그를 남긴 뒤에만 해라.
작업 종류에 맞는 evidence를 남겨라.
```

## 이번 프로젝트 적용 순서

1. Spec Agent가 문서와 Issue 초안을 정리합니다.
2. 사람이 Issue 우선순위를 승인합니다.
3. Dev Agent가 `[feat] 커피 메뉴 목록 조회 API`부터 구현합니다.
4. Review Agent가 PR 또는 diff를 검토합니다.
5. QA Agent가 검증 레벨과 누락된 실제 실행 검증을 확인합니다.
6. 사람이 merge 또는 follow-up Issue 생성을 결정합니다.

## 현재 프로젝트 루트

`C:\Users\user\Desktop\Commerce-live-chat-system-Agora\.claude\worktrees\coffee-order-system`

## 주의

현재 프로젝트는 Claude worktree 경로 안에 있습니다. Git remote와 branch 상태를 항상 명시합니다.
