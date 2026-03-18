package com.ofp.orderservice.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {

		Map<String, String> errors = new HashMap<>();
		if(ex.getBindingResult() != null) {
			ex.getBindingResult().getFieldErrors().forEach(error -> {
				String field = (error).getField();
				String message = error.getDefaultMessage();
				errors.put(field, message);
			});
		}

		Map<String, Object> response = new HashMap<>();
		response.put("timestamp", LocalDateTime.now().toString());
		response.put("status", HttpStatus.BAD_REQUEST.value());
		response.put("errors", errors);

		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {

		Map<String, Object> response = new HashMap<>();
		response.put("timestamp", LocalDateTime.now().toString());
		response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		response.put("error", "An unexpected error occurred");

		return ResponseEntity.internalServerError().body(response);
	}
}
