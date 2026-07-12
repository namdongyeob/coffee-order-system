# Issue #11 Commands

## 실행 결과

- focused Level 4: `./gradlew.bat test --tests "*RankingEventConsumerDltIntegrationTest" --no-daemon` → 1 test PASS, `BUILD SUCCESSFUL in 1m 22s`.
- 관련 Kafka 회귀: `./gradlew.bat test --tests "*RankingEventConsumerDltIntegrationTest" --tests "*RankingEventConsumerKafkaRedisIntegrationTest" --tests "*OrderEventKafkaIntegrationTest" --no-daemon` → PASS, `BUILD SUCCESSFUL in 1m 46s`.
- Dev 전체 회귀: `./gradlew.bat test --no-daemon` → 48 tests, failures 0, errors 0, `BUILD SUCCESSFUL in 2m 40s`.
- Level 5: `docker compose -f docker/compose.yaml up -d`, `./gradlew.bat bootRun --args="--spring.profiles.active=local" --no-daemon`, Redis stop, type header를 포함한 Kafka CLI producer/consumer → local profile start, health 200, retry 2회와 실제 `.DLT` 원문 PASS.
- cleanup: 앱 종료 뒤 `docker compose -f docker/compose.yaml --profile tools down -v`; `docker compose ... ps` 빈 목록, `docker ps`에는 기존 `rag-pgvector`만 남음을 확인했습니다.
- repository gate와 정적 검사는 evidence 동기화 뒤 실행합니다.

첫 수동 Kafka CLI 입력은 `__TypeId__` header가 없어 listener 전에 역직렬화가 실패했으며 DLT 성공으로 계산하지 않았습니다. clean `down -v` 뒤 header를 포함한 입력으로 다시 관찰했습니다.
