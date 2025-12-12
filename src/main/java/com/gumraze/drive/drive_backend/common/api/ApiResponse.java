package com.gumraze.drive.drive_backend.common.api;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 API 응답 포맷")
public record ApiResponse<T>(
    @Schema(description = "요청 성공 여부", example = "true")
    boolean success,
    @Schema(description = "응답 메시지")
    String message,
    @Schema(description = "실제 응답 데이터")
    T data,
    @Schema(description = "오류 코드", example = "INVALID_ARGUMENT")
    String errorCode,
    @Schema(description = "응답 생성 시각")
    LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return success("OK", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> successMessage(String message) {
        return new ApiResponse<>(true, message, null, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, message, null, errorCode, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String errorCode, String message, T data) {
        return new ApiResponse<>(false, message, data, errorCode, LocalDateTime.now());
    }
}
