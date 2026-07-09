# 테스트 전략

## 필수 검증

| Level | 대상 |
| --- | --- |
| Level 1 Unit | 서비스와 도메인 정책. |
| Level 2 Controller | 요청, 응답, 검증, 에러 포맷. |
| Level 3 DB Integration | JPA, 트랜잭션, 비관적 락, 동시성. |
| Level 4 Infra Integration | Kafka, Redis, Redisson, DLT. |
| Level 5 Local Run | 로컬 애플리케이션 기동 후 API 호출. |
| Level 6 Postman/curl/http | 실제 API 요청 산출물. |
| Level 7 k6 | Load, Stress, Spike 관찰. |

## 실행 속도와 신뢰성 기준

- 개발 중에는 변경 범위에 맞는 focused test를 먼저 실행합니다.
- 작은 Controller API는 `@WebMvcTest`와 `MockMvc`로 HTTP mapping, status, response body를 검증합니다.
- DB schema, JPA mapping, transaction, lock이 핵심인 Issue는 Level 3 DB Integration으로 검증합니다.
- Kafka, Redis, Redisson, DLT가 핵심인 Issue는 Level 4 Infra Integration으로 검증합니다.
- Testcontainers는 필요한 검증 레벨에서만 사용합니다. Controller 계약만 확인하는 Issue에 무거운 full context 테스트를 기본값으로 두지 않습니다.
- PR 업데이트 전에는 전체 `./gradlew.bat test --no-daemon` smoke test를 실행합니다.
- 전체 테스트가 느리거나 불안정하면 focused test 결과와 함께 원인, 재현 명령, 남은 미검증 항목을 evidence에 남깁니다.

## 에이전트별 검증 분담

- Dev Agent는 자기 변경 범위의 focused test를 실행합니다.
- Review Agent는 테스트를 재실행하지 않습니다. 대신 diff, 요구사항, 설계 경계, 테스트 케이스 누락을 검토합니다.
- QA Agent는 테스트를 재실행하지 않습니다. 대신 evidence와 verification-log가 완료 주장을 뒷받침하는지 검토합니다.
- Main Agent가 최종 focused test와 전체 smoke test를 단일 실행으로 재검증합니다.
- 같은 워크스페이스에서 Gradle 테스트를 병렬 실행하지 않습니다. 병렬 실행이 필요하면 별도 worktree 또는 별도 build directory를 사용합니다.

## k6 우선순위

1. Load Test.
2. Stress Test.
3. Spike Test.
4. Soak Test는 문서상 후보로만 둡니다.

## 완료 규칙

Mock 테스트는 DB, Kafka, Redis, 로컬 실행, 실제 API 검증을 대체하지 않습니다.
