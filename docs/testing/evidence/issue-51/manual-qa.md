# Manual QA

- `python scripts/rebuild_verification_log.py`가 Issue별 source와 legacy source의 행을 전역 표로 재현했습니다. 기준 전역 88행은 누락 없이 보존됐고, 추가 행은 Issue #51의 한 행뿐입니다.
- 생성 전역 뷰는 저장소에 기록하거나 커밋하지 않습니다.
- Level 5/6은 Issue 본문 결정대로 NO이며 runtime 또는 HTTP 검증을 완료로 표현하지 않습니다.
