# Issue #104 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/104

Execution mode: STANDARD
Execution mode reason: schema, transaction, lock, Kafka, Redis, security 위험이 없는 Lombok getter/생성자 치환과 문서 추가입니다. Entity 5종에 걸쳐 있지만 각 파일은 서로 독립적인 동일 패턴 적용이며 domain 메서드(charge/pay/markPublished)는 변경하지 않았습니다.
Level 5 required: NO
Level 5 reason: API 동작, 런타임 설정, 인프라 연결을 바꾸지 않는 내부 구현 치환입니다. Lombok이 생성하는 getter/생성자 signature가 기존 수동 작성분과 동일함을 compileJava/compileTestJava 성공과 독립 리뷰로 확인했습니다.
Level 6 required: NO
Level 6 reason: Controller/DTO 계약이 바뀌지 않아 실제 HTTP 요청 검증 대상이 아닙니다.

## 완료 기준

- [x] `docs/architecture/layered-design-policy.md`에 Entity 컨벤션 절(protected 생성자, getter만 공개, Lombok 구현)을 추가했습니다.
- [x] Entity 5종(`Menu`, `UserPoint`, `Order`, `ProcessedEvent`, `OutboxEvent`)이 모두 동일한 Lombok 패턴(`@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`)을 사용합니다.
- [x] `MenuController.java`, `MenuService.java`에 누락되어 있던 한 줄 한국어 파일 헤더 주석을 추가했습니다.
- [x] `./gradlew compileJava`, `./gradlew compileTestJava` 성공을 확인했습니다.
- [x] 독립 검증 agent(general-purpose, fresh)가 diff를 직접 읽고 getter/생성자 signature 동일성, domain 메서드 보존, 범위 밖 변경 없음을 확인했습니다(PASS).
- [x] 전체 회귀(76 tests, failures 0, errors 0), `MenuControllerTest`(Level 2), `OutboxEventIntegrationTest`(Level 4, 실제 Kafka Testcontainers)를 실제로 실행해 PASS를 확인했습니다.

## 참고

- 원래 작업 디렉터리 경로(한글 포함)에서는 Windows 시스템 로캘(MS949)과의 조합으로 Gradle 테스트 워커가 `ClassNotFoundException`으로 실행 자체가 되지 않았습니다(`git stash`로 원본 `main` HEAD에서도 재현). 이 제약을 피해 WSL Ubuntu 클론과 비한글 Windows 경로 클론(`C:\coffee-verify`, Docker Desktop 사용)에서 같은 커밋을 fetch해 실제로 테스트를 실행했습니다. 세부 내용은 `verification.md` 참고.
- `Current head`는 이 커밋을 가리킵니다: `e09452e0d05b883606af7e6ae6bb38a500c67914`.
