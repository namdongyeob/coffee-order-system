package com.example.coffeeordersystem.menu.controller;

import com.example.coffeeordersystem.menu.dto.MenuResponse;
import com.example.coffeeordersystem.menu.dto.PopularMenuResponse;
import com.example.coffeeordersystem.menu.service.MenuService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

	private final MenuService menuService;

	@GetMapping
	public ResponseEntity<List<MenuResponse>> getMenus() {
		return ResponseEntity.ok(menuService.getMenus());
	}

	@GetMapping("/popular")
	public ResponseEntity<List<PopularMenuResponse>> getPopularMenus() {
		return ResponseEntity.ok(menuService.getPopularMenus());
	}
}
