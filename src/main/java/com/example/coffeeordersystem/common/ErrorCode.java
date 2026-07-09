// 공통 API 에러 코드를 정의하는 열거형입니다.
package com.example.coffeeordersystem.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INVALID_CHARGE_AMOUNT(HttpStatus.BAD_REQUEST, "INVALID_CHARGE_AMOUNT", "유효하지 않은 포인트 충전 요청입니다."),
	MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU_NOT_FOUND", "메뉴를 찾을 수 없습니다."),
	USER_POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_POINT_NOT_FOUND", "사용자 포인트를 찾을 수 없습니다."),
	INSUFFICIENT_POINT(HttpStatus.CONFLICT, "INSUFFICIENT_POINT", "포인트 잔액이 부족합니다."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
