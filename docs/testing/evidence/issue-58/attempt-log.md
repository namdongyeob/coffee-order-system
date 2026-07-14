# Issue Attempt Log

Issue: #58
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/58
Branch: claude/issue-58-level-gate-enforce

Current disposition: BLOCKED
Current Attempt: 1
Current head: 미확정(Review·QA 전 draft)

## Attempt 1

### Generate

- 시작 시각은 기록하지 못해 `미측정`입니다.
- `gh issue view 58`로 본문과 합의 코멘트를 확인하고 STRICT/Level 5·6 NO, "ENFORCE만 hard fail, OBSERVE는 선택 측정, DROP 미구현, exemption은 고정 code만" 접근을 그대로 채택했습니다.
- `scripts/harness_gate.py`의 기존 `validate_changed_path_mode`(경로→Mode 강제) 구조를 참고해 경로→Level 강제 로직을 추가했습니다: `LEVEL_PATH_ENFORCE_RULES`(M1 controller→Level2, M2 consumer→Level4, M3 order/event→Level4), `required_path_levels`, `parse_level_exemptions`, `validate_level_exemptions`, `required_path_levels_needing_pass`.
- `validate_issue_evidence`에 선택 인자 `changed_paths_for_level`을 추가해 기존 `required_verification_levels(acceptance)`(Level 5/6 자기신고)와 새 path 기반 필요 Level을 합집합으로 만들어 기존 `validate_verification_log`의 PASS 행 검사에 재사용했습니다(새 에러 메시지 경로를 따로 만들지 않고 기존 인프라를 재사용).
- `main()`에서 `changed_paths(...)`를 한 번만 계산해 `validate_issue_evidence`와 `validate_changed_path_mode` 양쪽에 재사용하도록 정리했습니다(중복 호출 제거).
- M3는 `**/event/**`가 아니라 `**/order/event/**`로 좁혀 `event/domain/ProcessedEvent.java` 이름 충돌 오탐을 피했습니다. M8은 정규식이 `src/main/java`만 매치하도록 해 별도 예외 코드 없이 구조적으로 구현했습니다.
- `LevelPathEnforcementTest`(17개), `Issue57ReplayFixtureRegressionTest`(5개)를 `scripts/tests/test_harness_gate.py`에 추가했습니다.

### Evaluate

- Level 0 하네스 회귀: `python -m pytest scripts/tests/test_harness_gate.py -q`가 130 passed, 115 subtests passed입니다(기존 107 passed에서 신규 23개 테스트 추가, 회귀 없음).
- fresh Review Agent와 fresh QA Agent 결과는 아직 반영 전입니다. `Current disposition`은 두 결과가 확정되기 전까지 `BLOCKED`로 유지합니다.

### Failure Cause

- 없음(초안 단계). 독립 Review·QA 완료 전이라 PASS로 표시하지 않습니다.

### Change Scope

- `scripts/harness_gate.py`: M1/M2/M3 ENFORCE 매핑, exemption code 검사, `validate_issue_evidence`/`main()` 통합.
- `scripts/tests/test_harness_gate.py`: `LevelPathEnforcementTest`, `Issue57ReplayFixtureRegressionTest` 추가.
- `docs/testing/evidence/issue-58/`: 신규 evidence 6개 작성.
- 애플리케이션 코드, `docs/` 정본 문서는 변경하지 않았습니다.

### Reverification

- 독립 Review·QA 완료 뒤 이 절과 disposition·head를 갱신합니다.

### Next Attempt

- fresh Review Agent, fresh QA Agent dispatch 대기 중입니다.
