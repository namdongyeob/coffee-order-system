# Issue #9 Manual QA

- Level 5 앱은 `Started CoffeeOrderSystemApplication in 41.772 seconds`로 기동했습니다.
- `/actuator/health`는 HTTP 200과 status `UP`을 반환했습니다.
- Redis 7.4.2 Testcontainers container `d0d37842e623`에서 `redis-cli ping`은 `PONG`을 반환했습니다.
- Level 6은 외부 HTTP API 변경이 없어 실행하지 않습니다.
