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

    /**
     * Handles validation failures from controller method arguments and maps them to a standardized error response.
     *
     * @param ex the MethodArgumentNotValidException containing binding results and field validation errors
     * @return a ResponseEntity containing a failure ApiResponse<Void> with the validation ResultCode and a message formatted as "<field>: <message>" when a field error exists, or the default validation message otherwise
     */
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

    /**
     * Handle an IllegalArgumentException by returning a failure response indicating an invalid argument.
     *
     * @return a ResponseEntity containing a failure ApiResponse with the `INVALID_ARGUMENT` result code and the exception's message; the HTTP status is taken from that result code
     */
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

    /**
     * Handle uncaught exceptions and map them to HTTP responses based on the request's Accept header.
     *
     * <p>If the request's Accept header is absent or does not include "application/json", this method
     * blocks JSON responses and returns a 400 Bad Request with no body. For requests that accept JSON,
     * it returns an error ApiResponse with the INTERNAL_SERVER_ERROR status.</p>
     *
     * @param ex the uncaught exception
     * @param request the HTTP request; the Accept header is inspected to decide whether to return JSON
     * @return a ResponseEntity: 400 Bad Request with no body for non-JSON requests, or a response
     *         with the INTERNAL_SERVER_ERROR HTTP status and a failure ApiResponse body for JSON requests
     */
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