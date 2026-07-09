# Source Map

구현이 시작되면 패키지와 파일 위치를 이 문서에 맞춰 갱신합니다.

## 추천 패키지

| 영역 | 패키지 |
| --- | --- |
| 공통 예외 | `com.example.coffeeordersystem.common` |
| 메뉴 | `com.example.coffeeordersystem.menu` |
| 포인트 | `com.example.coffeeordersystem.point` |
| 주문 | `com.example.coffeeordersystem.order` |
| 랭킹 | `com.example.coffeeordersystem.ranking` |
| Kafka 이벤트 | `com.example.coffeeordersystem.event` |
| 설정 | `com.example.coffeeordersystem.config` |

## 구현 원칙

- Controller, Service, Repository 구조로 시작합니다.
- DTO는 각 도메인 패키지 안에 둡니다.
- 엔티티는 도메인 패키지 안에 두고, 지나친 공통 상속 구조는 만들지 않습니다.
- Kafka payload는 `event` 패키지에서 관리합니다.
- Redis key 생성 규칙은 한 곳에서 관리합니다.
- 레이어 책임과 금지 규칙은 `docs/architecture/layered-design-policy.md`를 따릅니다.

## 갱신 규칙

새 도메인 패키지를 추가하거나 책임 위치가 바뀌면 이 문서를 같이 수정합니다.
