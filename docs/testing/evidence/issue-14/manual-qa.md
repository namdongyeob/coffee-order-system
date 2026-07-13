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

## Review remediation Level 4

- Kafka topic을 2 partition으로 구성해 한 partition만 target offset으로 이동한 뒤 timeout을 주입했습니다.
- service는 pre-swap snapshot으로 모든 partition offset을 복원하고 broker 재조회로 일치를 확인한 뒤 live Redis backup을 복원했습니다.
- 보상 재조회 실패를 주입한 경로는 성공이나 완전 rollback을 주장하지 않고 운영자 확인이 필요한 불확실 상태로 종료했습니다.
- lock lease renewal, token ownership, 두 번째 runner 차단과 위험 변경 전 lock 상실 중단을 확인했습니다.
- 변경 후 Level 5를 다시 실행해 local health 200, charge 200, order 201, rebuild score `1`, normal offset `1`·lag `0`, temp/backup 0개를 확인했습니다. 종료 뒤 project services 0개와 port 8080 free였습니다.

## Independent QA 재검증

- Testcontainers Kafka·MySQL·Redis에서 focused Level 4 통합 7건과 단위 3건, 총 10건을 1분 41초에 통과했습니다.
- 2-partition partial offset update와 timeout 보상·broker 재조회, Redis rollback, 보상 실패의 불확실 fail-closed를 다시 확인했습니다.
- Redis token lease 갱신·상실, 소유자 release, 두 번째 runner 차단과 비자정 snapshot 8개 날짜 경계를 다시 확인했습니다.
- clean Compose Level 5에서 health 200, charge 200, order 201 뒤 live score `1`, normal offset `1`, log-end `1`, lag `0`을 확인했습니다.
- Maintenance success는 삭제한 live key의 score `1` 복구와 temp/backup 0개를, DB mismatch는 non-zero 종료와 live score `1`, offset `1`, lag `0`, temp/backup 0개 보존을 확인했습니다.
- QA가 시작한 앱과 project Compose·volume·network를 정리했고 port 8080은 free였습니다. 기존 `rag-pgvector`는 건드리지 않았습니다.
