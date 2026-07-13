// DLT 선택 재발행의 최종 상태를 표현합니다.
package com.example.coffeeordersystem.recovery;

public enum DltReplayStatus {
	REPUBLISHED,
	SKIPPED_ALREADY_PROCESSED
}
