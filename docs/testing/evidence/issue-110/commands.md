# Commands

| Command | Purpose | Result |
| --- | --- | --- |
| `gradlew.bat clean test --tests "*RankingRebuildServiceIntegrationTest.deduplicatesMatchingEventIdsAndExposesReplayMetrics" --tests "*RankingRebuildServiceIntegrationTest.rejectsConflictingPayloadsForTheSameEventId" --no-daemon --max-workers=1 --console=plain` | reboot 후 단일 focused Level 4 재검증 | `compileTestJava` 뒤 Test Executor가 `RankingRebuildServiceIntegrationTest`를 포함한 모든 test class에 `ClassNotFoundException`을 기록했고 target XML은 생성되지 않음 |
| `gradlew.bat test --tests "*RankingRebuildServiceIntegrationTest.deduplicatesMatchingEventIdsAndExposesReplayMetrics" --tests "*RankingRebuildServiceIntegrationTest.rejectsConflictingPayloadsForTheSameEventId" --no-daemon --max-workers=1` | TDD RED | `RankingRebuildResult` 지표 accessor 3개가 없어 `compileTestJava FAILED` |
| `gradlew.bat clean compileJava --no-daemon --max-workers=1 --console=plain` | production compile | `BUILD SUCCESSFUL in 26s` |
| `gradlew.bat test --tests "*RankingRebuildServiceIntegrationTest.deduplicatesMatchingEventIdsAndExposesReplayMetrics" --tests "*RankingRebuildServiceIntegrationTest.rejectsConflictingPayloadsForTheSameEventId" --no-daemon --max-workers=1 --console=plain` | focused Level 4 GREEN | BLOCKED. `compileTestJava` 뒤 Test Executor가 모든 test class를 `ClassNotFoundException`으로 보고, problems report 쓰기에서 `FileAlreadyExistsException` 발생 |
| `$env:GRADLE_USER_HOME='C:\tmp\issue-110-gradle-home'; gradlew.bat clean test ... --project-cache-dir C:\tmp\issue-110-project-cache` | 허용된 출력 격리 1회 | exit code 1. Gradle wrapper가 isolated home의 lock file 부모 디렉터리를 만들지 못했고 test XML은 생성되지 않음 |
| `gradlew.bat clean test --tests "*RankingRebuildServiceIntegrationTest.deduplicatesMatchingEventIdsAndExposesReplayMetrics" --tests "*RankingRebuildServiceIntegrationTest.rejectsConflictingPayloadsForTheSameEventId" --no-daemon --max-workers=1 --console=plain` | latest main rebase 후 단일 focused Level 4 재검증 | `BUILD FAILED in 29s`. target을 포함한 모든 test class `ClassNotFoundException`, target XML 없음 |

Level 4와 Level 5는 PASS가 아닙니다.
