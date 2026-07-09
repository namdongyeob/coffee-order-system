# Issue 완료 전 체크리스트

모든 Issue 처리자는 PR을 열기 전에 이 목록을 확인합니다.

## 공통 체크리스트

- [ ] 관련 문서와 대상 Issue 본문을 읽었습니다.
- [ ] Issue 범위 밖 구현이나 리팩터링을 하지 않았습니다.
- [ ] Controller, Service, Repository 책임이 섞이지 않았습니다.
- [ ] 필요한 테스트 또는 검증 명령을 실행했습니다.
- [ ] `docs/testing/verification-log.md`에 검증 결과를 기록했습니다.
- [ ] 새로 발견한 반복 실수나 주의점이 있으면 `docs/ai/agent-mistakes.md`에 기록했습니다.
- [ ] API, DB, 정책, 복구 방식이 바뀌면 관련 문서를 함께 갱신했습니다.
- [ ] PR 본문에 검증 결과와 남은 위험을 적었습니다.

## 기록 기준

`docs/ai/agent-mistakes.md`에는 다음 경우만 기록합니다.

- 같은 실수가 다음 Issue에서도 반복될 가능성이 있습니다.
- 원인과 수정 방식이 확인되었습니다.
- 구현자나 리뷰어가 나중에 읽고 바로 피할 수 있는 내용입니다.

단순한 추측, 개인 감상, 이미 Issue 본문에 충분히 적힌 일반 요구사항은 기록하지 않습니다.

## 3계층 설계 확인

구현 Issue에서는 `docs/architecture/layered-design-policy.md`를 기준으로 다음을 확인합니다.

- Controller에 비즈니스 규칙이 들어가지 않았습니다.
- Service가 HTTP 요청/응답 객체에 직접 의존하지 않습니다.
- Repository가 비즈니스 결정을 하지 않습니다.
- 공통 유틸이나 Manager가 책임을 숨기는 우회 계층이 되지 않았습니다.
