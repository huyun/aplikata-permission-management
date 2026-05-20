package com.aplikata.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.aplikata.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
		String message = ex.getMessage();
		if (ex instanceof BadCredentialsException) {
			message = "Invalid username or password";
		} else if (ex instanceof UsernameNotFoundException) {
			message = "User not found";
		}
		ApiResponse<Void> response = ApiResponse.error(401, message);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	// Handle custom business exceptions
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
		ApiResponse<Void> response = ApiResponse.error(ex.getCode(), ex.getMessage());
		// Return the corresponding HTTP status code based on the code; here we simply
		// return 200, but the frontend judges based on the business code
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	// Handle parameter validation exceptions (e.g., @Valid validation failures)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
			MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		ApiResponse<Map<String, String>> response = ApiResponse.error(400, "Parameter validation failed", errors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// Handle all other uncaught exceptions
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
		// Log the error
		ex.printStackTrace();
		ApiResponse<Void> response = ApiResponse.error(500, "Internal server error");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

}
