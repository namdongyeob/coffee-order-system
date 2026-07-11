package com.example.coffeeordersystem.menu.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.coffeeordersystem.menu.dto.MenuResponse;
import com.example.coffeeordersystem.menu.dto.PopularMenuResponse;
import com.example.coffeeordersystem.menu.service.MenuService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void getMenusReturnsSeedMenus() throws Exception {
		mockMvc.perform(get("/api/menus"))
				.andExpect(status().isOk())
				.andExpect(content().json("""
						[
						  {"id": 1, "name": "아메리카노", "price": 4500},
						  {"id": 2, "name": "카페라떼", "price": 5000},
						  {"id": 3, "name": "카푸치노", "price": 5500},
						  {"id": 4, "name": "에스프레소", "price": 4000}
						]
						"""));
	}

	@Test
	void getPopularMenusReturnsRankedMenus() throws Exception {
		mockMvc.perform(get("/api/menus/popular"))
				.andExpect(status().isOk())
				.andExpect(content().json("""
						[
						  {"rank": 1, "menuId": 2, "menuName": "카페라떼", "orderCount": 12}
						]
						"""));
	}

	@TestConfiguration
	static class MenuControllerTestConfig {

		@Bean
		MenuService menuService() {
			MenuService menuService = mock(MenuService.class);
			when(menuService.getMenus()).thenReturn(List.of(
					new MenuResponse(1L, "아메리카노", 4500),
					new MenuResponse(2L, "카페라떼", 5000),
					new MenuResponse(3L, "카푸치노", 5500),
					new MenuResponse(4L, "에스프레소", 4000)
			));
			when(menuService.getPopularMenus()).thenReturn(List.of(
					new PopularMenuResponse(1, 2L, "카페라떼", 12)
			));
			return menuService;
		}
	}
}
