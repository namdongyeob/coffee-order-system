# Issue Attempt Log

Issue: #58
Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/58
Branch: claude/issue-58-level-gate-enforce

Current disposition: PASS
Current Attempt: 1
Current head: 6193ebda300692aedb3e7d8539436fc23b17daa2

## Attempt 1

### Generate

- 시작 시각은 기록하지 못해 `미측정`입니다.
- `gh issue view 58`로 본문과 합의 코멘트를 확인하고 STRICT/Level 5·6 NO, "ENFORCE만 hard fail, OBSERVE는 선택 측정, DROP 미구현, exemption은 고정 code만" 접근을 그대로 채택했습니다.
- `scripts/harness_gate.py`의 기존 `validate_changed_path_mode`(경로→Mode 강제) 구조를 참고해 경로→Level 강제 로직을 추가했습니다: `LEVEL_PATH_ENFORCE_RULES`(M1 controller→Level2, M2 consumer→Level4, M3 order/event→Level4), `required_path_levels`, `parse_level_exemptions`, `validate_level_exemptions`, `required_path_levels_needing_pass`.
- `validate_issue_evidence`에 선택 인자 `changed_paths_for_level`을 추가해 기존 `required_verification_levels(acceptance)`(Level 5/6 자기신고)와 새 path 기반 필요 Level을 합집합으로 만들어 기존 `validate_verification_log`의 PASS 행 검사에 재사용했습니다.
- `main()`에서 `changed_paths(...)`를 한 번만 계산해 `validate_issue_evidence`와 `validate_changed_path_mode` 양쪽에 재사용하도록 정리했습니다.
- M3는 `**/event/**`가 아니라 `**/order/event/**`로 좁혀 `event/domain/ProcessedEvent.java` 이름 충돌 오탐을 피했습니다. M8은 정규식이 `src/main/java`만 매치하도록 해 별도 예외 코드 없이 구조적으로 구현했습니다.
- `LevelPathEnforcementTest`(17개), `Issue57ReplayFixtureRegressionTest`(5개)를 `scripts/tests/test_harness_gate.py`에 추가했습니다.

### Evaluate

- Level 0 하네스 회귀: `python -m pytest scripts/tests/test_harness_gate.py -q`가 130 passed, 115 subtests passed입니다(기존 107 passed에서 신규 23개 테스트 추가, 회귀 없음).
- fresh Review Agent: **APPROVED**, P0/P1 없음. M1·M2·M3·M8 구현이 설계 문서와 일치함을 재계산해 확인했고, M6이 어디에도 hard fail로 구현되지 않았음을 grep으로 확인했습니다. P2 1건: `docs/testing/level-mapping-design.md`의 exemption 표기 형식(2필드)이 실제 구현(4필드: level+path 추가)과 다르다는 지적.
- fresh QA Agent: **PASS**. P1 1건: Review와 동일한 exemption 형식 불일치(설계 문서 그대로 적으면 `parse_level_exemptions`가 빈 리스트를 반환해 예외가 조용히 무효화될 위험). Review·QA 두 독립 검증이 같은 결함을 다른 등급(P2/P1)으로 각각 지적했습니다.
- Review·QA 결과를 받은 뒤 `docs/testing/level-mapping-design.md`의 exemption 형식 설명을 실제 구현(`Level exemption: <LEVEL> <CODE> — <path> — <PR 번호 또는 커밋 SHA>`)과 일치하도록 수정했습니다(커밋 `6193ebd`). 코드는 변경하지 않았습니다(원래 구현이 맞고 문서가 뒤처져 있었습니다).

### Failure Cause

- RED(경미, 문서만): Review·QA가 공통으로 지적한 exemption 표기 형식의 설계 문서-구현 불일치. `level-mapping-design.md` 갱신으로 해결했습니다.

### Change Scope

- `scripts/harness_gate.py`: M1/M2/M3 ENFORCE 매핑, exemption code 검사, `validate_issue_evidence`/`main()` 통합.
- `scripts/tests/test_harness_gate.py`: `LevelPathEnforcementTest`, `Issue57ReplayFixtureRegressionTest` 추가.
- `docs/testing/level-mapping-design.md`: exemption 표기 형식 설명을 실제 구현과 정합화(1문단).
- `docs/testing/evidence/issue-58/`: 신규 evidence 6개 작성.
- 애플리케이션 코드는 변경하지 않았습니다.

### Reverification

- `python -m pytest scripts/tests/test_harness_gate.py -q`는 head `6193ebd`에서 130 passed, 115 subtests passed입니다.
- `python scripts/harness_gate.py --issue 58 --branch claude/issue-58-level-gate-enforce --base-ref e27bb76 --check-links --include-worktree`는 결과를 `commands.md`에 기록합니다.
- Level 5/6은 NO입니다. 하네스 코드 전용 Issue라 runtime/API 테스트는 실행하지 않았습니다.
- 종료 시각은 기록하지 못해 `미측정`입니다.

### Next Attempt

- 없음. fresh 독립 Review(APPROVED)와 fresh 독립 QA(PASS) 모두 완료했고, 두 역할이 공통으로 지적한 exemption 형식 정합성 결함을 이 Attempt에서 반영했습니다. 남은 것은 GitHub Actions `quality-gates` CI 확인과 사람의 merge 승인입니다.
