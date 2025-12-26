package com.gumraze.drive.drive_backend.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "공통 API 응답 포맷")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(

        @Schema(description = "요청 성공 여부", example = "true")
        boolean success,

        @Schema(description = "결과 코드", example = "OK")
        ResultCode code,

        @Schema(description = "응답 메시지", example = "요청 성공")
        String message,

        @Schema(description = "실제 응답 데이터")
        T data,

        @Schema(description = "응답 생성 시각(UTC)")
        OffsetDateTime timestamp
) {

    /**
     * =======================
     * API 성공 메서드
     * =======================
     */

    public static <T> ApiResponse<T> success(ResultCode code, String message, T data) {
        validateSuccess(code);
        return new ApiResponse<>(true, code, message, data, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> success(ResultCode code, T data) {
        return success(code, code.defaultMessage(), data);
    }

    public static ApiResponse<Void> success(ResultCode code) {
        return success(code, code.defaultMessage(), null);
    }

    /**
     * =======================
     * API 실패 메서드
     * =======================
     */

    public static ApiResponse<Void> failure(ResultCode code, String message) {
        validateFailure(code);
        return new ApiResponse<>(false, code, message, null, OffsetDateTime.now());
    }

    public static ApiResponse<Void> failure(ResultCode code) {
        return failure(code, code.defaultMessage());
    }

    /**
     * =======================
     * 내부 검증
     * =======================
     */

    private static void validateSuccess(ResultCode code) {
        if (!code.isSuccess()) {
            throw new IllegalArgumentException("성공 응답은 성공 ResultCode를 사용해야 합니다");
        }
    }

    private static void validateFailure(ResultCode code) {
        if (code.isSuccess()) {
            throw new IllegalArgumentException("실패 응답은 실패 ResultCode를 사용해야 합니다");
        }
    }
}
