-- Rebuild swap 이후 event ledger와 pending run 이벤트를 보존합니다.
create table ranking_event_ledger (
    event_id varchar(36) not null,
    event_type varchar(100) not null,
    payload_fingerprint char(64) not null,
    state varchar(32) not null,
    source varchar(32) not null,
    rebuild_run_id varchar(36) null,
    reserved_at datetime(6) not null,
    redis_applied_at datetime(6) null,
    committed_at datetime(6) null,
    primary key (event_id),
    constraint chk_ranking_event_ledger_state
        check (state in ('RESERVED', 'REDIS_APPLIED', 'COMMITTED')),
    constraint chk_ranking_event_ledger_source
        check (source in ('NORMAL_CONSUMER', 'DLT_REPLAY', 'REBUILD')),
    constraint chk_ranking_event_ledger_type
        check (event_type = 'order.completed'),
    index idx_ranking_event_ledger_run (rebuild_run_id)
);

create table ranking_rebuild_run (
    run_id varchar(36) not null,
    state varchar(32) not null,
    created_at datetime(6) not null,
    swapped_at datetime(6) null,
    completed_at datetime(6) null,
    primary key (run_id),
    constraint chk_ranking_rebuild_run_state
        check (state in ('PREPARED', 'SWAPPED_PENDING_LEDGER', 'COMPLETED'))
);

create table ranking_rebuild_run_event (
    run_id varchar(36) not null,
    event_id varchar(36) not null,
    event_type varchar(100) not null,
    order_id bigint not null,
    user_id bigint not null,
    menu_id bigint not null,
    paid_amount int not null,
    ordered_at datetime(6) not null,
    payload_fingerprint char(64) not null,
    primary key (run_id, event_id),
    constraint fk_ranking_rebuild_run_event_run
        foreign key (run_id) references ranking_rebuild_run (run_id),
    constraint chk_ranking_rebuild_run_event_type
        check (event_type = 'order.completed'),
    index idx_ranking_rebuild_run_event_event (event_id)
);
