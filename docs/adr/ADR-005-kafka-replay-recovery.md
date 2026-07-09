# ADR-005 Kafka Replay 복구

## 상태

Challenge candidate.

## 결정

Redis 랭킹 유실 복구는 Kafka replay 전략으로 문서화합니다. MVP 이후 `ranking-rebuild-group` 기반 rebuild runner를 도전 구현 후보로 둡니다.

## 이유

이 방식은 강의의 Kafka 로그, Consumer Group, Offset, Redis ZSET 개념을 과제 상황에 맞게 응용합니다.