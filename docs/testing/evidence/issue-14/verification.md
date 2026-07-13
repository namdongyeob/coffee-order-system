# 검증 로그

| 날짜 | Issue | Level | 결과 | 검증 범위 | 명령/Evidence | 비고 |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-07-13 | Issue #14 최종 repository 검증 | Level 5 | PASS | current Kafka earliest replay·Redis rebuild·retention loss fail-closed와 repository 정적 검증 | `docs/testing/evidence/issue-14/commands.md`; `docs/testing/evidence/issue-14/manual-qa.md`; Issue #14 repository gate; `git diff --check`; PR body preflight | 최신 main에서 focused 11건, related ranking/Kafka 28건, 전체 62건이 PASS했습니다. Level 5는 earliest `1`의 recent 보존 rebuild 성공과 earliest/latest `2/2` actual recent loss의 DB mismatch, live score·normal offset 보존, temp/backup cleanup을 확인했습니다. |
