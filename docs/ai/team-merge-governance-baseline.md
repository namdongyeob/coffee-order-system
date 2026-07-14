# 팀 저장소 Merge Governance Baseline (조건부 참조)

이 문서는 핵심 실행 계약이 아니며, 4인 팀 저장소로 이전하거나 사람 승인 가능 팀원이 참여하는 시점에만 읽습니다. merge 거버넌스 기본값의 정본은 [오케스트레이션 정책](orchestration-policy.md)의 "merge 거버넌스" 절이며, 이 문서는 팀 이전 시 활성화할 GitHub branch rule의 구체 항목만 확정합니다.

## 현재 개인 저장소 (그대로 유지)

- PR과 GitHub Actions `quality-gates`(deterministic CI)를 우선 강제합니다. 이 저장소에는 아직 branch protection이나 ruleset을 설정하지 않았습니다.
- AI Review/QA 결과는 `docs/testing/evidence/issue-{number}/`의 Markdown 자기신고이며, GitHub required check가 아닙니다.
- 이 현행은 팀 이전 전까지 바뀌지 않습니다.

## 팀 이전 시 활성화할 branch rule

- **GitHub native approval 최소 1명**(사람 도메인 오너). AI Review/QA 승인은 이 approval을 대체하지 않습니다.
- **Stale approval dismissal**: 승인 뒤 새 push가 있으면 기존 승인을 무효화합니다.
- **Unresolved conversation 차단**: PR의 미해결 대화가 남아 있으면 merge를 막습니다.
- **CI required check 유지**: `quality-gates`는 현재도 강제하고 있으며 팀 이전 후에도 그대로 required로 둡니다.

## AI Review/QA를 required check로 만들지 않는 이유

[Issue #56](https://github.com/namdongyeob/coffee-order-system/issues/56)이 정의한 신뢰 경계를 그대로 따릅니다: CI가 직접 실행한 명령·exit code만 ground truth이고, 로컬 에이전트가 작성하는 receipt·evidence는 변조 가능한 자기신고이며 증명이 아닙니다. 로컬 에이전트가 직접 게시하는 GitHub Check도 이 경계에서는 evidence Markdown과 신뢰 수준이 같습니다 — "기계 캡처처럼 보이는 자기 신고"일 뿐입니다. 그래서 이 baseline은 AI Review/QA를 required check로 승격하지 않고, 사람의 native approval만 merge 필수 조건으로 둡니다.

독립 CI 실행(API 키·격리된 실행 환경에서 정확한 head SHA에 대해 실행하고 비용·보안·timeout을 통제하며 오탐·미탐을 측정하는 방식)으로 신뢰를 높이는 방안은 [Issue #93](https://github.com/namdongyeob/coffee-order-system/issues/93)에서 P2 advisory spike로 조건부 검토합니다. #93은 착수 전제 조건이 갖춰지기 전까지 착수하지 않는 상태이며, 이 baseline은 #93의 결과를 기다리지 않고 사람 approval만으로 완결됩니다.

## 활성화 방법과 소유권

branch protection·ruleset 활성화는 GitHub 저장소 접근 제어 설정 변경입니다. **사람이 GitHub UI 또는 `gh api`로 직접 수행**하며, 하네스나 AI 에이전트는 이 설정을 자동으로 변경하지 않습니다. 이 문서는 그 활성화 시점에 사람이 참고할 체크리스트입니다.

### 활성화 체크리스트 (팀 이전 시점에만 수행, 지금은 미실행)

- [ ] Settings → Branches → `main`에 branch protection rule 추가.
- [ ] "Require a pull request before merging" 활성화, required approval 최소 1.
- [ ] "Dismiss stale pull request approvals when new commits are pushed" 활성화.
- [ ] "Require conversation resolution before merging" 활성화.
- [ ] "Require status checks to pass before merging"에 `quality-gates`가 포함돼 있는지 확인(신규 추가가 아니라 기존 강제의 재확인).
- [ ] AI Review/QA Check는 required 목록에 추가하지 않는다(advisory 유지, #93 조건 충족 전까지).
