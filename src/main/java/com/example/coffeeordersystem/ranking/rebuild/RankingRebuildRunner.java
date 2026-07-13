// 명시적으로 활성화된 maintenance 실행에서만 랭킹 rebuild를 시작합니다.
package com.example.coffeeordersystem.ranking.rebuild;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ranking.rebuild.enabled", havingValue = "true")
public class RankingRebuildRunner implements ApplicationRunner {

	private final RankingRebuildService service;

	@Override
	public void run(ApplicationArguments args) {
		service.rebuild();
	}
}
