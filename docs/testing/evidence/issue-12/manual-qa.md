# Issue #12 Manual QA

## 환경

- 실행일: 2026-07-12.
- server profile: `local`.
- clean environment: 시작 전과 canonical 재실행 전에 `docker compose -f docker/compose.yaml down -v --remove-orphans`.
- 인프라: MySQL, Redis, Kafka healthy.
- 앱: `Started CoffeeOrderSystemApplication in 20.456 seconds`.
- health: HTTP 200, `{"groups":["liveness","readiness"],"status":"UP"}`.

## Level 6 요청·응답 원문

```text
timestamp=2026-07-12T20:16:26.8276337+09:00
GET http://127.0.0.1:8080/api/menus
status=200
response=[{"id":1,"name":"아메리카노","price":4500},{"id":2,"name":"카페라떼","price":5000},{"id":3,"name":"카푸치노","price":5500},{"id":4,"name":"에스프레소","price":4000}]

timestamp=2026-07-12T20:16:27.0608737+09:00
POST http://127.0.0.1:8080/api/points/charge
body={"userId":1201,"amount":10000}
status=200
response={"userId":1201,"balance":10000}

timestamp=2026-07-12T20:16:27.3802161+09:00
POST http://127.0.0.1:8080/api/orders
body={"userId":1201,"menuId":1}
status=201
response={"orderId":1,"userId":1201,"menuId":1,"menuName":"아메리카노","paidAmount":4500,"status":"PAID","orderedAt":"2026-07-12T20:16:27.4742857"}

timestamp=2026-07-12T20:16:28.4126157+09:00
GET http://127.0.0.1:8080/api/menus/popular
status=200
response=[{"rank":1,"menuId":1,"menuName":"아메리카노","orderCount":1}]
poll=500ms 간격 두 번째 시도

timestamp=2026-07-12T20:16:28.4159619+09:00
POST http://127.0.0.1:8080/api/points/charge
body={"userId":1202,"amount":1000}
status=200
response={"userId":1202,"balance":1000}

timestamp=2026-07-12T20:16:28.4662277+09:00
POST http://127.0.0.1:8080/api/orders
body={"userId":1202,"menuId":1}
status=409
response={"code":"INSUFFICIENT_POINT","message":"포인트 잔액이 부족합니다."}

timestamp=2026-07-12T20:16:28.5155236+09:00
POST http://127.0.0.1:8080/api/points/charge
body={"userId":1203,"amount":10000}
status=200
response={"userId":1203,"balance":10000}

timestamp=2026-07-12T20:16:28.5715218+09:00
POST http://127.0.0.1:8080/api/orders
body={"userId":1203,"menuId":999999}
status=404
response={"code":"MENU_NOT_FOUND","message":"메뉴를 찾을 수 없습니다."}
```

## Adversarial QA

- 잔액 1000인 사용자의 4500원 메뉴 주문은 주문을 만들지 않고 409 공통 에러를 반환했습니다.
- 존재하지 않는 menuId는 포인트가 충분해도 주문을 만들지 않고 404 공통 에러를 반환했습니다.
- malformed body가 된 최초 shell 실행은 400 validation 응답이었으며 canonical API 결과로 사용하지 않았습니다.

## Independent QA 재현

- 동일한 clean Compose와 local profile에서 canonical 요청 순서를 재실행했습니다.
- health 원문 상태는 HTTP 200/`UP`, 메뉴 200, 충전 200, 주문 201이었습니다.
- 인기 메뉴는 첫 poll에서 빈 목록이었고 500ms 뒤 두 번째 poll에서 menu 1과 orderCount 1을 확인했습니다.
- 잔액 부족은 409 `INSUFFICIENT_POINT`, 없는 메뉴는 404 `MENU_NOT_FOUND`였고 위 canonical 요청·응답 기록과 일치했습니다.
- 첫 QA 실행은 assertions가 통과했지만 캡처 스크립트가 원문을 출력하지 않아 evidence로 채택하지 않았습니다. clean 재실행 1회에서 원문을 확보했으며 제품 결함은 0건입니다.
- 재실행 뒤 앱, 프로젝트 Compose·volume, 임시 파일과 port 8080을 정리했고 기존 `rag-pgvector`는 유지했습니다.

## Cleanup receipt

- 완료 시각: 2026-07-12T20:16:51.0115553+09:00.
- 프로젝트 Compose `ps`: 비어 있음.
- port 8080 listener: 없음.
- 남은 컨테이너: 기존 `rag-pgvector`만 존재.
- 저장소 밖 임시 JSON request body: 제거.

## 미검증 항목과 위험

- Postman collection import는 검증하지 않았고 `.http` 산출물만 제공합니다.
- 인기 메뉴 반영은 비동기이므로 `.http` 사용자는 주문 뒤 결과가 나타날 때까지 인기 메뉴 요청을 다시 실행해야 합니다.
