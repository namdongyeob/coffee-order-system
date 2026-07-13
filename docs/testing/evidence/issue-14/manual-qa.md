# Issue #14 Manual QA

## Level 4

- 실제 Kafka 3.9.1, MySQL 8.4.5, Redis 7.4.2 Testcontainers를 사용했습니다.
- 일반 consumer active member, Redis lock, DB mismatch를 fail-closed로 확인했습니다.
- success에서는 lower boundary 포함, upper boundary 제외, live key 원자 교체, 정상 group offset 이동, `processed_event` 0건과 반복 실행 동일 점수를 확인했습니다.

## Level 5

- clean Compose에서 MySQL·Redis·Kafka healthy와 normal local app health 200을 확인했습니다.
- userId 1401 포인트 충전 200, menuId 1 주문 201 뒤 Redis 당일 key의 member `1` score `1`을 확인했습니다.
- 일반 앱을 종료해 active member가 사라진 뒤 live key를 삭제하고 maintenance runner를 실행했습니다. member `1` score `1`이 복구됐고 normal group offset은 captured end `1`, lag `0`이었습니다.
- DB에만 PAID 주문을 추가한 mismatch 실행은 명시적 exception과 process failure로 끝났고 live score `1`, normal offset `1`, temp/backup key 0개를 유지했습니다.
- 성공·실패 모두 일반 `processed_event`를 rebuild가 기록하지 않는 계약은 Level 4에서 확인했습니다.
- 종료 시 project Compose와 volume을 `down -v`로 정리했고 service 0개와 port 8080 free를 확인했습니다.
