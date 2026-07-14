create table outbox_event (
    id bigint not null auto_increment,
    event_id varchar(100) not null,
    event_type varchar(100) not null,
    payload text not null,
    created_at datetime(6) not null,
    published_at datetime(6) null,
    primary key (id),
    constraint uk_outbox_event_event_id unique (event_id)
);

create index idx_outbox_event_published_at_id on outbox_event (published_at, id);
