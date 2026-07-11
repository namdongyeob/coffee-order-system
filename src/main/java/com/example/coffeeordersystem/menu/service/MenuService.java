package com.example.coffeeordersystem.menu.service;

import com.example.coffeeordersystem.menu.dto.MenuResponse;
import com.example.coffeeordersystem.menu.dto.PopularMenuResponse;
import com.example.coffeeordersystem.menu.domain.Menu;
import com.example.coffeeordersystem.menu.repository.MenuRepository;
import com.example.coffeeordersystem.ranking.service.PopularMenuRanking;
import com.example.coffeeordersystem.ranking.service.PopularMenuRankingService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuService {

	private final MenuRepository menuRepository;
	private final PopularMenuRankingService popularMenuRankingService;

	@Transactional(readOnly = true)
	public List<MenuResponse> getMenus() {
		return menuRepository.findAllByOrderByIdAsc()
				.stream()
				.map(MenuResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<PopularMenuResponse> getPopularMenus() {
		List<PopularMenuRanking> rankings =
				popularMenuRankingService.findRecentSevenDayRankings(LocalDate.now());
		Map<Long, Menu> menus = new HashMap<>();
		menuRepository.findAllById(rankings.stream().map(PopularMenuRanking::menuId).toList())
				.forEach(menu -> menus.put(menu.getId(), menu));
		List<PopularMenuResponse> results = new ArrayList<>();
		for (PopularMenuRanking ranking : rankings) {
			Menu menu = menus.get(ranking.menuId());
			if (menu != null) {
				results.add(new PopularMenuResponse(results.size() + 1, menu.getId(), menu.getName(), ranking.orderCount()));
			}
			if (results.size() == 3) {
				break;
			}
		}
		return results;
	}
}
