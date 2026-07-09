# Claude 및 LazyCodex 작업 가이드

이 프로젝트는 GitHub Issue를 구현 작업의 단일 출처로 사용합니다.

## 필수 흐름

1. `AGENTS.md`를 읽습니다.
2. 대상 Issue를 읽습니다.
3. 연결된 `docs/` 문서를 읽습니다.
4. Issue 범위만 구현합니다.
5. 가장 작은 관련 검증부터 실행합니다.
6. `docs/testing/verification-log.md`에 결과를 기록합니다.
7. 검증 레벨과 명령 결과를 함께 보고합니다.

## 금지 사항

- 요구사항, API, 도메인 정책, 검증 범위가 불명확한 상태에서 구현을 시작하지 않습니다.
- 현재 Issue가 아닌 기능을 함께 수정하지 않습니다.
- 과한 공통 프레임워크나 Generic Manager를 만들지 않습니다.
- Kafka, Redis, Testcontainers 또는 docker-compose 근거 없이 인프라 검증 완료라고 말하지 않습니다.
- 명시된 Issue 없이 DLT 메시지를 자동 재처리하지 않습니다.

## 권장 에이전트 역할

- Dev Agent: Issue 하나만 구현합니다.
- Review Agent: 요구사항 누락, 테스트 누락, 회귀 가능성, 과한 추상화를 검토합니다.
- QA Agent: 검증 누락과 재현 절차를 확인합니다. QA Agent는 구현을 직접 고치지 않습니다.