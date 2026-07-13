// 랭킹 복구를 안전하게 중단해야 하는 원인을 표현합니다.
package com.example.coffeeordersystem.ranking.rebuild;

public class RankingRebuildException extends RuntimeException {

	public RankingRebuildException(String message) {
		super(message);
	}

	public RankingRebuildException(String message, Throwable cause) {
		super(message, cause);
	}
}
