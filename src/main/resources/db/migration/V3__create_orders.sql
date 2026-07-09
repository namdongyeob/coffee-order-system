create table orders (
    id bigint not null auto_increment,
    user_id bigint not null,
    menu_id bigint not null,
    paid_amount int not null,
    status varchar(30) not null,
    ordered_at datetime(6) not null,
    primary key (id),
    constraint fk_orders_menu foreign key (menu_id) references menu (id),
    constraint fk_orders_user_point foreign key (user_id) references user_point (user_id),
    constraint chk_orders_paid_amount_positive check (paid_amount > 0)
);

create index idx_orders_status_ordered_at on orders (status, ordered_at);
create index idx_orders_ordered_at_menu_id on orders (ordered_at, menu_id);
