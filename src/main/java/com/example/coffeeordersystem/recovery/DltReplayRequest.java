// DLT에서 정확히 한 레코드를 선택하는 승인 입력을 표현합니다.
package com.example.coffeeordersystem.recovery;

public record DltReplayRequest(
		String dltTopic,
		int partition,
		long offset,
		String approvedBy,
		String reason
) {
}
