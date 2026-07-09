# ADR-003 Kafka와 RabbitMQ와 DB 비교

## 상태

Accepted.

## 결정

주문 완료 이벤트 전송에는 Kafka를 사용합니다.

## 이유

과제에는 주문 내역을 실시간으로 데이터 수집 플랫폼에 전송하는 요구가 있습니다. 또한 강의에서 Kafka, Consumer Group, replay 개념, DLT를 학습했으므로 이번 과제의 설명과 검증 대상에 적합합니다.