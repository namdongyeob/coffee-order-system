# Issue #58 Acceptance Criteria

Issue URL: https://github.com/namdongyeob/coffee-order-system/issues/58

Execution mode: STRICT
Execution mode reason: harness 검사 코드(workflow policy)를 변경합니다.
Level 5 required: NO
Level 5 reason: 애플리케이션 런타임 변경이 없습니다.
Level 6 required: NO
Level 6 reason: HTTP 계약 변경이 없습니다.

## 완료 기준

- [x] 확정 매핑 위반이 각각 FAIL하고 replay fixture가 회귀 테스트로 고정됨. `scripts/harness_gate.py`에 M1(`controller/**`→Level 2), M2(`consumer/**`→Level 4), M3(`order/event/**`→Level 4) ENFORCE 매핑을 구현했습니다. M8(`src/test/**`만 변경)은 매핑이 `src/main/java`만 매치하도록 정규식을 좁혀 자연히 제외됩니다. `scripts/tests/test_harness_gate.py`의 `LevelPathEnforcementTest`가 매핑 위반 FAIL·PASS·exemption 케이스를 단위 테스트로 검증하고, `Issue57ReplayFixtureRegressionTest`가 Issue #57의 replay 표본 5건(#7·#8·#9·#40·#10)의 실제 변경 경로를 회귀 테스트로 고정했습니다.
- [x] exemption code 외 사유로 통과 불가함을 테스트로 검증. `validate_level_exemptions`가 `docs/testing/level-mapping-design.md`의 고정 exemption code 5개 밖의 코드를 거부합니다. `test_unknown_exemption_code_is_rejected`, `test_fixed_exemption_codes_are_accepted`가 이를 검증합니다.
- [x] 기존 Issue 재검증이 소급 FAIL하지 않음. `Issue57ReplayFixtureRegressionTest`의 5개 테스트가 각 Issue의 실제 변경 경로와 실제 verification-log.md PASS 행을 재현해 새 Level 요구 오류가 생기지 않음을 확인합니다.

M6(`service/**`→Level 4)은 Issue #57에서 저장소 소유자가 OBSERVE 유지로 결정한 항목이므로 이 Issue에서 hard fail로 구현하지 않았습니다(`test_unmatched_service_path_is_observe_not_enforce`).

검증 실행 head는 아래 attempt-log.md와 verification.md를 참고합니다.
