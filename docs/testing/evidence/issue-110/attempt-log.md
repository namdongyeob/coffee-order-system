# Issue Attempt Log

Issue: #110
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/110
Branch: issue-110
Current disposition: PASS
Current Attempt: 5
Current head: 8ba84153c49384bc35fa660162c56ff7adeef12a

## Attempt 5

### Generate

- `RankingRebuildServiceIntegrationTest`의 매 테스트 시작 시 Kafka `order.completed` 기록을 정리해 #110의 신규 중복 eventId 테스트가 선행 테스트 레코드를 읽지 않도록 했습니다.
- 기존 offset 복구 테스트 두 건이 DB에 삽입한 주문 전체의 Kafka 이벤트를 직접 발행하도록 보완했습니다.

### Evaluate

- 기존 전체 suite에서는 #110의 신규 `deduplicatesMatchingEventIdsAndExposesReplayMetrics`가 선행 Kafka 레코드를 포함해 DB 집계 불일치로 실패했습니다.
- Kafka 정리만 적용한 재현에서 기존 offset 복구 테스트 두 건이 선행 레코드에 의존했음을 확인했고, 각 테스트의 누락 이벤트를 직접 발행한 뒤 독립 실행을 확인했습니다.

### Failure Cause

- 테스트 fixture가 Redis와 DB만 정리하고 Kafka 토픽 레코드는 유지해, 테스트 실행 순서에 따라 서로의 이벤트를 읽었습니다.

### Change Scope

- `RankingRebuildServiceIntegrationTest`만 수정했습니다.
- production 코드, ledger, DLT, runner 설정, Kafka topic과 offset 설계는 변경하지 않았습니다.

### Reverification

- `clean test`로 rebuild 테스트 클래스 10건, #110 Level 4 focused 2건, 전체 suite 85건을 모두 failures 0, errors 0으로 확인했습니다.
- 전체 suite는 `BUILD SUCCESSFUL in 4m 39s`와 Gradle daemon exit status 0을 확인했습니다.

### Next Attempt

- 없음.

## Attempt 4

### Generate

- `ranking.rebuild.enabled=true`이면 일반 `RankingEventConsumer` bean이 등록되지 않도록 조건을 추가했습니다.
- maintenance runner 활성화 시 consumer bean이 없고, 기본 실행에서는 기존 consumer가 등록되는 focused 조건 테스트를 추가했습니다.

### Evaluate

- 수정 전 조건 테스트는 rebuild enabled인데 `RankingEventConsumer` bean이 존재하여 의도한 대로 FAIL 했습니다.
- 수정 후 focused Level 4는 실제 Kafka·MySQL·Redis Testcontainers에서 동일 payload 중복 제거와 충돌 payload fail-closed를 PASS 했습니다.
- Level 5에서 local compose Kafka·MySQL·Redis를 healthy로 기동하고 `order.completed` topic을 만든 뒤 maintenance runner를 실행했습니다.

### Failure Cause

- 최초 runtime 실행은 `Start-Process`가 application argument를 Gradle option으로 전달해 시작 전 실패했습니다. 환경변수 방식으로 교정했습니다.
- 교정된 첫 runtime 실행은 비활성화된 일반 consumer가 topic을 자동 생성하지 않는 clean compose 환경에서 `UnknownTopicOrPartitionException`으로 fail-closed 했습니다. `order.completed` topic을 명시적으로 생성한 뒤 재실행했습니다.

### Change Scope

- 일반 ranking Kafka consumer의 maintenance runner 상호 배제, focused 조건 테스트, Issue #110 evidence만 변경했습니다.
- 영구 ledger, DLT replay, Redis 조회·tie 정책, Kafka offset·topic 구조는 변경하지 않았습니다.

### Reverification

- `clean test` focused command는 조건 테스트 2건과 actual Kafka·MySQL·Redis integration 2건을 failures 0, errors 0으로 PASS 했습니다.
- Level 5 runner는 `ranking_rebuild_completed inputRecords=0 uniqueEvents=0 conflicts=0`를 남겼고, `ranking-consumer-group`은 active member가 없음을 Kafka CLI로 확인했습니다.
- compose 컨테이너와 runner process tree를 정리했습니다.

### Next Attempt

- 없음.

## Attempt 3

### Generate

