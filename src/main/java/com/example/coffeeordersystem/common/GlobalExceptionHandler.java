// API 예외를 공통 에러 응답 포맷으로 변환합니다.
package com.example.coffeeordersystem.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return ResponseEntity.status(errorCode.getStatus())
				.body(ErrorResponse.from(errorCode));
	}

	@ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
	public ResponseEntity<ErrorResponse> handleInvalidRequest() {
		ErrorCode errorCode = ErrorCode.INVALID_CHARGE_AMOUNT;
		return ResponseEntity.status(errorCode.getStatus())
				.body(ErrorResponse.from(errorCode));
	}
}
