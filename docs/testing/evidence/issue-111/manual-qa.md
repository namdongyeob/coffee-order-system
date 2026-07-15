# Manual QA

- ADR-008이 existing `processed_event`를 공용 ledger로 재사용하지 않는 이유와 별도 ledger의 key·state·retention을 기록했는지 확인했습니다.
- `DLT → rebuild`, `rebuild → DLT`, 동시 요청, swap 뒤 ledger backfill crash를 상태 전이 표에서 확인했습니다.
- 변경 파일은 ADR과 Issue evidence뿐이며 Java production/test, migration, Kafka·Redis·DLT runtime 설정과 event payload 변경이 없음을 확인했습니다.
- Level 5/6은 Issue 범위상 NO이며 실행하지 않았습니다.
