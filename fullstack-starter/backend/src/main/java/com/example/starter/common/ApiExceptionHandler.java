package com.example.starter.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiResponse<Void>> invalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error(400, exception.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> unexpected(Exception exception) {
        return ResponseEntity.internalServerError().body(ApiResponse.error(500, "Internal server error"));
    }
}
