// recovery lock 또는 pending rebuild 때문에 DLT 재발행을 나중에 재시도하도록 표시합니다.
package com.example.coffeeordersystem.recovery;

public class DltReplayRetryableException extends DltReplayException {

	public DltReplayRetryableException(String message) {
		super(message);
	}
}
