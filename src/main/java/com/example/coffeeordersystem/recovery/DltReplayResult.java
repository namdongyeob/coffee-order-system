// DLT 선택 재발행의 운영자 확인 결과를 전달합니다.
package com.example.coffeeordersystem.recovery;

public record DltReplayResult(DltReplayStatus status, String eventId, String risk) {
}
