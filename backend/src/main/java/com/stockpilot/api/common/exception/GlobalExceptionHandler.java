package com.stockpilot.api.common.exception;

import com.stockpilot.api.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import com.stockpilot.api.portfolio.exception.PortfolioNotFoundException;
import com.stockpilot.api.portfolio.exception.InsufficientSharesException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProviderUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleProviderUnavailable(ProviderUnavailableException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("PROVIDER_UNAVAILABLE")
                .message(ex.getMessage())
                .timestamp(Instant.now().toString())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(InvalidSymbolException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSymbol(InvalidSymbolException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_SYMBOL")
                .message(ex.getMessage())
                .timestamp(Instant.now().toString())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorResponse> handleTokenRefresh(TokenRefreshException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("TOKEN_REFRESH_FAILED")
                .message(ex.getMessage())
                .timestamp(Instant.now().toString())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_FAILED")
                .message("Validation failed for parameters: " + errors)
                .timestamp(Instant.now().toString())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_FAILED")
                .message(ex.getMessage())
                .timestamp(Instant.now().toString())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PortfolioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePortfolioNotFound(PortfolioNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("PORTFOLIO_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now().toString())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientSharesException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientShares(InsufficientSharesException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INSUFFICIENT_SHARES")
                .message(ex.getMessage())
                .timestamp(Instant.now().toString())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message(ex.getMessage())
                .timestamp(Instant.now().toString())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
