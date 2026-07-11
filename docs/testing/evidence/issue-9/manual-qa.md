# Issue #9 Manual QA

## Final independent QA

- Level 4 focused actual Redis는 5 tests, failures 0, errors 0, skipped 0으로 `BUILD SUCCESSFUL in 1m 03s`였습니다.
- Level 1 전체 회귀는 35 tests, failures 0, errors 0, skipped 0으로 `BUILD SUCCESSFUL in 1m 17s`였습니다.
- Level 5는 MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2를 기동했습니다.
- 앱은 `Started CoffeeOrderSystemApplication in 42.982 seconds`로 기동했습니다.
- `/actuator/health`는 HTTP 200과 status `UP`을 반환했습니다.
- Redis `redis-cli ping`은 `PONG`을 반환했습니다.

## Raw Redis probe

- Probe 방식은 애플리케이션 Service 호출이 아니라 Redis container에 대한 direct `redis-cli ZINCRBY`였습니다.
- 실행 순서는 date 30/member 101 두 번, date 30/member 202 한 번, date 31/member 101 한 번이며 각 반환값은 `1`, `2`, `1`, `1`이었습니다.
- `popular:menus:2099-12-30`: member `202` score `1`, member `101` score `2`.
- `popular:menus:2099-12-31`: member `101` score `1`.
- 같은 날짜의 member 분리, 같은 member의 날짜별 key 분리, 같은 key/member의 원자적 누적 결과를 실제 Redis 값으로 확인했습니다.
- 이 probe는 Level 5 Redis runtime의 ZSET과 key isolation만 증명합니다. Service 경로는 Level 4 실제 Redis integration test가 별도로 증명합니다.

## Cleanup receipt

- `KEYS 'popular:menus:2099-12-*'`로 QA key 두 개를 확인한 뒤 삭제했고 `DEL` 결과는 `2`였습니다.
- 두 key 각각의 후속 `EXISTS` 결과는 `0`, `0`이었습니다.
- 기존 `rag-pgvector` 리소스는 건드리지 않았습니다.
- QA 종료 후 QA 소유 애플리케이션과 인프라 리소스가 남지 않았습니다.
- Level 6은 외부 HTTP API 변경이 없어 `NO`입니다.
