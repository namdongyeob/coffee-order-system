# Manual QA

문서 전용 SOLO 작업이라 실제 시각 QA(스크린샷 등)는 필요하지 않습니다. 아래는 문서 정확성을 실제 저장소 상태와 대조 관찰한 결과입니다.

## 관찰 1 — 독립 agent의 사실 검증

fresh general-purpose agent가 README를 신뢰하지 않고 다음을 직접 읽어 대조했습니다.

- `build.gradle`(Spring Boot 4.1.0, Java 17, Redisson 4.6.1, QueryDSL 5.1.0, Lombok, Flyway) — 일치.
- 실제 패키지 트리(`find -maxdepth 2 -type d`) — README 다이어그램과 일치.
- 컨트롤러·DTO 실제 필드명/타입, HTTP 상태코드(201 등) — 일치.
- `ErrorCode.java`의 6개 코드와 HTTP 상태 — 정확히 일치.
- Redisson `tryLock(2, 5, TimeUnit.SECONDS)`, Outbox `fixedDelayString=2000`, 랭킹 Lua dedup 스크립트 — README 서술과 일치.
- `V1`~`V5` migration 파일명 — 일치.
- `docker/compose.yaml` 포트(13306/16379/19092/18080/15540) — 일치.
- README 링크 14개 — 전부 존재.
- 미래형/계획형 문구("다음 구현 대상은...", "예정입니다") — 없음 확인.

## 관찰 2 — 유일한 지적 사항의 반증

agent가 "테스트 76개" 주장을 `grep -c "@Test"` 결과 82와 다르다고 지적했습니다. `grep -c "@Test"`는 `@TestConfiguration`, `@Testcontainers`, `@TestMethodOrder` 등 `@Test`를 부분 문자열로 포함하는 다른 애노테이션까지 카운트하는 오탐임을 확인했습니다. 정확한 패턴(`^\s*@Test(\(.*\))?\s*$`)으로 재확인한 결과 76건이며, 이는 Issue #99 merge 직후 실행한 전체 회귀의 JUnit XML 집계(`build/test-results/test/*.xml`, tests=76 total, 0 failures)와 정확히 일치합니다. README는 정확했고 수정하지 않았습니다.

## 미검증 항목

없음. 문서 전용 변경이라 Level 5/6(실제 앱 기동·HTTP)은 대상이 아닙니다.
