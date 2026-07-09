# 로컬 실행 Runbook

## 명령

```powershell
.\gradlew.bat test
.\gradlew.bat bootRun
```

## 필요한 서비스

- MySQL.
- Redis.
- Kafka.
- Kafka UI.
- RedisInsight.

테스트는 Testcontainers를 우선 사용하고, 로컬 수동 검증은 docker-compose 기반으로 진행합니다.