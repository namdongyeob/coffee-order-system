package com.example.coffeeordersystem.order.repository;

import com.example.coffeeordersystem.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderQueryRepository {
}
