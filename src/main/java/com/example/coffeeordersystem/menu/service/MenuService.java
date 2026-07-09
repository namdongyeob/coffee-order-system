package com.example.coffeeordersystem.menu.service;

import com.example.coffeeordersystem.menu.dto.MenuResponse;
import com.example.coffeeordersystem.menu.repository.MenuRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {

	private final MenuRepository menuRepository;

	public MenuService(MenuRepository menuRepository) {
		this.menuRepository = menuRepository;
	}

	@Transactional(readOnly = true)
	public List<MenuResponse> getMenus() {
		return menuRepository.findAllByOrderByIdAsc()
				.stream()
				.map(MenuResponse::from)
				.toList();
	}
}
