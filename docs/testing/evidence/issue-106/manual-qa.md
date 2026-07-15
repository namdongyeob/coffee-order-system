# Manual QA

문서 전용 SOLO 작업이라 실제 시각 QA(스크린샷 등)는 필요하지 않습니다. 아래는 문서 정확성을 실제 저장소 상태와 대조 관찰한 결과입니다.

## 관찰 1 — 독립 Combined Verifier subagent 검토

이 작업의 대화 맥락을 전달받지 않은 fresh general-purpose agent가 `docs/architecture/code-style-guide.md`의 모든 주장을 실제 `src/main/java`, `src/test/java` 코드와 대조했습니다.

- DTO record + `from(entity)` 팩토리 — `*/dto/*.java` 6개 파일 전수 확인, 반례 없음.
- 패키지·클래스 네이밍(`Controller`/`Service`/`Repository`/`Request`/`Response` 접미사) — 확인.
- 테스트 메서드명(`getMenusReturnsSeedMenus` 등 4개 인용) — 실제 라인과 정확히 일치, `test` 접두사·언더스코어 사용 사례 전무.
- wildcard import 금지 — `grep "^import.*\.\*;" src/main/java` 결과 0건.
- 상수 UPPER_SNAKE_CASE — 인용된 상수 전부 실제 코드와 일치.
- "다루지 않는 것" 절의 예외 처리·로깅 배제 — `RankingRebuildException`, `DltReplayException`, `IllegalStateException` 직접 사용, `@Slf4j` 일부 클래스만 사용을 확인해 배제 판단이 정확함을 확인.

## 관찰 2 — 지적 사항 재검증과 수정

subagent가 `XxxIntegrationTest`가 항상 root 테스트 패키지에 있다는 서술이 문서 자신이 든 예시(`ranking/rebuild/RankingRebuildServiceIntegrationTest.java`, 실제 패키지 `ranking.rebuild`)와 모순된다고 지적했습니다. `head -3`으로 `OrderPaymentIntegrationTest.java`(root)와 `RankingRebuildServiceIntegrationTest.java`(`ranking.rebuild`)의 실제 `package` 선언을 직접 확인해 지적이 정확함을 확인하고, 문서에서 위치 관련 서술을 "위치는 규칙이 아니다"로 정정했습니다.

## 미검증 항목

없음. 문서 전용 변경이라 Level 5/6(실제 앱 기동·HTTP)은 대상이 아닙니다.
