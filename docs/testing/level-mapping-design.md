<!-- Level 자기신고 구멍을 측정하기 위한 path→Level 후보 매핑 설계와 replay 근거 -->
# Level 매핑 설계와 replay 근거 (Issue #57)

## 배경

현재 게이트(`scripts/harness_gate.py`)는 `validate_changed_path_mode`로 변경 경로→Execution mode는 강제하지만, 변경 경로→필요 Level은 강제하지 않는다. `required_verification_levels` 계열 검사는 evidence에 `Level N required: YES/NO`와 이유 문자열이 "존재하는지"만 확인하고, 그 판단이 실제 변경 내용과 맞는지는 검사하지 않는다. 즉 Kafka Consumer나 Redisson 락을 추가하면서 "Level 4 required: NO, 이유: ..."라고만 적어도 게이트를 통과한다.

이 문서는 그 구멍을 메울 path→Level 매핑 후보를 설계하고, 실제 merge된 Issue #7·#8·#9·#40·#10의 diff에 replay해 오탐(false positive: 매핑이 요구하지만 실제로는 불필요)과 미탐(false negative: 매핑이 놓치지만 실제로는 필요했던 경우)을 측정한다. 게이트 코드 변경은 #58의 범위이며, 이 Issue는 어떤 매핑을 ENFORCE할지 결정하는 근거만 만든다.

## Level을 서열이 아닌 독립 capability로 정의

`docs/testing/test-strategy.md`의 Level 0~7은 성숙도 단계가 아니라 서로 다른 것을 검증하는 독립된 capability다. Level 4 PASS는 Kafka·Redis 통합을 확인했다는 뜻이지 Level 2(Controller 계약)나 Level 3(DB 트랜잭션)를 대체하지 않는다. 실제로 Issue #40은 Level 1·3·4·5를 모두 별도로 PASS시켰다(Kafka Consumer가 DB 멱등성과 Redis 갱신을 함께 검증해야 했기 때문). 따라서 이 설계의 매핑은 "이 경로가 바뀌면 최소 Level N이 필요하다"는 하한 규칙의 집합이며, 한 diff가 여러 규칙에 동시에 매치하면 필요한 Level 집합은 합집합이다. 상위 Level 통과가 하위 Level 요구를 자동으로 만족시키지 않는다.

## replay 방법

1. Issue #7(PR #38), #8(PR #39), #9(PR #41), #40(PR #42), #10(PR #43)의 실제 변경 파일 목록을 `gh pr diff <PR> --name-only`로 수집했다.
2. 각 Issue가 실제로 PASS시킨 Level은 해당 PR이 `docs/testing/verification-log.md`에 추가한 행에서 그대로 가져왔다(당시는 Issue별 `verification.md` 분리 이전이라 global 파일에 기록됨). 이 값을 "실측 필요 Level"의 대리 지표로 사용한다 — Review·QA가 독립적으로 PASS를 요구하고 확인한 Level이기 때문이다.
3. 후보 매핑 규칙을 각 PR의 변경 경로에 적용해 "매핑이 요구하는 Level 집합"을 계산하고, 실측 Level과 비교한다.
   - 매핑이 요구했고 실측에도 있음 → 참(정확한 탐지).
   - 매핑이 요구했지만 실측에 없음 → 오탐 후보.
   - 매핑이 요구하지 않았지만 실측에 있음 → 미탐 후보(경로만으로 못 잡는 값).
4. 이 5건은 표본이며 전수가 아니다. 표본에 없는 경로(예: `db/migration/**`, `ranking/rebuild/**`, `recovery/**`)는 구조적으로만 추정하고 OBSERVE로 분류한다.

## 후보 매핑과 분류

