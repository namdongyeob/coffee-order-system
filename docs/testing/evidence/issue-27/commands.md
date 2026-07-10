# Issue #27 Commands

| 명령 | 목적 | 결과 |
| --- | --- | --- |
| `python scripts/harness_gate.py --issue 27 --base-ref origin/main --check-links` | Issue evidence와 변경 Markdown 링크 검사 | PASS. verification log 추가 후 재실행으로 확정합니다. |
| `rg -n -i --glob '*.md' 'reviewer-checklist\\.md|remove-ai-slop\\.md|done-claim-template\\.md|verification-levels\\.md|docs/ai/5-6-orchestration-implementation-plan\\.md' .` | 삭제·이동 전 경로 참조 검사 | PASS. 일치 항목이 없으며 `rg` 종료 코드 1은 무결과입니다. |
| `git diff --check` | Markdown 공백 오류 검사 | PASS. 공백 오류가 없습니다. |
| `git diff --name-only origin/main` | 허용 범위와 앱 코드 미변경 검사 | PASS. 문서와 evidence 파일만 변경되었습니다. |
| `python scripts/harness_gate.py --issue 27 --base-ref origin/main --check-links` | Claude 리뷰 반영 후 Issue evidence와 Markdown 링크 재검사 | PASS. Harness gate가 통과했습니다. |
| `rg -n -i --glob '*.md' 'reviewer-checklist\\.md|remove-ai-slop\\.md|done-claim-template\\.md|verification-levels\\.md|docs/ai/5-6-orchestration-implementation-plan\\.md' .` | 삭제·이동 전 경로 참조 재검사 | PASS. 일치 항목이 없으며 `rg` 종료 코드 1은 무결과입니다. |
| PowerShell `StartsWith('- 필수. [Review Gate]')`와 링크 수 정규식 | Review hot path 필수 링크 수 재검사 | PASS. 필수 링크가 5개입니다. |
| `git diff --check` | Markdown 공백 재검사 | PASS. 공백 오류가 없습니다. |
