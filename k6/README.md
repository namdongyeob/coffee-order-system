# k6 주문 API 시나리오

## 사전 조건

- k6 v2.0.0 이상, Docker Desktop, JDK 17 이상이 필요합니다.
- 프로젝트 Compose를 `down -v` 후 다시 기동한 clean local 환경에서 `local` profile 애플리케이션을 실행합니다.
- 스크립트의 `setup()`은 synthetic user ID만 사용해 VU별 포인트를 1,000,000P 충전합니다. 실제 사용자 ID나 개인정보를 입력하지 않습니다.
- k6는 성능 관찰 도구이며 포인트·주문 정합성을 증명하지 않습니다.

## 안전한 기본 실행

기본 `safe` profile은 로컬 PC 보호를 위해 Load 2 VU, Stress 6 VU, Spike 8 VU로 제한됩니다.

```powershell
k6 inspect k6/order-load.js
k6 inspect k6/order-stress.js
k6 inspect k6/order-spike.js
k6 run --summary-export load-summary.json k6/order-load.js
k6 run --summary-export stress-summary.json k6/order-stress.js
k6 run --summary-export spike-summary.json k6/order-spike.js
```

각 시나리오는 주문 HTTP 201과 JSON 응답을 성공으로 분류합니다. 그 밖의 응답은 `order_error_rate`에 기록하며 `checks`, `http_req_failed`, p95, 주문 성공·오류율 threshold를 함께 판정합니다. 요청에는 `test_type`, `scenario`, `profile`, `api`, `data_class=synthetic` tag가 붙습니다.

## 명시적 heavy 실행

`heavy`는 더 큰 부하를 의도적으로 선택할 때만 사용합니다. 공유 환경이나 운영 환경을 대상으로 실행하지 않습니다.

```powershell
$env:K6_PROFILE = 'heavy'
k6 run k6/order-load.js
Remove-Item Env:K6_PROFILE
```

`BASE_URL`, `K6_USER_BASE`, `K6_MENU_ID`, `K6_CHARGE_AMOUNT`, `K6_THINK_TIME_SECONDS`를 환경변수로 바꿀 수 있습니다. 기본 synthetic user base는 `130000`, 메뉴는 seed menu ID `1`입니다. heavy profile은 긴 실행 중 포인트 소진이 성능 오류로 오인되지 않도록 같은 synthetic 사용자에게 4회 충전합니다. 알 수 없는 `K6_PROFILE`은 실행 전에 실패합니다.

## 정리

실행 후 앱을 종료하고 `docker compose -f docker/compose.yaml down -v`로 이 프로젝트 컨테이너와 volume만 정리합니다. 기존 MySQL 3306과 다른 프로젝트 컨테이너는 건드리지 않습니다.
