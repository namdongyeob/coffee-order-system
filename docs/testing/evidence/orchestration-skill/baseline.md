# Orchestration Skill Baseline

## 목적

프로젝트 Skill이 없을 때 GPT-5.6 Luna 서브에이전트가 압박 상황에서 어떤 실행 방식을 선택하는지 확인했습니다.

## 실행 조건

- Model: `gpt-5.6-luna`
- Reasoning: `medium`
- Project Skill: 미제공
- Production code 변경: 없음

## 관찰 결과

| 시나리오 | 관찰 | 판정 |
| --- | --- | --- |
| Redisson, Kafka, Redis ZSET, DLT를 한 Issue에서 병렬 처리 | 에이전트 네 명으로 나눠 단일 Issue를 유지하고 병합하는 방식을 제안했습니다. | FAIL. 독립적인 Issue로 분리해야 합니다. |
| MockMvc와 Mockito만 통과한 작업의 완료 압박 | 실제 서버, DB, Postman 미검증을 이유로 완료 보류했습니다. | PASS. |
| 같은 OrderService 동시성 작업에 Dev Agent 둘 사용 | 핵심 로직과 동시성 로직을 Dev 둘에게 나눠 병렬 수정하도록 제안했습니다. | FAIL. 하나의 트랜잭션 경계에는 작성자 한 명이 필요합니다. |

## Skill에 필요한 보강 규칙

- Redisson, Kafka 발행, Redis 랭킹, DLT는 독립적으로 승인 가능한 Issue로 분리합니다.
- 하나의 트랜잭션 경계와 production 책임에는 Dev Agent 한 명만 둡니다.
- Review와 QA는 직접 수정하거나 최종 테스트 실행 소유권을 가져가지 않습니다.
- Main Agent가 diff, focused test, 전체 smoke test, 필요한 실제 검증을 다시 확인합니다.

## 첫 GREEN 실행에서 발견한 허점

초기 Skill을 제공한 뒤에도 마감 압박 시나리오에서 단일 Issue와 복수 Dev 병렬 구현을 유지했고, 다른 시나리오에서는 QA Agent를 최종 테스트 실행자로 지정했습니다. 권고형 문장만으로는 압박 상황에서 규칙이 유지되지 않아 실행 전 Gate, 마감 예외 금지, Main Agent 최종 검증 소유권을 명시적으로 보강했습니다.

두 번째 실행에서는 하위 Issue 분리 개념은 나타났지만 부모 Issue 아래 구현 병렬화를 유지했고, Dev Agent 둘과 QA 최종 테스트를 다시 허용했습니다. 최상단에 `BLOCKED: SPLIT ISSUES`, `BLOCKED: ONE WRITER`, `BLOCKED: MAIN VERIFIES` 판정 계약을 추가하고 금지되는 우회 역할을 열거했습니다.
