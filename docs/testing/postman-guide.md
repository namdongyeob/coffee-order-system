# Postman 검증 가이드

API 구현 후 Postman collection 또는 `.http` 파일을 작성합니다.

## 최소 요청

1. 메뉴 목록 조회.
2. 포인트 충전.
3. 주문 결제.
4. 인기 메뉴 Top 3 조회.
5. 잔액 부족 주문 실패.
6. 없는 메뉴 주문 실패.

## 기록 기준

- 요청 URL.
- 요청 body.
- 응답 status.
- 응답 body.
- 실행 시각.
- 서버 profile.

Postman collection을 만들면 `postman/` 폴더에 저장합니다. IntelliJ `.http` 파일을 만들면 `http/` 폴더에 저장합니다.
