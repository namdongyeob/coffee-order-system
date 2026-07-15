# 코드 스타일 가이드

이 문서는 계층 책임 규칙([계층 설계 정책](layered-design-policy.md))과 별개로, 실제 코드에서 이미 일관되게 지켜지고 있는 네이밍·DTO·테스트 작성 스타일을 정리합니다. 여기 실린 규칙은 전부 기존 코드에서 예외 없이 관찰된 패턴이며, 모듈마다 갈려 있는 패턴(예외 처리 방식, 로깅 방식)은 의도적으로 제외했습니다.

## DTO

- 요청·응답 DTO는 클래스가 아니라 Java `record`로 작성합니다.
- Entity를 감싸는 응답 DTO는 정적 팩토리 메서드 `from(entity)`를 둡니다. Entity 매핑이 필요 없는 단순 DTO는 `from`을 두지 않습니다.
- 요청 DTO는 record 컴포넌트에 Jakarta Bean Validation 애노테이션(`@NotNull`, `@Positive`, `@Min`, `@Max` 등)을 직접 붙입니다.

예: `order/dto/OrderResponse.java`의 `public record OrderResponse(...)` + `public static OrderResponse from(Order order)`, `menu/dto/MenuResponse.java`, `point/dto/PointChargeResponse.java`도 같은 패턴입니다. 요청 DTO는 `order/dto/OrderCreateRequest.java`, `point/dto/PointChargeRequest.java`를 참고합니다.

## 패키지와 클래스 네이밍

- 패키지는 기능별로 나누고, 그 안에서 `controller`, `service`, `repository`, `domain`, `dto`로 다시 나눕니다(예: `menu/controller`, `menu/service`, `menu/repository`, `menu/domain`, `menu/dto`).
- 클래스명은 역할을 접미사로 드러냅니다. `Controller`, `Service`, `Repository`, `Request`, `Response`.

## 테스트 네이밍

- 좁은 범위 단위·슬라이스 테스트(`@WebMvcTest`, Mockito 단위 테스트 등)는 기능 패키지 아래 `XxxTest`로 둡니다. 예: `menu/controller/MenuControllerTest.java`, `order/service/OrderServiceLockTest.java`.
- Testcontainers 기반 `@SpringBootTest` 풀스택 테스트는 `XxxIntegrationTest`로 둡니다. 패키지 위치는 root 테스트 패키지(`OrderPaymentIntegrationTest.java`)와 기능 패키지(`ranking/rebuild/RankingRebuildServiceIntegrationTest.java`)가 섞여 있어 위치 자체는 규칙이 아니지만, 같은 대상을 다루는 순수 단위 테스트(`RankingRebuildServiceTest.java`)와는 `IntegrationTest` 접미사로 항상 구분됩니다.
- 테스트 메서드명은 `test` 접두사나 언더스코어 없이, "행동 + 기대 결과"를 서술하는 camelCase 문장형으로 짓습니다. 예: `getMenusReturnsSeedMenus`, `chargeCreatesUserPointWhenRowDoesNotExist`, `createOrderFailsWithConflictWhenUserLockCannotBeAcquired`, `createOrderReturnsNotFoundWhenMenuDoesNotExist`.

## Import

- wildcard import(`import foo.*`)를 쓰지 않습니다. 항상 명시적으로 클래스 단위로 import합니다.

## 상수

- 매직 넘버·문자열·기간은 `private static final` 필드로 빼고 이름은 UPPER_SNAKE_CASE로 씁니다. 예: `point/service/PointService.java`의 `MAX_CHARGE_AMOUNT`, `MAX_CONCURRENCY_RETRIES`, `ranking/service/PopularMenuRankingService.java`의 `KEY_PREFIX`, `PROCESSED_KEY_TTL`, `ranking/rebuild/RankingRebuildService.java`의 `TOPIC`, `NORMAL_GROUP`, `POLL_TIMEOUT`.

## 이 문서에서 다루지 않는 것

- 예외 처리: `common/ApiException.java` + `common/ErrorCode.java` 패턴이 API 계층 검증·비즈니스 오류에는 쓰이지만, `ranking/rebuild/RankingRebuildException.java`, `recovery/DltReplayException.java`처럼 모듈 전용 예외를 따로 두거나 `IllegalStateException`을 직접 던지는 곳도 있어 저장소 전체의 단일 규칙으로 문서화하지 않습니다.
- 로깅: `@Slf4j`를 쓰는 클래스(`recovery/DltReplayRunner.java`, `order/event/OrderEventPublisher.java`, `order/event/OutboxEventPublisher.java`)가 일부 있지만 핵심 `Service` 4종(`MenuService`, `OrderService`, `PointService`, `PopularMenuRankingService`)에는 로깅이 전혀 없어 일관된 규칙으로 보기 어렵습니다.