| # | 경로 패턴 | 요구 Level | 근거 | 분류 |
| --- | --- | --- | --- | --- |
| M1 | `src/main/java/**/controller/**.java` | Level 2 | #10에서 `MenuController.java` 변경 → 실측 Level 2 PASS 존재. 표본 내 이 패턴에 매치되는 다른 변경 없음(0 오탐). | **ENFORCE** |
| M2 | `src/main/java/**/consumer/**.java` | Level 4 | #40에서 `RankingEventConsumer.java`, `RankingEventProcessor.java`(`ranking/consumer/`) 변경 → 실측 Level 4 PASS 존재. 표본 내 다른 매치 없음(0 오탐). | **ENFORCE** |
| M3 | `src/main/java/**/order/event/**.java` | Level 4 | #8에서 `OrderEventPublisher.java`, `OrderCompletedEvent.java`(Kafka producer) 변경 → 실측 Level 4 PASS 존재. **주의**: 패턴을 `**/event/**`로 넓히면 `event/domain/ProcessedEvent.java`, `event/repository/ProcessedEventRepository.java`(순수 JPA entity, Kafka와 무관)까지 매치되어 오탐을 만든다. 실제 이 저장소에 이름 충돌 패키지가 존재하므로 `order/event`로 정확히 좁혀야 한다. | **ENFORCE (좁힌 패턴만)** |
| M4 | `src/main/resources/db/migration/**.sql` | Level 3 | 표본 5건에 migration 파일 변경 없음 → 검증 불가. `test-strategy.md`가 DB·schema를 Level 3 대상으로 명시하므로 구조적으로는 타당하나 replay 근거가 없다. | **OBSERVE** |
| M5 | `src/main/resources/application.properties` 등 설정 파일 변경 | Level 4 또는 Level 5 | #8, #40에서 함께 변경되었고 실측 Level 4/5 PASS 존재하지만, 이 파일은 로그 레벨처럼 인프라와 무관한 설정도 담을 수 있어 표본만으로는 원인(설정 변경 자체가 원인인지, 같은 PR의 다른 코드 변경이 원인인지)을 분리할 수 없다. 오탐 위험이 구조적으로 높다. | **OBSERVE** |
| M6 | `src/main/java/**/service/**.java` | Level 4 | #7 `OrderService.java`(Redisson), #8 `OrderService.java`(Kafka 발행 호출), #9 `PopularMenuRankingService.java`(Redis 쓰기), #10 `MenuService.java`·`PopularMenuRankingService.java`(Redis 조회) 모두 실측 Level 4 PASS와 일치한다(4/4). 그러나 이 일치는 표본 선택 편향일 수 있다 — 5건 모두 "Redis/Kafka 기능 구현" Issue였기 때문에 `service/` 경로가 매번 인프라를 호출했을 뿐, `service/` 디렉터리 자체가 인프라 호출을 의미하지 않는다. `point/service/**`처럼 순수 포인트 계산만 바꾸는 변경도 이 패턴에 매치되어 실제로는 불필요한 Level 4를 요구할 위험이 있다. 표본에 그런 반례가 없어 확인할 수 없다. | **OBSERVE (논쟁적 — 아래 "판단이 필요한 항목" 참고)** |
| M7 | `src/main/java/**/ranking/rebuild/**.java`, `src/main/java/**/recovery/**.java` | Level 4 | DLT replay·rebuild는 Kafka·Redis 인프라를 다루는 것이 구조적으로 명백하지만, 표본 5건에 변경이 없어 replay 근거가 없다. | **OBSERVE** |
| M8 | `src/test/**` 하위 경로만 변경(운영 코드 변경 없음) | 매핑 없음(Level 요구 안 함) | #7에서 `OrderControllerTest.java`(`order/controller/` 테스트 경로)가 변경됐지만 실측에는 Level 2 PASS 행이 없었다. 테스트 경로까지 M1과 동일하게 매치시키면 오탐이 발생했을 것이다. 그래서 컨트롤러·컨슈머·이벤트 매핑(M1~M3)은 `src/main` 운영 코드에만 적용하고 `src/test`는 매치 대상에서 제외한다. | **DROP (경로 매핑 대상에서 제외를 확정)** |

### 판단이 필요한 항목 — 사용자 보고

M6(`**/service/**.java` → Level 4)은 표본 내 4/4 일치라는 강한 신호와, "표본 선택 편향으로 인한 우연의 일치일 수 있다"는 반대 근거가 동시에 존재하는 논쟁적 판단이다. `service/` 경로만으로 인프라 호출 여부를 알 수 없다는 구조적 한계가 있고, 이 저장소에는 `point/service/**`처럼 실제로 순수 DB/비즈니스 로직만 다루는 서비스 디렉터리도 있다. #58에서 이 규칙을 ENFORCE로 승격할지, 계속 OBSERVE로 남겨 추가 표본(예: `point/service/**` 변경 Issue)을 기다릴지는 사람 판단이 필요하다. 이 Issue에서는 사용자에게 보고하고 자동으로 ENFORCE에 포함하지 않는다.

## 경로 기반 검사의 한계

- **이름 충돌**: M3에서 확인했듯 같은 단어(`event`)가 서로 다른 의미(Kafka 이벤트 vs JPA entity)로 쓰이는 패키지가 실제로 존재한다. 패턴은 짧은 키워드가 아니라 저장소의 실제 패키지 전체 경로로 좁혀야 한다.
- **문서 수정과 실제 API 변경 구분 불가**: 경로 매칭은 `docs/api/api-spec.md` 같은 문서 파일이 실제 계약을 바꿨는지, 오탈자만 고쳤는지 구분하지 못한다. 이 설계는 `src/main/java/**/controller/**.java` 같은 코드 경로만 신호로 쓰고 문서 경로는 Level 요구 신호에서 제외한다.
- **표본 편향**: M6처럼 5건의 표본이 모두 특정 기능 계열(Redis/Kafka 구현)에서 나왔기 때문에, 경로와 실제 인프라 호출 여부가 표본 안에서는 항상 같이 움직였다. 반례가 되는 Issue(순수 DB 로직만 바꾸는 `service/` 변경)가 표본에 없으면 오탐률을 확정할 수 없다.
- **파일명 리팩터링**: 클래스를 이동하거나 패키지를 재구성하면 매핑이 깨진다. 이 설계는 현재 패키지 구조(`order`, `menu`, `point`, `ranking`, `event`, `recovery`)를 전제로 하며, 구조가 바뀌면 매핑도 다시 검증해야 한다.
- **삭제만 있는 diff**: 파일을 삭제만 하는 변경(예: dead code 제거)도 경로 매칭에 걸리면 불필요한 Level을 요구할 수 있다. 이번 표본에는 삭제 전용 diff가 없어 확인하지 못했다.

