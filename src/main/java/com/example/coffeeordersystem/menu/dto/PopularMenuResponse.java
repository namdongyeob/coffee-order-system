package com.example.coffeeordersystem.menu.dto;

public record PopularMenuResponse(int rank, Long menuId, String menuName, long orderCount) {
}
