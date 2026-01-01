package com.gumraze.drive.drive_backend.common.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* =========================
     *  Validation Exception
     * ========================= */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = Optional.ofNullable(fieldError)
                .map(err -> String.format("%s: %s", err.getField(), err.getDefaultMessage()))
                .orElse(ResultCode.VALIDATION_ERROR.defaultMessage());

        log.warn("[검증 실패]: {}", message);

        ResultCode code = ResultCode.VALIDATION_ERROR;
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.failure(code, message));
    }

    /* =========================
     *  Illegal Argument
     * ========================= */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
        log.warn("[잘못된 인자]: {}", ex.getMessage());

        ResultCode code = ResultCode.INVALID_ARGUMENT;
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.failure(code, ex.getMessage()));
    }

    /* =========================
     *  Generic Exception (중요)
     * ========================= */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        String accept = request.getHeader("Accept");

        // API 요청이 아닌 경우 → JSON 응답 금지
        if (accept == null || !accept.contains("application/json")) {
            log.warn(
                "[비 API 요청 예외 차단] method={}, uri={}, accept={}",
                request.getMethod(),
                request.getRequestURI(),
                accept
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // API 요청만 JSON으로 처리
        log.error("[예상치 못한 API 에러]", ex);

        ResultCode code = ResultCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.failure(code, code.defaultMessage()));
    }
}
