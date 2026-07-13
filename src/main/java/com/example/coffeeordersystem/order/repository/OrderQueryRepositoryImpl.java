// 주문 DB 원천 집계를 QueryDSL로 실행합니다.
package com.example.coffeeordersystem.order.repository;

import static com.example.coffeeordersystem.order.domain.QOrder.order;

import com.example.coffeeordersystem.order.domain.OrderStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;

public class OrderQueryRepositoryImpl implements OrderQueryRepository {

	private final JPAQueryFactory queryFactory;

	public OrderQueryRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public List<PaidMenuOrderCount> findTopPaidMenuOrderCounts(LocalDateTime from, LocalDateTime to) {
		return queryFactory
				.select(Projections.constructor(PaidMenuOrderCount.class, order.menu.id, order.id.count()))
				.from(order)
				.where(
						order.status.eq(OrderStatus.PAID),
						order.orderedAt.goe(from),
						order.orderedAt.lt(to))
				.groupBy(order.menu.id)
				.orderBy(order.id.count().desc())
				.limit(3)
				.fetch();
	}
}
