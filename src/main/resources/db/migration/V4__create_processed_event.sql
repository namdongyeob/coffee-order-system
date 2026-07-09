create table processed_event (
    id bigint not null auto_increment,
    event_id varchar(100) not null,
    event_type varchar(100) not null,
    consumer_group varchar(100) not null,
    processed_at datetime(6) not null,
    primary key (id),
    constraint uk_processed_event_event_id unique (event_id)
);
