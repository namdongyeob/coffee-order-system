package com.example.coffeeordersystem.menu.dto;

import com.example.coffeeordersystem.menu.domain.Menu;

public record MenuResponse(Long id, String name, int price) {

	public static MenuResponse from(Menu menu) {
		return new MenuResponse(menu.getId(), menu.getName(), menu.getPrice());
	}
}
