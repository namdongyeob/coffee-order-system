# Manual QA

- 이 Issue는 `scripts/harness_gate.py`(게이트 로직)와 `scripts/tests/test_harness_gate.py`(테스트)만 변경합니다. 애플리케이션 코드, docs/ 정본은 변경하지 않았습니다.
- Level 5/6은 Issue 본문 결정대로 NO이며 runtime 또는 HTTP 검증을 완료로 표현하지 않습니다.
- Issue #57 replay(#7·#8·#9·#40·#10)에서 확정된 ENFORCE 매핑만 구현했습니다: M1(`src/main/java/**/controller/**.java`→Level 2), M2(`src/main/java/**/consumer/**.java`→Level 4), M3(`src/main/java/**/order/event/**.java`→Level 4). M4·M5·M7(OBSERVE)과 M6(`service/**`, 저장소 소유자가 OBSERVE 유지로 결정)은 hard fail로 구현하지 않았습니다.
- M3는 `**/event/**`가 아니라 `**/order/event/**`로 정확히 좁혀서 `event/domain/ProcessedEvent.java`(Kafka와 무관한 JPA entity) 같은 이름 충돌 오탐을 피합니다. `test_top_level_event_package_name_collision_does_not_match`로 회귀 고정했습니다.
- M8(테스트 전용 경로 제외)은 별도 예외 처리가 아니라 정규식이 `src/main/java`만 매치하도록 설계해 구조적으로 구현했습니다. `test_test_only_paths_do_not_match_m8`로 확인했습니다.
- exemption code는 `docs/testing/level-mapping-design.md`가 정의한 고정 5개(`TEST_ONLY_IN_MATCHED_DIR`, `NO_BEHAVIOR_CHANGE`, `DOC_STRING_ONLY`, `SUPERSEDED_BY_HIGHER_LEVEL`, `HARNESS_SELF_CHANGE`) 밖의 코드는 `validate_level_exemptions`가 거부합니다. 자유 산문 사유(예: "로깅 문구만 바뀌어서...")는 고정 정규식이 파싱하지 못해 애초에 인식되지 않습니다(`test_free_prose_exemption_reason_is_not_parsed`).
- 부분 예외(여러 파일이 같은 Level에 매치했는데 일부만 예외 처리된 경우)는 여전히 해당 Level을 요구합니다(fail-closed). `test_partial_exemption_coverage_still_requires_level`로 검증했습니다.
- `changed_paths_for_level`는 `validate_issue_evidence`의 새 선택 인자(기본값 `None`)라 기존 호출자(테스트 다수, 기존 CLI 흐름 밖 사용처)는 동작이 바뀌지 않습니다. `test_evidence_ignores_level_check_when_changed_paths_not_supplied`로 하위 호환을 확인했습니다.
- 소급 FAIL 방지: Issue #57이 replay한 5건(#7·#8·#9·#40·#10)의 실제 변경 파일 목록과 실제 `verification-log.md` PASS 행을 그대로 재현한 fixture 테스트 5개(`Issue57ReplayFixtureRegressionTest`)가 전부 통과합니다 — 이 게이트 코드가 실제로 병합됐어도 과거 5개 Issue는 여전히 FAIL하지 않았을 것임을 뜻합니다.
- 문서·정책 변경은 없어 `docs/testing/level-mapping-design.md`를 참조만 하고 수정하지 않았습니다(코드 주석에서 경로만 인용).
- Gradle 빌드, runtime, API 테스트는 이 Issue 범위(하네스 스크립트)와 무관해 의도적으로 실행하지 않았습니다. 하네스 자체 회귀(`pytest scripts/tests/test_harness_gate.py`)만 근거로 사용했습니다.
