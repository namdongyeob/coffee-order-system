// 기간별 유료 주문 메뉴 집계 결과를 전달합니다.
package com.example.coffeeordersystem.order.repository;

public record PaidMenuOrderCount(Long menuId, Long orderCount) {
}
