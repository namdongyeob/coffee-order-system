# Issue #102 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/102

Execution mode: SOLO
Execution mode reason: 애플리케이션 동작, build, runtime을 변경하지 않는 문서 전용 작업입니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임을 변경하지 않는 문서 전용 작업입니다.
Level 6 required: NO
Level 6 reason: 실제 API 계약을 변경하지 않는 문서 전용 작업입니다.

## 완료 기준

- [x] README.md가 재작성되었습니다. 설계 목표·의도, 과제 범위(필수/구현 완료/범위 밖), 기술 스택 선택 이유, 패키지 구조, 핵심 정책(포인트·주문·이벤트·Outbox·랭킹), 실제 API 요청/응답 예시, 오류 코드, 로컬 실행, 테스트 현황, 문서 진입점을 포함합니다.
- [x] 실제 코드·문서와 불일치하는 내용이 없습니다. 독립 검증 agent가 tech stack, 패키지 구조, DTO 필드, 오류 코드, 정책 수치(Redisson waitTime/leaseTime, Outbox 폴링 주기), migration 목록, 로컬 실행 포트, 문서 링크 14개를 실제 코드·파일과 대조해 전부 일치함을 확인했습니다. 유일하게 지적된 "테스트 76개" 항목은 검증 agent의 grep 패턴 오류(`@TestConfiguration`/`@Testcontainers`를 `@Test`로 오카운트)였음을 정확한 패턴 재확인(`grep -E "^\s*@Test(\(.*\))?\s*$"` → 76건, 직전 전체 회귀 실행의 JUnit XML 집계 76건과 일치)으로 반증했습니다.
- [x] 구현 범위가 변경되지 않았습니다. README.md 1개 파일만 수정했습니다.
- [x] `docs/testing/evidence-guide.md`의 기본 evidence 파일을 작성했습니다.

## 참고

- 독립 검증: general-purpose agent 1개(fresh, README를 신뢰하지 않고 실제 소스·문서를 직접 읽어 10개 항목 대조). 결과: 9개 항목 정확, 1개 항목("테스트 76개")은 agent 자신의 grep 오탐으로 확인, README는 최종적으로 수정 불필요.
- `Current head`는 이 커밋을 가리킵니다: `224b3232583f315348009a74ccf073ab8ac71e81`.
