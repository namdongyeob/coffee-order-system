# Issue #77 Manual QA

## 실제 인프라 관찰

- Testcontainers의 Kafka 3.9.1, MySQL 8.4.5, Redis 7.4.2를 사용하는 기존 실제 Spring Boot 통합 경로를 유지했습니다.
- 수정 전 로그에서 DLT observer subscribe, input producer ack, main listener assignment와 offset reset, retry 시 seek 2회, DLT observer assignment와 offset reset, DLT record poll 순서를 확인했습니다.
- 수정은 main listener assignment를 실제 condition으로 기다린 뒤 input을 보내며 임의 sleep이나 무조건 재시도를 추가하지 않습니다.

## Automated verification

- 격리 DLT 테스트는 새 Gradle 프로세스 3회 모두 PASS했습니다.
- 관련 Kafka/DLT 통합 테스트와 전체 51 tests가 PASS했습니다.
- 실제 DLT record의 key `6101`, partition `0`, eventId와 `menuId`를 포함한 value, `KafkaHeaders.DLT_ORIGINAL_TOPIC=order.completed` assertion을 유지했습니다.

## Manual QA

- production/runtime/API 변경이 없어 별도 local application Level 5와 HTTP Level 6 실행은 하지 않았습니다.
- 실제 Kafka path는 Level 4 Testcontainers 통합 테스트로 관찰했습니다.

## Adversarial QA와 남은 위험

- listener assignment 전에 input을 보내는 기존 race만 제거했고 retry 횟수, 20초 bounded DLT poll과 10초 invocation verification은 늘리지 않았습니다.
- assertion을 삭제하거나 약화하지 않았고 production DLT recoverer를 변경하지 않았습니다.
- 현재 머신의 수정 전 exact 재실행 2회는 PASS였으므로 기존 live Issue RED와 단계별 로그를 함께 원인 근거로 사용합니다.
- fresh Review, independent QA, Docs 최종 동기화와 최신 CI는 아직 pending입니다.

## Cleanup receipt

- 모든 Gradle 실행이 종료된 뒤 현재 worktree 관련 Java/Gradle 프로세스는 0개였습니다.
- `docker ps`에서 이번 테스트의 Kafka, MySQL, Redis와 Ryuk 잔존 컨테이너는 0개였습니다.
- 다른 worktree, Issue #14, PR #76과 Issue #15 파일은 변경하지 않았습니다.
