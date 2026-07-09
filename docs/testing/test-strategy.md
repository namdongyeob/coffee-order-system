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

## k6 우선순위

1. Load Test.
2. Stress Test.
3. Spike Test.
4. Soak Test는 문서상 후보로만 둡니다.

## 완료 규칙

Mock 테스트는 DB, Kafka, Redis, 로컬 실행, 실제 API 검증을 대체하지 않습니다.