## exemption code 체계

경로 매핑이 ENFORCE로 승격된 뒤에도 실제로는 Level이 불필요한 예외 상황이 나올 수 있다(예: `order/event/**`에 순수 로깅 문구만 고치는 변경). 이런 예외를 자유 문장 사유로 우회하면 `required_verification_levels`가 이미 겪고 있는 자기신고 구멍을 그대로 재생산한다. 그래서 예외는 고정된 code 목록에서만 선택하고, 각 code는 `docs/testing/evidence/issue-{number}/acceptance-criteria.md`에 `Level exemption: <CODE> — <PR 번호 또는 커밋 SHA>` 형식으로 기록한다. code 밖의 자유 서술은 exemption으로 인정하지 않는다.

| code | 의미 | 승인 주체 |
| --- | --- | --- |
| `TEST_ONLY_IN_MATCHED_DIR` | 매치된 운영 코드 경로가 아니라 같은 패키지의 테스트 파일만 바뀜(M8이 이미 기본 제외하므로 M8 예외 케이스에서 패턴이 새로 넓어졌을 때만 사용) | Review Agent 확인 뒤 QA Agent 최종 승인 |
| `NO_BEHAVIOR_CHANGE` | 매치된 파일에서 로깅, 주석, import 정리처럼 컴파일 결과가 동일한 변경만 있고 실행 경로가 바뀌지 않음 | QA Agent가 diff를 직접 확인하고 승인. Review Agent 단독 승인 불가 |
| `DOC_STRING_ONLY` | 매치된 Java 파일 안에서 Javadoc·주석 문자열만 바뀜 | Review Agent 승인으로 충분 |
| `SUPERSEDED_BY_HIGHER_LEVEL` | 같은 diff가 이미 더 엄격한 별도 Level PASS를 보유하고, 매치된 Level이 그 상위 검증에 포함됨이 QA가 실행한 실제 테스트 케이스로 증명됨(Level 서열 취급 금지 원칙과 충돌하지 않도록 "포함 증명"이 있을 때만 사용) | QA Agent가 포함 근거(테스트 케이스명)를 명시하고 승인 |
| `HARNESS_SELF_CHANGE` | 게이트·replay 스크립트 자신을 수정하는 Issue라서 대상 애플리케이션 인프라가 실행되지 않음 | Main Coordinator 확인 뒤 사람 승인 |

exemption code는 매핑을 약화하는 도구가 아니라 매핑이 맞았을 때만 쓰는 좁은 탈출구다. `NO_BEHAVIOR_CHANGE`와 `SUPERSEDED_BY_HIGHER_LEVEL`처럼 오남용 위험이 큰 code는 QA Agent 승인을 요구해 Dev가 스스로 자신의 변경에 예외를 선언할 수 없게 한다.

### 표본 내 실제 exemption 사례

이 Issue의 replay 범위(#7·#8·#9·#40·#10)에서는 ENFORCE 후보(M1·M2·M3)가 매치된 모든 경로가 실제로 해당 Level PASS를 보유했다. 즉 이 5건 안에서는 exemption이 필요한 실제 사례가 없었다(0건). exemption code 목록은 향후 OBSERVE 항목이 ENFORCE로 승격되거나 새 Issue에서 반례가 나올 때를 대비한 구조이며, 이 Issue는 그 구조만 정의하고 실사용 사례는 #58 이후 운영에서 축적한다.

## #58로 전달하는 확정 사항

- ENFORCE 후보: M1(`controller/**`→Level 2), M2(`consumer/**`→Level 4), M3(`order/event/**`→Level 4, 좁힌 패턴), M8(`src/test`만 변경 시 경로 매핑 미적용).
- OBSERVE 후보(추가 표본 필요, #58에서 hard fail로 구현하지 않음): M4(migration→Level 3), M5(설정 파일→Level 4/5), M6(`service/**`→Level 4, 논쟁적), M7(rebuild/recovery→Level 4).
- 과거 Issue #7·#8·#9·#40·#10은 이 replay로 새로운 누락이 발견되지 않았다(모든 ENFORCE 후보가 실측 PASS와 일치). 소급 FAIL 대상 없음.
- 표본 확장 후보(참고, 이 Issue의 공식 replay 범위 밖): PR #68("Kafka Consumer 재시도와 DLT 이동")과 PR #76("Kafka replay 기반 Redis ranking rebuild")도 `ranking/consumer/**`(M2)를 변경했다. 이 Issue는 Issue 본문이 지정한 #7·#8·#9·#40·#10 5건만 공식 replay 표본으로 사용했으므로 이 두 PR을 M2 신뢰도 수치에 포함하지 않았지만, #58에서 M2를 ENFORCE로 구현하기 전 추가 신뢰도 확인용 표본으로 검토할 가치가 있다.
