create table menu (
    id bigint not null auto_increment,
    name varchar(100) not null,
    price int not null,
    primary key (id),
    constraint uk_menu_name unique (name),
    constraint chk_menu_price_positive check (price > 0)
);

insert into menu (id, name, price)
values
    (1, '아메리카노', 4500),
    (2, '카페라떼', 5000),
    (3, '카푸치노', 5500),
    (4, '에스프레소', 4000);
