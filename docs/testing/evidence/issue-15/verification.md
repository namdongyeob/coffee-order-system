# 검증 로그

Attempt: 2
Head: a49e0103d938f8f078601afb4502e04a5f7ded73

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-13 | Issue #15 DLT original offset fail-closed | Level 4 | PASS | Kafka DLT record에 original topic·partition은 두고 original offset만 생략한 재발행 차단 | `./gradlew.bat test --tests "*DltReplayServiceIntegrationTest" --no-daemon --max-workers=1` | Testcontainers Kafka·Redis·MySQL에서 tests 4, failures 0, errors 0이었습니다. offset 검증 구현은 이미 존재했고 새 회귀 테스트가 offset 단독 누락 시 `DltReplayException`을 확인합니다. |
| 2026-07-13 | Issue #15 local script fail-closed | Level 5 | PASS | Compose MySQL·Redis·Kafka healthy 상태의 local profile script 진입과 존재하지 않는 DLT topic/offset 차단 | `./scripts/replay_dlt_message.ps1 -Partition 0 -Offset 0 -ApprovedBy operator-a -Reason 'Offset header validation'` | application은 local 인프라 연결 후 지정 offset을 찾지 못해 10초 제한 뒤 `DltReplayException`과 exit code 1로 종료했습니다. 재발행 없는 fail-closed 결과입니다. |

Level 6은 Issue #15에서 required NO입니다. 공개 HTTP API를 추가하거나 변경하지 않았으므로 실행하지 않았습니다.
