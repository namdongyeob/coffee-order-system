# Issue #119 Manual QA

Issue: #119
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/119
Date: 2026-07-16

## 실제 환경

- `docker/compose.yaml`로 MySQL 8.4.5, Kafka 3.9.1, Redis 7.4.2를 초기 volume부터 기동했고 세 서비스가 모두 healthy였습니다.
- local 앱은 Flyway V1~V6 적용, HTTP 8080 기동, `ranking-consumer-group`의 `order.completed-0` 할당까지 완료했습니다.

## Normal consumer

- 사용자 11901에 10000 point를 충전하고 menu 1 주문을 생성했습니다.
- event `4e39dd51-2b7c-4ac3-a3b7-a6992287fc65`은 ledger `COMMITTED/NORMAL_CONSUMER`, processed_event 1행이 됐습니다.
- `popular:menus:2026-07-16`의 menu 1 score는 1이고 applied-event marker는 ledger와 같은 fingerprint였습니다.

## DLT → Rebuild

- consumer를 중지하고 PAID order 2와 DLT-only event `11900000-0000-4000-8000-000000000002`를 만들었습니다.
- DLT runner는 `REPUBLISHED`했고 원본 offset 1에는 `ranking-replay-source=DLT_REPLAY` header가 존재했습니다.
- runner 직후 ledger는 `RESERVED/DLT_REPLAY`였고, maintenance Rebuild는 `inputRecords=2 uniqueEvents=2 conflicts=0`으로 완료했습니다.
- 최종 ledger는 `COMMITTED/DLT_REPLAY`, rebuild run은 `COMPLETED`, menu 1과 menu 2 score는 각각 1이었습니다.

## Rebuild → DLT

- 이미 Rebuild에 포함된 첫 event를 DLT offset 1에서 다시 replay하고 normal consumer를 시작했습니다.
- consumer group은 current/end/lag `3/3/0`이 됐지만 ledger 출처는 `COMMITTED/NORMAL_CONSUMER`, processed_event는 1행으로 유지됐습니다.
- menu 1과 menu 2 score는 각각 1로 유지되어 반대 방향 중복 집계도 없었습니다.

## Pending fail-closed와 cleanup

- 실제 DB에 `PREPARED` rebuild run을 삽입한 동안 DLT runner는 `DltReplayRetryableException`으로 종료했습니다.
- 이 기대 실패 뒤 원본 topic end offset 3, ledger 2행, menu score 1/1은 변하지 않았고 shared lock key도 없었습니다.
- pending fixture를 제거해 pending run 0을 확인하고 `docker compose down -v --remove-orphans`로 환경을 정리했습니다.

## 남은 게이트

- 독립 Review, 독립 QA, 최신 GitHub Actions CI는 Ready PR head에서 수행합니다.
