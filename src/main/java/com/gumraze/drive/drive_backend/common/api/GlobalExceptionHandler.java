package com.gumraze.drive.drive_backend.common.api;

import com.gumraze.drive.drive_backend.common.exception.ForbiddenException;
import com.gumraze.drive.drive_backend.common.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    // JSON 파싱 실패(예: 잘못된 값)를 400 VALIDATION_ERROR로 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("[요청 파싱 실패]: {}", ex.getMessage());

        ResultCode code = ResultCode.VALIDATION_ERROR;
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.failure(code, ex.getMessage()));
    }

    // 존재하지 않는 리소스를 요청할 경우 404 NOT_FOUND로 처리
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        log.warn("[존재하지 않는 리소스 요청]: {}", ex.getMessage());

        ResultCode code = ResultCode.NOT_FOUND;
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.failure(code, ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException ex) {
        log.warn("[접근 권한 없음]: {}", ex.getMessage());
        ResultCode code = ResultCode.FORBIDDEN;
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.failure(code, ex.getMessage()));
    }


    /* =========================
     *  Generic Exceptions
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
