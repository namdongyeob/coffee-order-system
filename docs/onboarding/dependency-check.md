# 의존성 점검

현재 IntelliJ에서 생성된 프로젝트와 설계 문서 사이의 차이를 정리합니다.

## 현재 포함된 의존성

| 의존성 | 상태 | 비고 |
| --- | --- | --- |
| Spring Web MVC | 포함 | REST API 구현에 사용합니다. |
| Spring Data JPA | 포함 | 메뉴, 포인트, 주문, 이벤트 처리 기록 저장에 사용합니다. |
| MySQL Driver | 포함 | 로컬과 테스트 DB 기준입니다. |
| Spring Data Redis | 포함 | RedisTemplate 또는 RedisRepository 기반 접근에 사용합니다. |
| Spring for Apache Kafka | 포함 | 주문 완료 이벤트 발행과 Consumer 구현에 사용합니다. |
| Validation | 포함 | 요청 DTO 검증에 사용합니다. |
| Lombok | 포함 | DTO와 엔티티 보일러플레이트 감소에 사용합니다. |
| Actuator | 포함 | 상태 확인과 관찰용입니다. |
| Testcontainers | 포함 | MySQL, Redis, Kafka 통합 테스트에 사용합니다. |
| Spring REST Docs | 미포함 | 현재 `build.gradle`에는 명시적으로 포함하지 않습니다. API 문서는 Markdown 우선으로 진행합니다. |
| Flyway Migration | 포함 | DB schema 고정에 사용합니다. |

## 추가 검토가 필요한 의존성

| 의존성 | 추천 | 이유 |
| --- | --- | --- |
| Redisson | 추가 완료 | 설계 문서에서 사용자 주문 진입 락을 Redisson으로 정했고 `build.gradle`에 `redisson-spring-boot-starter:4.6.1`을 추가했습니다. |
| QueryDSL | 후반 Issue 추천 | 인기 메뉴 API는 Redis 기준으로 충분합니다. DB 검증 조회와 EXPLAIN 단계에서 추가하는 편이 범위 관리에 좋습니다. |
| Spring REST Docs | 선택 | 과제 제출용 문서는 현재 Markdown이 더 빠릅니다. API 계약이 안정된 뒤 추가해도 됩니다. |

## 버전 점검

현재 `build.gradle`의 Spring Boot 버전은 `4.1.0`입니다. Spring 공식 릴리스 공지 기준 Spring Boot 4.1.0은 2026-06-10에 Maven Central에 공개된 정식 릴리스입니다.

이번 프로젝트는 Spring Boot 4.1.0 기준으로 유지합니다. Redisson은 `redisson-spring-boot-starter:4.6.1`을 사용합니다. Redisson 4.6.1 릴리스 노트에는 Spring Boot 4.1.0 integration 항목이 포함되어 있습니다.

강의 자료가 Spring Boot 3.x 중심일 수 있으므로 예제 코드나 문서와 충돌이 생기면 해당 Issue에서만 다운그레이드 여부를 다시 판단합니다. 충돌이 없다면 버전 변경은 하지 않습니다.

참고입니다.

- Spring Boot 4.1.0 릴리스 공지: https://spring.io/blog/2026/06/10/spring-boot-4
- Redisson release notes: https://github.com/redisson/redisson/releases

## Testcontainers 이미지 점검

현재 테스트 설정은 다음 이미지 tag를 사용합니다.

- `apache/kafka-native:3.9.1`
- `mysql:8.4.5`
- `redis:7.4.2`

`latest`는 시간이 지나면 같은 테스트가 다른 이미지로 실행될 수 있으므로 고정 tag로 변경했습니다.

## 추천 결정

- Redisson은 추가했습니다.
- QueryDSL은 MVP 구현 후 검증 조회 Issue에서 추가합니다.
- Spring REST Docs는 선택으로 두고, Markdown API 명세와 http/Postman 산출물을 먼저 만듭니다.
- Testcontainers 이미지는 고정 tag로 바꿨습니다.
- Spring Boot는 4.1.0을 유지합니다.
