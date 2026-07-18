# Issue #125 Manual QA

Issue: #125
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/125
Date: 2026-07-18

## 읽은 문서와 역할

- Dev Agent 한 명이 production/test의 유일한 writer였습니다. 구현 subagent는 사용하지 않았습니다.
- `AGENTS.md`, coffee-order issue loop, Context Router, orchestration policy, agent rules, test strategy, evidence guide를 읽었습니다.
- Redis ranking hot path의 Redis 설계, ADR-004, 인기 메뉴 정책, 도메인 규칙과 직접 요구된 ADR-008, Issue #119를 확인했습니다.

## Automated verification

- fixed Clock 단일 read, 양수 기간, batch `1..1000`, marker/Kafka/DLT/rebuild window 비교를 단위 테스트로 확인했습니다.
- Attempt 3에서 cleanup disabled도 core retention 안전성을 검증하고, 1초 미만 marker TTL이 Redis Lua의 `EX 0` 인자로 내려가기 전에 거부됨을 확인했습니다.
- Attempt 4에서 1500ms marker가 Redis `EX` 적용 시 1초로 내림되는 실효값을 기준으로 ledger 1500ms보다 짧다고 판정해 기동 전에 거부함을 확인했습니다.
- MySQL 8.4에서 cutoff 1μs 전·정확히 cutoff·이후, 상태별 보존, rebuild 상태별 보존, batch·재실행·동시 실행과 mutation-time predicate 재확인을 확인했습니다.
- Redis 7.4에서 구성한 marker TTL이 실제 key TTL에 반영됨을 확인했습니다.
- 기존 bilateral DLT↔rebuild, normal ledger, Kafka DLT 회귀를 그대로 통과했습니다.

## Manual QA

- 실제 Compose MySQL·Redis·Kafka가 모두 `healthy`인 상태에서 local profile 앱 PID `34888`을 기동했습니다.
- Flyway가 7개 migration을 적용하고 Tomcat 8080과 scheduler가 시작됨을 로그로 확인했습니다.
- old independent `COMMITTED` 3행은 tick별 `2`, `1`, `0`건으로 bounded 삭제됐습니다.
- old `RESERVED` 1행과 `PREPARED` rebuild에 연결된 old `COMMITTED` 1행은 남았습니다.
- TTL 60초 marker를 직접 만든 뒤 여러 scheduler tick 후 value `fingerprint`, TTL 57초로 유지됨을 확인했습니다.
- invalid protection 설정은 `ranking.ledger.cleanup.kafka-retention must be <= ledger-retention`으로 앱 기동을 거부했고 사전 삽입한 적격 행은 `COMMITTED`로 남았습니다.

## Adversarial QA

- cutoff 정확히 같은 행, cutoff 이후 행, `RESERVED`, `REDIS_APPLIED`, 모든 미완료/recovery rebuild 상태를 삭제 대상에서 제외했습니다.
- 두 cleanup thread가 동시에 실행돼도 각 반환값은 batch 2 이하이고 반환 삭제 합계와 DB 잔여 수가 일치했습니다. 잠금 경합 tick의 0건 종료는 다음 주기 재시도로 처리합니다.
- 후보 선택 뒤 상태가 `REDIS_APPLIED`로 바뀐 상황을 가정해 delete predicate가 0건을 반환하고 행을 보존함을 확인했습니다.
- external retention이 더 길면 DB/Redis 변경 전에 실패하고, Redis marker를 조회하거나 일괄 삭제하는 cleanup 의존성이 없음을 확인했습니다.
- Attempt 2에서 외부 protection window 기본값을 모두 제거했습니다. cleanup disabled context는 외부 값 없이 기동하고 scheduler bean이 없으며, enabled context는 첫 누락 `kafka-retention`에서 기동을 거부합니다.
- EXPLAIN은 별도 테스트 전용 query가 아니라 production `CANDIDATE_SQL`에 `explain`만 앞에 붙여 실행했습니다.
- Attempt 3에서 disabled 설정의 외부 window는 선택 상태를 유지하면서 marker TTL 0·ledger보다 짧은 값·sub-second 값은 Policy 생성 시 거부했습니다.
- 실패한 GitHub CI 동시성 테스트를 로컬에서 같은 `kafka-retention` 예외로 재현한 뒤, 외부 window 검증 분리 후 같은 테스트가 예외 없이 PASS함을 확인했습니다.
- fractional-second marker 설정은 production `toSeconds()`와 같은 변환으로 실효 TTL을 만든 뒤 ledger retention과 비교하므로 설정값만 같은 1500ms/1500ms도 안전하다고 오판하지 않습니다.

## Cleanup receipt

- Level 5 앱과 Gradle daemon PID가 모두 종료됐습니다.
- `docker compose -f docker/compose.yaml down -v`로 이 검증이 만든 MySQL·Redis·Kafka container/network/volume만 제거했습니다.
- 전체 회귀 뒤 Gradle/Test Executor Java 프로세스와 `org.testcontainers=true` container는 0개였습니다.
- 다른 프로젝트 container, host MySQL과 사용자 파일은 변경하지 않았습니다.

## 미검증 항목과 남은 위험

- Level 6은 HTTP 계약이 없는 내부 scheduler라 Issue 결정대로 실행하지 않았습니다.
- 운영 Kafka/DLT effective retention은 배포 환경 topic/broker 설정을 운영자가 확인해 config에 입력하는 경로입니다. 실제 운영 cluster 값은 이 로컬 검증에서 확인하지 않았습니다.
- 독립 Review, independent QA와 최신 PR-head CI는 draft PR 뒤 pending입니다.
- evidence의 verified production head는 `d976fe6`이고 evidence-only commit 뒤 PR head와 의도적으로 다릅니다. 두 head의 코드 차이는 없으며 최신 evidence-only PR head 전체 회귀는 CI pending입니다.
