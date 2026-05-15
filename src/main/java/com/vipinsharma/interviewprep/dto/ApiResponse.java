package com.vipinsharma.interviewprep.dto;

import java.time.Instant;

public record ApiResponse<T>(T data, ErrorInfo error, MetaInfo meta) {

    public record ErrorInfo(String code, String message) {}

    public record MetaInfo(String timestamp) {}

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, null, new MetaInfo(Instant.now().toString()));
    }

    public static <T> ApiResponse<T> error(ErrorCode code, String message) {
        return new ApiResponse<>(null, new ErrorInfo(code.name(), message), new MetaInfo(Instant.now().toString()));
    }
}
