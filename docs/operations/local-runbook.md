# 로컬 실행 Runbook

## 사전 조건

- Docker Desktop과 Docker Compose v2 이상.
- JDK 17 이상. Gradle toolchain은 Java 17을 사용합니다.
- 프로젝트 root에서 실행합니다. 기존 MySQL 3306과 `rag-pgvector`는 중단하거나 삭제하지 않습니다. 이 프로젝트는 MySQL 13306을 사용합니다.

## 인프라와 관찰 도구 기동

재현 가능한 관찰을 위해 먼저 이 프로젝트 Compose의 volume만 정리합니다. 이 명령은 기존 MySQL 3306과 `rag-pgvector`를 건드리지 않습니다.

```text
docker compose -f docker/compose.yaml --profile tools down -v
docker compose -f docker/compose.yaml --profile tools up -d
docker compose -f docker/compose.yaml ps
```

PowerShell에서는 필요하면 환경변수 예시를 현재 세션에 설정합니다. `.env.example`은 비밀값 없는 local 기본값이며 `.env`는 commit하지 않습니다.

```powershell
Get-Content .env.example | ForEach-Object { if ($_ -match '^([^#=]+)=(.*)$') { Set-Item -Path "Env:$($matches[1])" -Value $matches[2] } }
docker compose -f docker/compose.yaml --profile tools down -v
docker compose -f docker/compose.yaml --profile tools up -d
docker compose -f docker/compose.yaml ps
```

MySQL `13306`, Redis `16379`, Kafka `19092`의 health가 `healthy`인지 확인합니다. Kafka UI는 http://localhost:18080, RedisInsight는 http://localhost:15540 입니다.

Kafka UI에서는 `coffee-order-local` cluster와 `order.completed` topic을 확인합니다. RedisInsight는 Compose 컨테이너에서 실행되므로 연결을 추가할 때 Host는 `redis`, Port는 `6379`로 입력합니다. Windows host port를 경유해야 하는 경우에만 보조 선택지로 Host `host.docker.internal`, Port `16379`을 사용합니다.

## 애플리케이션 기동

Gradle은 local profile로 실행합니다.

```powershell
$env:SPRING_PROFILES_ACTIVE = 'local'
.\gradlew.bat bootRun
```

IntelliJ Run Configuration에서 `CoffeeOrderSystemApplication`을 선택하고 Active profiles에 `local`을 입력합니다. Environment variables에는 `.env.example`의 `LOCAL_*` 값을 같은 이름으로 설정하거나 위 PowerShell 세션에서 IntelliJ를 실행합니다. 기본 profile은 datasource 정보를 제공하지 않으므로 local profile 없이 실행하지 않습니다.

## 실제 확인과 시각 evidence

별도 PowerShell에서 다음을 실행합니다.

```powershell
curl.exe -sS -i http://localhost:8080/actuator/health
curl.exe -sS -i http://localhost:8080/api/menus
```

health는 HTTP 200과 `"status":"UP"`을 반환해야 하며, 메뉴 API는 HTTP 200과 migration seed 메뉴를 반환해야 합니다. Flyway는 4개 migration을 프로젝트 MySQL에 적용합니다.

그 다음 `http/issue-61-local-runtime.http`를 실행해 user `6101`의 포인트를 충전하고 주문을 생성합니다. 이 순서를 건너뛰면 `order.completed` 이벤트와 `popular:menus:<date>` ZSET key가 생성되지 않습니다.

1. IntelliJ Run console에서 `local` profile, Flyway, Redis, Kafka, `Started CoffeeOrderSystemApplication`을 확인하고 `docs/testing/evidence/issue-61/screenshots/intellij-local-startup.png`으로 저장합니다.
2. Kafka UI에서 `coffee-order-local` cluster의 `order.completed` topic을 확인하고 `docs/testing/evidence/issue-61/screenshots/kafka-ui-order-completed-topic.png`으로 저장합니다.
3. RedisInsight에서 Host `redis`, Port `6379` 연결의 `popular:menus:<date>` ZSET과 member, score를 확인하고 `docs/testing/evidence/issue-61/screenshots/redisinsight-popular-menu-key.png`으로 저장합니다.

## 종료와 정리

애플리케이션을 Ctrl+C로 종료한 뒤, 프로젝트 컨테이너와 포트를 정리합니다.

```text
docker compose -f docker/compose.yaml --profile tools down -v
docker compose -f docker/compose.yaml ps
```

다른 프로젝트의 container, 기존 3306 MySQL, `rag-pgvector`는 중단하거나 삭제하지 않습니다.
