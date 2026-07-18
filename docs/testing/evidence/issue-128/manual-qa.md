# Manual QA

## Automated verification

- BLOCKED + required Level 5/6 + 각 Level PARTIAL + 구체적 최신 `Failure Cause` 조합이 PASS하는 것을 확인했습니다.
- BLOCKED에서 required Level PARTIAL 또는 blocker가 없으면 각각 FAIL하는 것을 확인했습니다.
- PASS에서 required Level의 PARTIAL은 계속 FAIL하고 동일 Issue·Level의 PASS만 인정합니다.

## Manual QA

- 앱·DB·Kafka·Redis 동작을 변경하지 않아 Level 5/6은 실행하지 않았습니다.
- 변경 파일은 하네스, 직접 test, Issue #128 evidence로 제한했습니다.

## Adversarial QA

- `BLOCKED`에 PASS 행을 넣는 기존 모순 검사는 계속 실패합니다.
- blocker를 `없음`으로 기록하면 실패합니다.
- 다른 Issue, 다른 Level 또는 PARTIAL로 PASS disposition을 만족시킬 수 없습니다.

## Cleanup receipt

- Java, Gradle, Docker 인프라를 기동하지 않았습니다.
- 테스트 임시 디렉터리는 `TemporaryDirectory` 종료 시 정리됐습니다.

## Unverified items and remaining risks

- 독립 Review, 독립 QA와 최신 PR head CI는 PR 생성 후 후속 gate입니다.
- blocker 문장의 사실성은 evidence 작성자와 독립 검증 역할이 확인하며 하네스는 비어 있거나 명백한 placeholder인지만 fail-closed로 검사합니다.
