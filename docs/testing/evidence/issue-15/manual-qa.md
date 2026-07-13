# Issue #15 Manual QA

## 계약 확인

- GitHub Issue #15와 `docs/architecture/recovery-strategy.md`는 운영자 승인 메시지의 선택 재발행을 요구합니다.
- `docs/operations/kafka-redis-runbook.md`는 DLT의 원본 topic, partition, offset, exception header를 확인하라고 하지만 재발행 script가 그 header를 검증하거나 어떻게 전달할지 규정하지 않습니다.
- 현재 consumer의 `RankingEventProcessor`는 `processed_event` 존재 시 반환해 중복 소비를 막습니다. 그러나 script의 사전 조회와 consumer 처리 사이의 경쟁을 성공, skip 또는 error 중 무엇으로 보고할지 정본에 없습니다.

## Runtime 확인

- Docker CLI와 Compose CLI는 설치되어 있습니다.
- Docker Desktop Linux daemon이 실행되지 않아 실제 Kafka, Redis, MySQL 컨테이너를 시작하거나 기존 컨테이너 상태를 관찰하지 못했습니다.
- 따라서 Level 4와 Level 5는 미검증이며 PASS로 주장하지 않습니다.

## 안전 정지

- 운영자 승인 방식, 선택 단위, header 처리, processed_event 경쟁 결과를 추측해 재발행 script를 만들지 않았습니다.
- 공개 API와 자동 전체 재처리는 만들지 않았습니다.
