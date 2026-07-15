# Manual QA

- Manual Level 5: local MySQL·Redis·Kafka compose를 healthy로 기동하고 maintenance runner를 환경변수 `SPRING_PROFILES_ACTIVE=local`, `RANKING_REBUILD_MAINTENANCE=true`, `RANKING_REBUILD_ENABLED=true`로 실행했습니다.
- 관찰: runner가 `ranking_rebuild_completed inputRecords=0 uniqueEvents=0 conflicts=0`를 기록했습니다. Kafka CLI는 `ranking-consumer-group` active member가 없다고 반환했습니다.
- Cleanup receipt: runner process tree를 종료하고 `docker compose -f docker\\compose.yaml down -v --remove-orphans`로 Issue가 기동한 compose service를 정리했습니다. 영구 ledger, DLT replay, Redis lookup/tie-breaking, Kafka offset 또는 topic 구조 변경은 없었습니다.
