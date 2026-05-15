package com.vipinsharma.interviewprep.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.vipinsharma.interviewprep.dto.ApiResponse;
import com.vipinsharma.interviewprep.dto.ErrorCode;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INVALID_REQUEST, ex.getMessage()));
    }
}
