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

## 참고

- `./gradlew test` 전체 실행은 이 변경과 무관하게 `ClassNotFoundException`으로 실패합니다. `git stash`로 원본(main HEAD) 상태에서 동일하게 재현되는 것을 확인해 기존 로컬 Gradle 테스트 실행 환경 문제이며 이번 변경의 회귀가 아님을 확인했습니다. Level 1 전체 회귀의 최종 판정은 GitHub Actions `quality-gates`를 따릅니다(정책상 로컬 실행 환경 문제로 완료 근거를 대체하지 않으며, CI 결과를 merge 전 확인합니다).
- `Current head`는 이 커밋을 가리킵니다: `e09452e0d05b883606af7e6ae6bb38a500c67914`.
