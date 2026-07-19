# Issue #137 Manual QA

## 확인 결과

- 단일 테스트 표의 17개 경로 사례에서 네 영향도 출력이 각각 기대값과 일치했습니다.
- current Issue evidence-only는 source/runtime 판정을 유지하고 README와 source 혼합, unknown, rename/delete는 fail-closed로 승격됩니다.
- auto-merge fixture는 Writer·Review·QA identity 중복, 판정 누락·실패·차단, stale Review/QA, source SHA와 다른 CI를 모두 차단합니다.
- workflow의 required job 이름 `quality-gates`는 유지되며 `edited` 조건, 한 번의 링크 검사, 한 번의 Gradle invocation을 정적 확인했습니다.
- source 이벤트는 `quality-gates`/`source` concurrency, edited 이벤트는 `metadata-gates`/`metadata` concurrency를 사용해 서로 취소·대체할 수 없습니다.
- rename/delete `ChangeRecord`가 두 post-QA helper에서 모두 stale을 반환합니다.
- optional BLOCKED/PASS, Attempt·verification head, metrics retry 불일치 fixture가 fail-closed로 PASS했습니다.
- #137 bootstrap은 Java CI true, #138 동일 경량 diff는 false로 분리되고 source workflow에는 setup-java와 단일 `./gradlew test --no-daemon`이 유지됩니다.
- execution-head rename/delete evidence는 A/M 전용 gate에서 거부되며 `metadata-gates: SUCCESS`만으로 merge-ready가 되지 않습니다.
- `ready_for_review`가 trigger에 없고 source `quality-gates`와 edited `metadata-gates` identity는 고정돼 있습니다.
- 현재 non-runtime repository script 열 개만 exact allowlist이며 replay DLT는 runtime-heavy, 신규 `scripts/new_ops.ps1`은 unknown heavy로 분류됩니다.
- lightweight+replay mixed와 scripts rename/delete는 모두 Java CI·Review/QA stale·runtime evidence stale을 요구합니다.
- production/test/runtime 파일, #132 branch·worktree·evidence와 저장소 설정은 변경하지 않았습니다.

## Adversarial QA

- `STANDARD` 선언으로 harness 변경을 낮추는 사례, current Issue가 아닌 evidence, light+source mixed, unknown path, rename/delete와 optional evidence 모순을 거부하는 fixture가 PASS했습니다.

## Cleanup receipt

- 실행한 Docker·로컬 Gradle 장기 process와 생성 인프라가 없어 cleanup 대상이 없습니다.
