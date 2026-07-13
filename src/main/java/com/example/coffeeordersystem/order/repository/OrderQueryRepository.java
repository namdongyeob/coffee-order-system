// 주문 DB 원천 검증 조회의 repository 계약을 정의합니다.
package com.example.coffeeordersystem.order.repository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderQueryRepository {

	List<PaidMenuOrderCount> findTopPaidMenuOrderCounts(LocalDateTime from, LocalDateTime to);
}
