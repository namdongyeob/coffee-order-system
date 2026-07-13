# Issue #15 Manual QA

## 계약 확인

- GitHub Issue #15 safety contract는 승인된 topic·partition·offset 한 건만 재처리하고, original topic·partition·offset header가 없으면 fail-closed하도록 요구합니다.
- `DltReplayService`는 original partition이 존재하고 original offset만 없는 record에 `DLT 원본 offset header가 없습니다.`를 발생시켜 재발행 전 종료합니다.
- `processed_event` 사전 조회와 consumer 처리의 경쟁 위험은 result에 기록하고 최종 중복 방어는 기존 consumer 멱등성에 맡깁니다.

## Runtime 확인

- `docker ps`에서 `docker-mysql-1`, `docker-redis-1`, `docker-kafka-1`이 healthy 상태임을 확인했습니다.
- local script는 MySQL·Redis·Kafka에 연결한 뒤 존재하지 않는 `order.completed.DLT` partition 0 offset 0을 10초 내 발견하지 못해 `DltReplayException`으로 종료했습니다.
- exit code `1`은 의도된 fail-closed 결과이며, original topic에 재발행했다는 로그나 결과는 관찰되지 않았습니다.

## 제외 범위 확인

- 공개 HTTP API와 자동 전체 재처리는 추가하지 않았습니다.
- 원본 offset 검증 production 코드는 변경하지 않았고, offset 단독 누락을 재현하는 통합 테스트만 추가했습니다.
