// 공통 API 에러 응답 본문입니다.
package com.example.coffeeordersystem.common;

public record ErrorResponse(String code, String message) {

	public static ErrorResponse from(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
	}
}
