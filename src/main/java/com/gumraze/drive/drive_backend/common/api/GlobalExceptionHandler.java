package com.gumraze.drive.drive_backend.common.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("[잘못된 인자]: {}", ex.getMessage());

        ResultCode code = ResultCode.INVALID_ARGUMENT;
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.failure(code, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("[예상치 못한 에러]", ex);

        ResultCode code = ResultCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.failure(code, ResultCode.INTERNAL_SERVER_ERROR.defaultMessage()));
    }
}
