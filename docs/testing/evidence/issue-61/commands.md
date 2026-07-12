# Issue #61 Commands

- `./gradlew.bat test --tests com.example.coffeeordersystem.LocalRuntimeConfigurationTest --no-daemon` passed after the RED configuration test was satisfied.
- `docker compose -f docker/compose.yaml config` validated the Compose model.
- `docker compose -f docker/compose.yaml --profile tools up -d` started MySQL 8.4.5, Redis 7.4.2, Kafka 3.9.1, Kafka UI v0.7.2, and RedisInsight 2.70.1.
- `curl.exe -sS -i http://localhost:8080/actuator/health` returned HTTP 200 and `{"groups":["liveness","readiness"],"status":"UP"}`.
- `curl.exe -sS -i http://localhost:8080/api/menus` returned HTTP 200 and the four Flyway seed menus.
- `docker exec docker-mysql-1 mysql -ucoffee -pcoffee -D coffee_order -e 'select count(*) as flyway_history from flyway_schema_history;'` returned `4`.
- `docker exec docker-redis-1 redis-cli ping` returned `PONG`.
- `docker exec docker-kafka-1 /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list` returned `order.completed` and `__consumer_offsets`.
- `Invoke-WebRequest http://localhost:18080` and `Invoke-WebRequest http://localhost:15540` both returned HTTP 200.

The Dev focused test and runtime commands are not independent QA results.
