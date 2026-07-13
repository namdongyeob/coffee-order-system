# Manual QA

- BLOCKED current disposition과 Issue #83 PASS verification 행의 조합이 `evidence reconciliation` 오류를 반환하는 fixture를 확인했습니다.
- PASS current disposition, checked acceptance, Attempt 1, 동일 head, retry 0 조합이 오류 없이 통과하는 fixture를 확인했습니다.
- metrics retry와 verification head를 각각 불일치시킨 fixture가 오류를 반환하는 것을 확인했습니다.
- execution head가 current HEAD의 unknown ancestor이면 실패하고, 이후 code/test 변경은 실패하며, Issue #83 evidence-only delta는 통과하는 fixture를 확인했습니다.
- Level 5/6은 Issue 범위상 NO이며 실행하지 않았습니다.
