# 서브에이전트 워크플로우

## 목적

서브에이전트는 구현 속도를 높이기 위한 도구가 아니라, 역할을 분리해서 추측과 누락을 줄이기 위한 도구입니다.

## 표준 역할

| 역할 | 입력 | 출력 |
| --- | --- | --- |
| Spec Agent | 과제 발제, 설계안, 강의자료, 현재 문서 | 정책 결정표, 질문 목록, Issue 후보 |
| Dev Agent | 하나의 Issue, 관련 문서, 현재 코드 | 구현 코드, focused test 결과, 검증 로그 초안 |
| Review Agent | PR diff, Issue, 관련 문서 | 리뷰 코멘트, 회귀 위험, 수정 필요 항목 |
| QA Agent | 실행 방법, 테스트 결과, 검증 로그 | 누락된 검증, evidence 보강 항목, follow-up Issue |
| Docs Agent | 확정된 결정, 구현 결과, 검증 결과 | README, ADR, API 명세, runbook 갱신 |

## 사용 순서

```text
Spec Agent
  -> 사람이 범위 승인
  -> Dev Agent
  -> Review Agent
  -> Dev Agent 수정
  -> QA Agent
  -> Docs Agent
  -> 사람이 merge/close
```

## Main Agent 역할

- Main Agent는 기본적으로 Coordinator입니다.
- 구현 Issue에서 Main Agent는 사용자가 명시적으로 허용하지 않는 한 production/test 코드를 직접 수정하지 않습니다.
- Main Agent는 Issue 범위 확인, 관련 문서 제공, Dev Agent 지시, diff 검토, 검증 재실행, evidence 정리, PR 업데이트를 담당합니다.
- 긴급한 빌드 스크립트 수정, 문서 오탈자, 충돌 해결처럼 Dev Agent 작업을 막는 작은 blocking 작업만 Main Agent가 직접 처리할 수 있습니다.
- Main Agent가 직접 수정했다면 PR 본문과 evidence에 이유를 남깁니다.

## 테스트 실행 소유권

- Dev Agent는 구현한 변경 범위에 대한 focused test를 실행하고 결과를 보고합니다.
- Review Agent는 테스트를 재실행하지 않고 diff, 요구사항 충족, 3계층 책임, 테스트 누락 여부만 검토합니다.
- QA Agent는 테스트를 재실행하지 않고 evidence, verification-log, 미검증 항목, 재현 절차의 충분성을 검토합니다.
- Main Agent만 최종 focused test와 전체 smoke test를 단일 실행으로 재검증합니다.
- 같은 워크스페이스에서 여러 에이전트가 동시에 Gradle `test` task를 실행하지 않습니다.

## 병렬 사용 예시

- Review Agent와 QA Agent는 같은 diff를 읽고 서로 다른 관점으로 검토할 수 있습니다.
- Docs Agent는 Dev Agent 구현이 끝난 뒤 확정된 변경만 문서화합니다.
- Spec Agent는 다음 Issue 후보를 정리할 수 있지만, 현재 Issue 구현 범위를 바꾸면 안 됩니다.

## 금지 예시

- 두 Dev Agent가 같은 Service 또는 Entity를 동시에 수정합니다.
- 여러 에이전트가 같은 워크스페이스에서 Gradle `test` task를 동시에 실행합니다.
- 정책이 확정되지 않은 상태에서 Dev Agent가 구현을 시작합니다.
- QA Agent가 검증 누락을 발견하고 직접 코드를 수정합니다.
- Review Agent가 요구사항을 바꿔서 구현 범위를 넓힙니다.

## 완료 보고 형식

```text
역할:
대상 Issue:
읽은 문서:
수행한 작업:
검증:
미검증:
후속 Issue 후보:
```
