# Manual QA

- 이 Issue는 신규 문서 `docs/testing/level-mapping-design.md`와 `docs/testing/evidence/issue-57/` 하위 evidence 6개만 추가합니다. 애플리케이션 코드, harness 스크립트(`scripts/harness_gate.py`)는 변경하지 않았습니다(변경은 #58 범위).
- Level 5/6은 Issue 본문·acceptance-criteria.md 결정대로 NO이며 runtime 또는 HTTP 검증을 완료로 표현하지 않습니다.
- replay 근거는 GitHub의 실제 merge된 PR(#38·#39·#41·#42·#43)의 `--name-only` 파일 목록과 각 PR이 `docs/testing/verification-log.md`에 추가한 PASS 행을 그대로 인용했습니다(가공하거나 요약 중 값을 바꾸지 않았습니다).
- `**/event/**`를 넓게 매칭하면 `event/domain/ProcessedEvent.java`(Kafka와 무관한 JPA entity)까지 걸린다는 이름 충돌을 `find` 명령으로 직접 확인하고, M3 매핑을 `order/event/**`로 좁혔습니다.
- M6(`**/service/**` → Level 4)은 표본 4/4 일치라는 강한 신호에도 불구하고 표본 선택 편향 가능성이 있어 ENFORCE로 자동 승격하지 않고 사용자에게 논쟁적 판단으로 보고합니다(`docs/testing/level-mapping-design.md`의 "판단이 필요한 항목" 절).
- exemption code는 자유 문장 사유를 인정하지 않는 고정 목록 5개로 정의했고, 오남용 위험이 큰 code(`NO_BEHAVIOR_CHANGE`, `SUPERSEDED_BY_HIGHER_LEVEL`)는 QA Agent 승인을 요구하도록 승인 주체를 분리했습니다.
- 문서 전용 Issue이므로 Gradle 빌드, runtime, API 테스트는 의도적으로 실행하지 않았습니다. 하네스 정적 회귀(`test_harness_gate.py`)만 Level 0 근거로 실행했습니다.