- Rebooted Git environment was available; no Gradle, Java, or Git HTTPS process was running before the focused test.

### Evaluate

- The required clean focused Level 4 command compiled main and test sources, but the Gradle test executor could not load any compiled test class.
- The binary test result reports `ClassNotFoundException`, including `RankingRebuildServiceIntegrationTest`; target test XML was not produced.

### Failure Cause

- This reproduces the prior environment-level Gradle test-runtime class-loading failure. The focused test body did not run, so the result cannot validate the Issue behavior.

### Change Scope

- No production or test source was changed after the failed retry. Only Issue #110 evidence is updated.

### Reverification

- `gradlew.bat clean test --tests "*RankingRebuildServiceIntegrationTest.deduplicatesMatchingEventIdsAndExposesReplayMetrics" --tests "*RankingRebuildServiceIntegrationTest.rejectsConflictingPayloadsForTheSameEventId" --no-daemon --max-workers=1 --console=plain` was blocked by the same class-loading failure.

### Next Attempt

- Do not retry unchanged. Run the focused Level 4 test only after the Gradle runtime can load compiled test classes.

## Attempt 1

### Generate

- Rebuild 범위에서 `eventId`와 주문 핵심 payload를 비교해 동일 payload는 한 번만 집계하고, 충돌은 예외와 구조화 로그로 중단하도록 구현했습니다.
- `RankingRebuildResult`와 runner 로그에 input, unique, conflict 지표를 추가했습니다.
- 실제 Kafka, Redis Testcontainers를 사용하는 동일 eventId 및 충돌 payload focused test를 추가했습니다.

### Evaluate

- RED는 새 지표 accessor가 없어 `compileTestJava`가 실패하는 것으로 확인했습니다.
- `clean compileJava --no-daemon --max-workers=1 --console=plain`은 PASS 했습니다.
- focused Level 4 test는 Gradle Test Executor가 모든 test class를 `ClassNotFoundException`으로 보고하고 problems report 쓰기 충돌까지 발생해 test body 실행 전 실패했습니다.
- 허용된 안정화 1회는 별도 `GRADLE_USER_HOME`과 project cache로 시도했지만, Gradle wrapper가 `C:\\tmp\\issue-110-gradle-home` 아래 lock file 부모 디렉터리를 만들지 못해 exit code 1로 시작 전 실패했습니다.

### Failure Cause

- 이 worktree의 Gradle generated test output 또는 test runtime 상태가 재현 가능하게 손상돼, 정상 `compileTestJava` 뒤에도 Test Executor가 어떤 compiled test class도 찾지 못합니다. 별도 Gradle home도 sandbox 파일 생성 제약으로 사용할 수 없습니다.

### Change Scope

- ranking rebuild production, focused integration test, Issue #110 evidence만 변경했습니다.
- 영구 ledger, DLT, ranking 조회 정책, Kafka offset 및 topic 구조는 변경하지 않았습니다.

### Reverification

- 상세 명령과 결과는 `commands.md`를 참고합니다.

### Next Attempt

- 깨끗한 Gradle test runtime에서 동일 focused Level 4 command를 재실행하고, PASS 뒤 Level 5 runner를 실행해야 합니다.

## Attempt 2

### Generate

- 최신 `origin/main` `2a73922` 위로 feature commit을 conflict 없이 rebase했습니다.

### Evaluate

- single clean focused Level 4 command를 재실행했습니다.
- Gradle Test Executor가 target을 포함한 모든 test class를 `ClassNotFoundException`으로 보고해 test body 실행 전 다시 실패했습니다.

### Failure Cause

- Attempt 1과 동일한 Gradle test runtime class-loading 문제가 재현됐습니다.

### Change Scope

- source 변경 없이 evidence의 최신 attempt와 head만 갱신했습니다.

### Reverification

- `gradlew.bat clean test --tests "*RankingRebuildServiceIntegrationTest.deduplicatesMatchingEventIdsAndExposesReplayMetrics" --tests "*RankingRebuildServiceIntegrationTest.rejectsConflictingPayloadsForTheSameEventId" --no-daemon --max-workers=1 --console=plain`은 `BUILD FAILED in 29s`입니다.
- target test XML은 생성되지 않았습니다.

### Next Attempt

- 실제 class output을 정상적으로 전달하는 Gradle runtime에서 focused Level 4를 재실행해야 합니다.
