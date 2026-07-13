// DLT 재발행을 fail-closed로 중단하는 예외입니다.
package com.example.coffeeordersystem.recovery;

public class DltReplayException extends RuntimeException {

	public DltReplayException(String message) {
		super(message);
	}

	public DltReplayException(String message, Throwable cause) {
		super(message, cause);
	}
}
