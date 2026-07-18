-- Bounded retention 후보를 state와 committed_at 순서로 찾고 잠금 범위를 제한합니다.
create index idx_ranking_event_ledger_cleanup
    on ranking_event_ledger (state, committed_at);
