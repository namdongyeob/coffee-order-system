package com.example.coffeeordersystem.order.domain;

import com.example.coffeeordersystem.menu.domain.Menu;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "menu_id", nullable = false)
	private Menu menu;

	@Column(name = "paid_amount", nullable = false)
	private int paidAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private OrderStatus status;

	@Column(name = "ordered_at", nullable = false)
	private LocalDateTime orderedAt;

	public Order(Long userId, Menu menu, int paidAmount, OrderStatus status, LocalDateTime orderedAt) {
		this.userId = userId;
		this.menu = menu;
		this.paidAmount = paidAmount;
		this.status = status;
		this.orderedAt = orderedAt;
	}
}
