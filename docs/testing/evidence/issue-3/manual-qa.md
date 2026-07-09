# Issue #3 Manual QA

Issue #3은 DB migration과 JPA schema 정합성 작업입니다. UI 화면이 없으므로 screenshot 대신 DB schema 테스트와 seed 조회를 evidence로 사용합니다.

## 확인 결과

- Flyway가 `menu`, `user_point`, `orders`, `processed_event` table을 생성합니다.
- 메뉴 seed 데이터가 조회됩니다.
- Repository가 schema-backed entity를 저장하고 다시 읽습니다.

확인 방식입니다.

- `DatabaseSchemaIntegrationTest.flywayCreatesRequiredTablesAndMenuSeedData`
- `DatabaseSchemaIntegrationTest.repositoriesPersistAndReadSchemaBackedEntities`

## Screenshot 여부

Issue #3은 백엔드 DB schema 작업입니다. 사용자 화면이 없으므로 screenshot은 만들지 않았습니다. 대신 Testcontainers MySQL 기반 schema 테스트 결과를 evidence로 남겼습니다.

## 서브에이전트

- 사용 여부: 사용하지 않음.
- 이유: DB migration, Entity, Repository, schema test는 순서 의존성이 커서 병렬 Dev Agent로 나누면 schema와 mapping이 어긋날 위험이 큽니다.
