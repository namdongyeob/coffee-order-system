package com.example.coffeeordersystem.point.domain;

import com.example.coffeeordersystem.common.ApiException;
import com.example.coffeeordersystem.common.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_point")
public class UserPoint {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false, unique = true)
	private Long userId;

	@Column(nullable = false)
	private int balance;

	protected UserPoint() {
	}

	public UserPoint(Long userId, int balance) {
		this.userId = userId;
		this.balance = balance;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public int getBalance() {
		return balance;
	}

	public void charge(int amount) {
		long chargedBalance = (long) balance + amount;
		if (chargedBalance < 0 || chargedBalance > Integer.MAX_VALUE) {
			throw new ApiException(ErrorCode.INVALID_CHARGE_AMOUNT);
		}
		this.balance = (int) chargedBalance;
	}
}
