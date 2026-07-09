create table user_point (
    id bigint not null auto_increment,
    user_id bigint not null,
    balance int not null,
    primary key (id),
    constraint uk_user_point_user_id unique (user_id),
    constraint chk_user_point_balance_non_negative check (balance >= 0)
);
