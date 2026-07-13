# GitHub Issue 초안

이 문서는 구현 순서를 고정하기 위한 Issue 후보입니다. 실제 GitHub Issue를 만들 때 하나씩 복사해 사용합니다.

## 추천 순서

| 순서 | 제목 | 유형 | 검증 레벨 |
| --- | --- | --- | --- |
| 1 | 프로젝트 의존성 정리와 로컬 실행 기준 확정 | chore | Level 0, Level 1 |
| 2 | Flyway 기반 DB 스키마와 메뉴 seed 데이터 구성 | feature | Level 3 |
| 3 | 메뉴 목록 조회 API 구현 | feature | Level 2 |
| 4 | 포인트 충전 API 구현 | feature | Level 2, Level 3 |
| 5 | 주문 결제 API와 포인트 차감 트랜잭션 구현 | feature | Level 2, Level 3 |
| 6 | Redisson 사용자 주문 진입 락 적용 | feature | Level 4 |
| 7 | 주문 완료 Kafka 이벤트 발행 구현 | feature | Level 4 |
| 8 | Kafka Consumer 멱등 처리와 Redis 랭킹 반영 구현 | feature | Level 4 |
| 9 | 인기 메뉴 Top 3 API 구현 | feature | Level 2, Level 4 |
| 10 | DLT 이동과 실패 재시도 설정 구현 | feature | Level 4 |
| 11 | Postman 또는 http 요청 산출물 작성 | test | Level 6 |
| 12 | k6 부하 테스트 시나리오 작성 | test | Level 7 |
| 13 | Kafka replay 기반 Redis rebuild 도전 기능 | stretch | Level 4, Level 5 |
| 14 | DLT 재처리 스크립트 도전 기능 | stretch | Level 4, Level 5 |
| 15 | QueryDSL 검증 조회와 EXPLAIN 문서화 | stretch | Level 3 |

## Issue 본문 기본 형식

```markdown
## 목표

## 범위

## 제외 범위

## 관련 문서

## 완료 기준

## 검증
```

## 먼저 만들면 좋은 Issue

### 프로젝트 의존성 정리와 로컬 실행 기준 확정

목표는 현재 Spring Initializr 결과와 설계 문서 사이의 차이를 정리하는 것입니다.

범위입니다.

- Spring Boot 버전 확정.
- Redisson 의존성 추가 여부 확정.
- Testcontainers 이미지 tag 고정.
- QueryDSL을 즉시 추가할지 후반 Issue로 둘지 결정.
- Gradle 테스트 통과 확인(Windows: `.\gradlew.bat test`, macOS·Linux: `./gradlew test`).

제외 범위입니다.

- API 구현.
- DB schema 구현.
- Kafka Consumer 구현.

완료 기준입니다.

- `docs/onboarding/dependency-check.md`가 최신 상태입니다.
- Gradle test 결과가 해당 Issue의 `docs/testing/evidence/issue-{number}/verification.md`에 기록되어 있습니다. 전역 뷰가 필요하면 [Evidence Guide](../testing/evidence-guide.md)의 재현 명령을 사용하며 생성 파일은 커밋하지 않습니다.
