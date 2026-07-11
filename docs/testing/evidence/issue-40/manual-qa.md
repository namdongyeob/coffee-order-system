# Issue #40 Manual QA

## 독립 QA 결과

- Focused unit 4건이 19초에 PASS했습니다.
- Level 3 실제 MySQL 3건이 1분 08초에 PASS했습니다. 새 이벤트 처리, 정상 완료 뒤 같은 eventId의 순차 duplicate 무시, 다른 eventId의 독립 처리, Redis 실패 시 DB rollback을 검증했습니다.
- Level 4 실제 Kafka/MySQL/Redis 1건이 1분 02초에 PASS했습니다. 원본, duplicate, 같은 key의 sentinel을 순서대로 발행한 뒤 assertion에서 DB에 원본과 sentinel eventId만 2건 존재하고 Redis score가 `2.0`임을 확인했습니다. 이 결과는 정상 완료 뒤 순차 duplicate가 row와 score를 추가하지 않음을 증명합니다.
- Level 4의 eventId 값은 테스트 assertion 수준에서 확인했습니다. raw DB/Kafka CLI로 eventId 값을 별도 수집하지 않았습니다.
- Fresh Level 1은 43 tests, 0 failures, 0 errors, 0 skipped이며 1분 46초에 PASS했습니다.
- Level 5에서 MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2와 애플리케이션을 기동했습니다. 애플리케이션은 40.173초에 시작했고 Consumer group partition assigned 로그와 health HTTP 200/`UP`을 확인했습니다.
- Level 6 traffic은 보내지 않았습니다. 따라서 Level 5 runtime DB와 Redis ZSET이 비어 있는 상태는 예상된 관찰 결과이며 Consumer 처리 evidence로 사용하지 않습니다.
- retry/error handler/DLT 설정은 없었습니다.
- 검증 리소스 정리 뒤 기존 `pgvector`만 남았습니다.

## Review와 보장 경계

- 초기 Review는 P1과 P2 두 건을 반환했습니다. sentinel 기반 결정적 duplicate evidence를 보강하고 동시 호출 보장 범위를 명시한 뒤 내부 Review가 최종 PASS했으며 추가 finding은 없습니다.
- 현재 구현의 `existsByEventId`와 `saveAndFlush` 사이에 direct concurrent same-event 호출이 경쟁하면 DB unique 제약이 중복 Redis 반영은 막지만 race loser는 unique 위반으로 실패할 수 있습니다. 동시 호출의 정상 반환은 보장하지 않습니다.
- 현재 evidence는 같은 Kafka key/partition에서 정상 완료 뒤 순차 재전달되는 duplicate만 보장합니다.
- Redis는 DB transaction에 참여하지 않습니다. Redis 성공 후 DB commit 전 process crash가 발생하면 DB 이력이 남지 않아 재전달 시 score가 다시 증가할 수 있습니다.
- 현재 기본 Kafka error handler는 처리 예외의 재시도가 소진되면 실패 record를 skip하고 offset을 commit할 수 있습니다. Redis 장기 장애가 계속되면 `processed_event`는 rollback되더라도 해당 이벤트의 랭킹 반영 기회가 유실될 수 있습니다.
- 이 장기 장애 복구는 Issue [#11](https://github.com/namdongyeob/coffee-order-system/issues/11)의 retry/DLT와 Issue [#14](https://github.com/namdongyeob/coffee-order-system/issues/14)의 replay/rebuild 경계이며 Issue #40은 구현하지 않습니다.
- 따라서 exactly-once, crash consistency, 모든 concurrent direct call의 성공을 주장하지 않습니다.

## 미검증과 외부 게이트

- Level 6은 공개 HTTP API 변경이 없어 요구하지 않았고 실행하지 않았습니다.
- retry, error handler, DLT, replay, rebuild는 Issue #40 범위 밖입니다.
- GitHub Actions CI와 사람의 최종 승인 여부는 이 로컬 evidence에서 확인하지 않았습니다.
