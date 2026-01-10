package com.sopt.cherrish.global.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.error.ErrorCode;
import com.sopt.cherrish.global.response.error.ErrorType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 커스텀 예외 처리
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<CommonApiResponse<Void>> handleBaseException(BaseException e) {
		ErrorType errorType = e.getErrorCode();
		log.warn("Business exception occurred: code={}, message={}", errorType.getCode(), errorType.getMessage());
		return ResponseEntity
			.status(errorType.getStatus())
			.body(CommonApiResponse.fail(errorType));
	}

	// @Valid 검증 실패 처리 (DTO validation)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public CommonApiResponse<Map<String, String>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e) {
		Map<String, String> errors = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.collect(Collectors.toMap(
				FieldError::getField,
				fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "유효하지 않은 값입니다"
			));
		log.warn("Validation failed: {}", errors);
		return CommonApiResponse.fail(ErrorCode.INVALID_INPUT, errors);
	}

	// 필수 요청 헤더 누락 처리
	@ExceptionHandler(MissingRequestHeaderException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public CommonApiResponse<Map<String, String>> handleMissingRequestHeaderException(
		MissingRequestHeaderException e) {
		log.warn("Required request header '{}' is missing", e.getHeaderName());
		Map<String, String> error = Map.of(
			"header", e.getHeaderName(),
			"message", "필수 헤더가 누락되었습니다"
		);
		return CommonApiResponse.fail(ErrorCode.INVALID_INPUT, error);
	}

	// 입력 값 검증 실패 처리 (Domain validation 등)
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public CommonApiResponse<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
		log.warn("Validation failed: {}", e.getMessage());
		Map<String, String> error = Map.of("message", e.getMessage());
		return CommonApiResponse.fail(ErrorCode.INVALID_INPUT, error);
	}

	// JSON 파싱 실패 처리 (enum, 날짜 형식 등)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public CommonApiResponse<Map<String, String>> handleMessageNotReadableException(HttpMessageNotReadableException e) {
		Map<String, String> errorDetails = new HashMap<>();

		if (e.getCause() instanceof InvalidFormatException invalidFormatException) {
			String fieldName = invalidFormatException.getPath().stream()
				.map(JsonMappingException.Reference::getFieldName)
				.collect(Collectors.joining("."));
			log.warn("Invalid format for field '{}': value={}, targetType={}",
				fieldName, invalidFormatException.getValue(), invalidFormatException.getTargetType().getSimpleName());
			errorDetails.put(fieldName, "올바른 형식이 아닙니다");
		} else {
			log.warn("Request body is not readable: {}", e.getMessage());
			errorDetails.put("body", "요청 데이터를 읽을 수 없습니다");
		}

		return CommonApiResponse.fail(ErrorCode.INVALID_FORMAT, errorDetails);
	}

	// 낙관적 락 충돌 처리 (동시성 제어)
	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public CommonApiResponse<Void> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException e) {
		log.warn("Optimistic locking failure: {}", e.getMessage());
		return CommonApiResponse.fail(ErrorCode.CONCURRENT_UPDATE);
	}

	// 그 외 모든 예외 처리
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public CommonApiResponse<Void> handleException(Exception e) {
		log.error("Unexpected error occurred", e);
		return CommonApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
	}
}
