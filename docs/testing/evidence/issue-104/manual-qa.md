# Manual QA

API 계약이나 런타임 동작을 바꾸지 않는 내부 구현 치환이라 실제 HTTP 요청 등 Level 6 QA 대상은 아닙니다. 아래는 사람이 직접 관찰한 결과입니다.

## 관찰 1 — 컴파일·테스트 코드 호환성

`./gradlew compileJava`, `./gradlew compileTestJava`를 실행해 Lombok이 생성하는 getter/생성자가 기존 수동 작성 코드를 호출하던 모든 지점(Service, Repository, DTO 변환 등)과 시그니처가 완전히 호환됨을 확인했습니다. 컴파일 에러 0건.

## 관찰 2 — 독립 Combined Verifier subagent 검토

이 작업의 대화 맥락을 전달받지 않은 fresh general-purpose agent를 별도로 실행해 diff(`%TEMP%/issue-104.diff`)와 실제 저장소 파일을 직접 대조하도록 했습니다. 확인 항목과 결과:

- Entity 5종의 getter/생성자 시그니처가 필드명·타입·순서까지 정확히 일치함 — 확인.
- `UserPoint.charge()`/`pay()`(`point/domain/UserPoint.java:36-52`), `OutboxEvent.markPublished()`(`order/event/OutboxEvent.java:48-50`) 도메인 메서드가 그대로 보존됨 — 확인.
- 남아있는 수동 getter/생성자, Lombok과 수동 정의 중복으로 인한 컴파일 충돌 가능성 — 없음.
- `docs/architecture/layered-design-policy.md`의 새 Entity 절이 실제 코드와 일치함 — 확인.
- `MenuController.java`, `MenuService.java`의 헤더 주석이 각 파일의 실제 책임을 정확히 설명함(범용 문구 아님) — 확인.
- `git diff main..HEAD --stat` 기준 의도한 7개 파일 외 변경 없음 — 확인.

판정: PASS. 지적 사항 없음.

## 관찰 3 — 실제 테스트 실행(Level 1/2/4)

원래 작업 디렉터리(한글 경로)에서는 `./gradlew test`가 `ClassNotFoundException`으로 실행 자체가 안 되어(세부 원인은 `verification.md` 참고), 같은 커밋을 WSL Ubuntu 클론과 Docker Desktop이 붙는 비한글 Windows 경로 클론(`C:\coffee-verify`)에서 fetch해 실제로 실행했습니다.

- `C:\coffee-verify`에서 `./gradlew.bat test` 전체 실행 — `build/test-results/test/*.xml` 27개 파일 집계 결과 tests=76, failures=0, errors=0, skipped=0.
- WSL 클론에서 `./gradlew test --tests '*MenuControllerTest*'` — tests=2, failures=0, errors=0(`getMenusReturnsSeedMenus`, `getPopularMenusReturnsRankedMenus`).
- `C:\coffee-verify`에서 `./gradlew.bat test --tests '*OutboxEventIntegrationTest*'` — tests=2, failures=0, errors=0(`publishPendingDeliversOutboxEventToKafkaAndMarksItPublished`, `createOrderSavesOutboxEventInOrderTransaction`), 실제 Kafka Testcontainers를 통해 검증.

## 미검증 항목

없음. Level 1(전체 회귀), Level 2(Controller), Level 4(Kafka)를 모두 실제로 실행해 확인했습니다.
