# Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `gh issue view 57 --json title,body,comments,state,labels` | Issue #57 본문·합의 코멘트 확인 | 접근 확정: Level을 서열이 아닌 independent capability로 정의, replay로 ENFORCE/OBSERVE/DROP 분류. |
| `gh pr list --search "<N> in:body" --state merged --json number,title,url` (N=7,8,9,40,10) | 각 Issue의 merge된 구현 PR 번호 확인 | #7→PR #38, #8→PR #39, #9→PR #41, #40→PR #42, #10→PR #43. |
| `gh pr diff <PR> --name-only` (38,39,41,42,43) | 각 PR의 실제 변경 파일 목록 수집 | `docs/testing/level-mapping-design.md`의 replay 표 근거 파일 목록으로 사용. |
| `gh pr diff <PR>` 뒤 `verification-log.md` 구간만 추출(`awk`+`grep '^+'`) | 각 Issue가 실측으로 PASS시킨 Level 행 수집 | #7: L1·L4·L5·L6 PASS. #8: L1·L4·L5·L6 PASS(+L6 PARTIAL 별건). #9: L1·L4·L5 PASS(L6 NO). #40: L1·L3·L4·L5 PASS(L6 NO). #10: L1·L2·L4·L5·L6 PASS. |
| `find src/main/java/com/example/coffeeordersystem/event -type f` | `**/event/**` 넓은 패턴의 이름 충돌 여부 확인 | `event/domain/ProcessedEvent.java`, `event/repository/ProcessedEventRepository.java`는 Kafka와 무관한 JPA entity/repository. M3를 `order/event/**`로 좁히는 근거. |
| `gh pr list --state merged --limit 100 --json number,title,files` + Python 필터 | ENFORCE 후보 경로가 5건 표본 밖에서도 등장하는지 확인(참고용, replay 표본 확장 아님) | PR #68·#76이 `ranking/consumer/**`를 추가로 변경했으나 Issue 범위(#7·#8·#9·#40·#10)에 없어 공식 replay 표본에는 포함하지 않음. |
| `python -m pytest scripts/tests/test_harness_gate.py -q` | worktree head에서 하네스 회귀 baseline 확인 | 107 passed, 110 subtests passed. |
| `python scripts/harness_gate.py --issue 57 --branch claude/issue-57-level-mapping-replay --base-ref 5859619 --check-links --include-worktree` | PR 전 preflight | 결과는 `verification.md`에 기록. |
