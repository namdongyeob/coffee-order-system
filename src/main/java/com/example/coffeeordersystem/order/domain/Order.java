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

@Entity
@Table(name = "orders")
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

	protected Order() {
	}

	public Order(Long userId, Menu menu, int paidAmount, OrderStatus status, LocalDateTime orderedAt) {
		this.userId = userId;
		this.menu = menu;
		this.paidAmount = paidAmount;
		this.status = status;
		this.orderedAt = orderedAt;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public Menu getMenu() {
		return menu;
	}

	public int getPaidAmount() {
		return paidAmount;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public LocalDateTime getOrderedAt() {
		return orderedAt;
	}
}
