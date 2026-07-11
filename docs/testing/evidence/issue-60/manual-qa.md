# Issue #60 Manual QA

정책 문서와 테스트 계약을 직접 대조했습니다.

- 적용 저장소가 `namdongyeob/coffee-order-system`으로 제한되고, 고정 큐가 `#61 -> #45 -> #55 -> #11 -> #21 -> #12 -> #13 -> #14 -> #15 -> #16 -> #51 -> #52 -> #53 -> #54 -> #56 -> #57 -> #58 -> #36` 순서로 명시됐습니다.
- #60 PR은 자동 merge·close하지 않고 사람이 merge해야 하며, 사람 merge 뒤 #61이 시작되고 #45는 #61 완료 뒤에만 시작한다는 bootstrap 경계를 확인했습니다.
- Reviewer는 fresh context·읽기 전용이고 `APPROVED`·`REVISE`·`BLOCKED`를 사용하며, `REVISE`는 원래 Dev에게 한 번만 반환하고 두 번째 `REVISE`는 안전 정지합니다.
- merge 조건은 Dev verification, fresh Review, QA, Docs evidence, 최신 CI/head SHA/mergeability와 우회 금지를 모두 요구합니다.
- #61의 runtime·IntelliJ 구현은 이 PR에 추가되지 않았고 별도 Issue 범위임을 확인했습니다.

애플리케이션 런타임이나 HTTP 계약은 변경하지 않았으므로 Level 5·6 manual QA는 적용하지 않습니다.
