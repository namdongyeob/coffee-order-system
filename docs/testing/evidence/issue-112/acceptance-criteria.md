# Issue #112 Acceptance Criteria

Issue: #112
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/112

Execution mode: STRICT
Execution mode reason: Kafka replay, Redis 원자 교체, MySQL ledger, 재시도 중복 방지와 장애 복구 경계를 함께 변경합니다.
Level 5 required: YES
Level 5 reason: 실제 Docker Compose의 MySQL·Kafka·Redis와 로컬 애플리케이션을 사용해 최초 rebuild, 동일 재실행, pending 복구를 관찰해야 합니다.
Level 6 required: NO
Level 6 reason: 외부 공개 HTTP 계약이나 부하·성능 목표를 변경하지 않습니다.

## Dev 단계 완료 기준

- [x] 선행 Issue #110과 #111의 main merge를 확인한 기준 브랜치에서 작업했습니다.
- [x] Rebuild가 수집한 고유 이벤트의 core payload fingerprint를 저장합니다.
- [x] 동일 eventId·동일 payload 재실행은 ledger 한 행과 Redis score 1을 유지합니다.
- [x] 동일 eventId·다른 payload는 swap 전에 fail-closed하고 live ranking을 변경하지 않습니다.
- [x] swap 뒤 ledger backfill 중단은 pending run과 rebuild lock을 보존합니다.
- [x] 다음 rebuild는 pending run을 먼저 backfill하고 새 replay·swap 없이 완료합니다.
- [x] Redis Lua가 live swap과 runId marker를 원자 기록해 swap 직후 DB mark 실패를 같은 run으로 복구합니다.
- [x] captured partition end와 이전 normal offset을 영속화하고 partial offset crash를 same-run move·verify로 복구합니다.
- [x] 완전 보상은 run/events/offset plan을 cascade 삭제하고, 불완전 보상은 `RECOVERY_REQUIRED`·events·lock을 보존합니다.
- [x] ledger backfill은 50행 batch마다 lease를 갱신하며 갱신 실패 시 pending을 완료 처리하지 않습니다.
- [x] 미완료 run의 swap marker·backup·원래 live 존재 메타는 TTL 없이 완료 또는 정상 cancel까지 보존합니다.
- [x] rollback Lua는 모든 날짜의 marker·backup·존재 메타를 먼저 검증한 뒤에만 live를 복원합니다.
- [x] marker·backup·존재 메타 소실은 live·offset·plan·event를 보존하고 `RECOVERY_REQUIRED`로 fail-closed 합니다.
- [x] recovery의 완전 보상·run cancel은 lock을 해제해 fresh rebuild를 허용하고, 불완전 보상은 lock을 유지합니다.
- [x] ledger에는 `COMMITTED`, `REBUILD`, rebuild run id가 기록됩니다.
- [x] `DltReplayService`, 정상 ranking consumer production 코드와 공통 applied-event marker는 변경하지 않았습니다.
- [x] RED→GREEN focused 검증, Rebuild 31 tests, 관련 54 tests, 전체 110 tests와 Level 5 검증이 PASS했습니다.
- [x] `git diff --check`, secret·large-file·diff scope 검사를 통과했습니다.

## STRICT 후속 게이트

Dev evidence와 Ready PR은 독립 Review, 독립 QA, 최신 GitHub Actions CI의 입력입니다. Review·QA·CI 판정과 mergeability는 GitHub 정본에서 후속 확인하며, 이 PR은 Issue를 자동 종료하지 않습니다.
