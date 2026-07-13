# Issue #88 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/88

Execution mode: STANDARD
Execution mode reason: 애플리케이션 동작과 workflow 정책을 변경하지 않고 미사용 문서를 삭제하고 archive 문서의 discoverability만 보강하는 문서 작업입니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임을 변경하지 않습니다.
Level 6 required: NO
Level 6 reason: HTTP API 계약과 요청 흐름을 변경하지 않습니다.

## 완료 기준

- [x] 삭제 후보 2건의 `git log` 활용 이력 확인 결과가 기록됩니다. `commands.md`에 `git log --oneline --follow` 결과를 기록했습니다. `questions-for-tutor.md`는 최초 커밋(`56d4196`) 이후 재수정 없음, `verification-matrix.md`는 최초 커밋과 `e2ec4cb`(verification-log 경로 변경에 따른 문자열 치환 1줄)만 있고 실제 내용 갱신은 없었습니다.
- [x] 삭제 또는 보관 위치 이동 결정과 근거가 기록됩니다. 두 문서 모두 삭제로 결정했습니다(이동 대신 삭제를 선택한 근거는 `attempt-log.md` 참고 — git 이력 자체가 필요 시 복구 가능한 보존 수단이며, 두 문서 모두 현재 정본과 실질 중복이거나 이미 해결된 메모라 별도 보관 위치가 실익이 없다고 판단).
- [x] archive 문서 2건에 discoverability 링크 또는 archive 표기가 추가됩니다. `k6-results.md`는 `test-strategy.md`의 "k6 우선순위" 절에서 링크했고, `orchestration-skill/baseline.md`는 상단에 "Status: Archive" 배지를 추가했습니다.
- [x] 애플리케이션 코드와 런타임 설정은 변경하지 않습니다. 이 PR은 `docs/` 하위 5개 파일만 변경했습니다.

검증 실행 head는 `d68f45c`입니다. fresh 독립 Combined Verifier는 head `215a1e3`(base~head 전체 diff 기준)에서 `APPROVED`입니다. 사용자 지시에 따라 merge는 하지 않습니다.
