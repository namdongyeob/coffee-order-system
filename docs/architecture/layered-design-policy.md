# 3계층 설계 정책

이 프로젝트는 Controller, Service, Repository 3계층을 기본 구조로 사용합니다. 목표는 단순한 구조를 유지하면서 도메인 규칙, 트랜잭션, 인프라 연동이 섞여 스파게티 코드가 되는 것을 막는 것입니다.

## 기본 방향

- 기능은 메뉴, 포인트, 주문, 랭킹, 이벤트 책임으로 나눕니다.
- 각 기능 안에서는 Controller, Service, Repository 흐름을 우선합니다.
- 새 계층은 문제를 실제로 줄일 때만 추가합니다.
- 공통화는 세 번째 중복이 보이고 책임이 명확할 때만 검토합니다.

## Controller

Controller가 담당합니다.

- HTTP method와 path 매핑.
- 요청 DTO validation.
- 인증 사용자나 path/query/body 값 추출.
- Service 호출.
- 응답 DTO와 HTTP status 반환.

Controller가 하지 않습니다.

- 포인트 잔액 검증 같은 비즈니스 판단.
- 트랜잭션 경계 설정.
- Repository 직접 호출.
- Redis, Kafka, Redisson 직접 호출.
- Entity를 그대로 응답으로 반환.

## Service

Service가 담당합니다.

- 비즈니스 규칙.
- 트랜잭션 경계.
- 주문과 포인트 차감 같은 유스케이스 흐름.
- Redisson 락 획득과 해제 흐름.
- Kafka 이벤트 발행 요청.
- 여러 Repository 호출 조합.

Service가 하지 않습니다.

- HTTP status나 Controller 전용 응답 포맷 결정.
- JPA query 세부 구현.
- Redis key 문자열을 여러 곳에 직접 조립.
- 모든 기능을 한 서비스에 몰아넣기.

## Repository

Repository가 담당합니다.

- Entity 저장과 조회.
- JPA query method.
- 비관적 락 조회.
- 필요한 경우 QueryDSL 기반 조회.

Repository가 하지 않습니다.

- 포인트 부족, 주문 가능 여부 같은 비즈니스 판단.
- Kafka 이벤트 발행.
- Redis ranking 갱신.
- Controller DTO 반환.

## 인프라 연동 위치

| 인프라 | 위치 |
| --- | --- |
| MySQL/JPA | Repository |
| DB transaction | Service |
| Redisson lock | Service 또는 전용 lock component |
| Redis ranking | ranking 책임의 Service 또는 전용 adapter |
| Kafka Producer | event 책임의 producer component |
| Kafka Consumer | event consumer component가 받고 ranking Service를 호출 |

전용 component를 만들더라도 기능 흐름은 Service에서 읽히게 유지합니다.

## 금지 패턴

- Controller에서 Repository를 바로 호출합니다.
- 하나의 Service가 메뉴, 포인트, 주문, 랭킹을 모두 처리합니다.
- `CommonService`, `Manager`, `Helper` 같은 이름으로 책임을 숨깁니다.
- Entity와 API DTO를 같은 목적으로 사용합니다.
- 예외 처리가 Controller마다 흩어집니다.
- Redis key나 Kafka topic 이름이 여러 클래스에 문자열로 흩어집니다.

## 리뷰 기준

PR 리뷰에서는 다음 질문으로 확인합니다.

- 이 코드를 읽으면 요청 흐름이 Controller -> Service -> Repository 순서로 보이나요?
- 비즈니스 규칙이 Controller나 Repository에 새어 나갔나요?
- 트랜잭션 경계가 Service에 있나요?
- 인프라 세부사항이 도메인 규칙을 읽기 어렵게 만들고 있나요?
- 공통화가 책임을 명확하게 만들었나요, 아니면 숨겼나요?
