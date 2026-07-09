# Issue #3 Commands

## RED

```text
./gradlew.bat test --tests com.example.coffeeordersystem.db.DatabaseSchemaIntegrationTest
```

결과입니다.

```text
BUILD FAILED
package com.example.coffeeordersystem.event.domain does not exist
package com.example.coffeeordersystem.menu.domain does not exist
package com.example.coffeeordersystem.order.domain does not exist
package com.example.coffeeordersystem.point.domain does not exist
```

이 실패는 Flyway migration, Entity, Repository가 아직 없음을 확인하기 위한 예상 실패입니다.

## GREEN focused integration

```text
./gradlew.bat test --tests com.example.coffeeordersystem.DatabaseSchemaIntegrationTest
```

결과입니다.

```text
BUILD SUCCESSFUL in 1m 14s
4 actionable tasks: 4 executed
```

확인한 내용입니다.

- Flyway가 `menu`, `user_point`, `orders`, `processed_event` table을 생성합니다.
- `menu` seed data 4건이 조회됩니다.
- JPA Repository가 schema-backed entity를 저장하고 다시 읽습니다.

## Full smoke

```text
./gradlew.bat test
```

결과입니다.

```text
BUILD SUCCESSFUL in 1m 21s
4 actionable tasks: 1 executed, 3 up-to-date
```
