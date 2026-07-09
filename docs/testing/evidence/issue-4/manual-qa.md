# Issue #4 Manual QA

Issue #4는 `GET /api/menus` API 구현입니다. 별도 UI가 없으므로 screenshot은 만들지 않았습니다.

## 확인한 API surface

테스트는 `@WebMvcTest`와 `MockMvc`로 Spring MVC Controller surface를 검증합니다. Issue #4의 범위는 Controller 테스트이므로 Kafka, Redis, MySQL Testcontainers를 띄우지 않습니다.

```text
GET /api/menus
```

기대 응답입니다.

```json
[
  {
    "id": 1,
    "name": "아메리카노",
    "price": 4500
  },
  {
    "id": 2,
    "name": "카페라떼",
    "price": 5000
  },
  {
    "id": 3,
    "name": "카푸치노",
    "price": 5500
  },
  {
    "id": 4,
    "name": "에스프레소",
    "price": 4000
  }
]
```

## 서브에이전트

- Review Agent: 사용함.
- QA Agent: 사용함.
- 목적: 구현 diff와 evidence 누락을 부모 세션 외부 관점에서 확인합니다.

## Subagent 결과 반영

- Review Agent와 QA Agent 모두 초기 검증 evidence가 재현되지 않는다고 지적했습니다.
- 지적에 따라 Testcontainers 기반 full context API 테스트를 Controller slice test로 바꿨습니다.
- `--no-daemon`으로 focused test와 full smoke test를 다시 실행해 PASS를 확인했습니다.
