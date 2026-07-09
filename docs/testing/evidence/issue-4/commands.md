# Issue #4 Commands

## RED

```text
./gradlew.bat test --tests com.example.coffeeordersystem.menu.controller.MenuControllerTest
```

결과입니다.

```text
BUILD FAILED
MenuControllerTest > getMenusReturnsSeedMenus() FAILED
AssertionFailedError at MenuControllerTest.java:33
```

의미입니다.

- `GET /api/menus` endpoint가 아직 구현되지 않았기 때문에 기대한 200 응답을 받지 못했습니다.
- 테스트가 실제 HTTP surface를 호출한다는 점을 확인했습니다.

## GREEN focused API

```text
./gradlew.bat clean test --tests com.example.coffeeordersystem.menu.controller.MenuControllerTest --no-daemon
```

결과입니다.

```text
BUILD SUCCESSFUL in 35s
5 actionable tasks: 5 executed
```

확인한 내용입니다.

- `GET /api/menus`가 200을 반환합니다.
- seed menu 4건의 `id`, `name`, `price`가 응답 body에 포함됩니다.
- Testcontainers를 쓰는 full context test가 아니라 `@WebMvcTest`와 `MockMvc`로 Controller 계약만 검증합니다.

## QA Agent 재검증 반영

Review Agent와 QA Agent가 초기 full context HTTP 테스트에서 Gradle test result store `EOFException`이 재현된다고 보고했습니다. 원인은 기능 assertion 실패가 아니라 무거운 Testcontainers 기반 테스트와 동시 재검증이 섞인 test result store 문제였습니다.

수정입니다.

- Issue #4 요구사항에 맞게 Controller slice test로 전환했습니다.
- `@WebMvcTest(MenuController.class)`와 `MockMvc`로 HTTP mapping, status, JSON body를 검증합니다.
- 재검증은 `--no-daemon`으로 단일 프로세스에서 수행했습니다.

## Full smoke

```text
./gradlew.bat test --no-daemon
```

결과입니다.

```text
BUILD SUCCESSFUL in 1m 13s
4 actionable tasks: 1 executed, 3 up-to-date
```
