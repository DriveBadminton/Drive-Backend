package com.gumraze.rallyon.backend.common.api;

import com.gumraze.rallyon.backend.common.exception.ConflictException;
import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.common.exception.ServiceUnavailableException;
import com.gumraze.rallyon.backend.common.exception.UnauthorizedException;
import com.gumraze.rallyon.backend.common.exception.UnprocessableEntityException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* =========================
     *  Validation Exception
     * ========================= */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        String message = Optional.ofNullable(ex.getBindingResult().getFieldError())
                .map(err -> String.format("%s: %s", err.getField(), err.getDefaultMessage()))
                .orElse("요청 값이 올바르지 않습니다.");

        log.warn("[검증 실패]: {}", message);

        ProblemDetail problem = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "/problems/validation-error",
                "요청 값이 올바르지 않습니다.",
                message,
                request
        );

        List<LinkedHashMap<String, String>> invalidParams = fieldErrors.stream()
                .map(error -> {
                    LinkedHashMap<String, String> entry = new LinkedHashMap<>();
                    entry.put("name", error.getField());
                    entry.put("reason", error.getDefaultMessage());
                    return entry;
                })
                .toList();
        problem.setProperty("invalidParams", invalidParams);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    /* =========================
     *  Illegal Argument
     * ========================= */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("[잘못된 인자]: {}", ex.getMessage());

        return buildProblemDetailResponse(
                HttpStatus.BAD_REQUEST,
                "/problems/invalid-argument",
                "잘못된 요청입니다.",
                ex.getMessage(),
                request
        );
    }

    // JSON 파싱 실패(예: 잘못된 값)를 400 BAD_REQUEST로 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn("[요청 파싱 실패]: {}", ex.getMessage());

        return buildProblemDetailResponse(
                HttpStatus.BAD_REQUEST,
                "/problems/invalid-request",
                "요청 본문을 읽을 수 없습니다.",
                "요청 본문을 읽을 수 없습니다.",
                request
        );
    }

    // 존재하지 않는 리소스를 요청할 경우 404 NOT_FOUND로 처리
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(
            NotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("[존재하지 않는 리소스 요청]: {}", ex.getMessage());

        return buildProblemDetailResponse(
                HttpStatus.NOT_FOUND,
                "/problems/not-found",
                "리소스를 찾을 수 없습니다.",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ProblemDetail> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request
    ) {
        log.warn("[접근 권한 없음]: {}", ex.getMessage());

        return buildProblemDetailResponse(
                HttpStatus.FORBIDDEN,
                "/problems/forbidden",
                "접근 권한이 없습니다.",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        log.warn("[인증 실패]: {}", ex.getMessage());

        return buildProblemDetailResponse(
                HttpStatus.UNAUTHORIZED,
                "/problems/unauthorized",
                "인증에 실패했습니다.",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParam(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        log.warn("[필수 파라미터 누락]: {}", ex.getParameterName());

        return buildProblemDetailResponse(
                HttpStatus.BAD_REQUEST,
                "/problems/missing-parameter",
                "필수 파라미터 누락",
                String.format("필수 파라미터 누락: %s", ex.getParameterName()),
                request
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        log.warn("[리소스 충돌]: {}", ex.getMessage());

        return buildProblemDetailResponse(
                HttpStatus.CONFLICT,
                "/problems/conflict",
                "요청이 현재 상태와 충돌합니다.",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ProblemDetail> handleUnprocessableEntity(
            UnprocessableEntityException ex,
            HttpServletRequest request
    ) {
        log.warn("[비즈니스 규칙 위반]: {}", ex.getMessage());

        return buildProblemDetailResponse(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "/problems/unprocessable-entity",
                "요청을 처리할 수 없습니다.",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleServiceUnavailable(
            ServiceUnavailableException ex,
            HttpServletRequest request
    ) {
        log.warn("[서비스 일시 사용 불가]: {}", ex.getMessage());

        return buildProblemDetailResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "/problems/service-unavailable",
                "서비스를 현재 사용할 수 없습니다.",
                ex.getMessage(),
                request
        );
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
        if (accept == null || (!accept.contains("application/json")
                && !accept.contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE))) {
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

        return buildProblemDetailResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "/problems/internal-server-error",
                "서버 오류가 발생했습니다.",
                "서버 오류가 발생했습니다.",
                request
        );
    }

    private ResponseEntity<ProblemDetail> buildProblemDetailResponse(
            HttpStatus status,
            String type,
            String title,
            String detail,
            HttpServletRequest request
    ) {
        ProblemDetail problem = buildProblemDetail(status, type, title, detail, request);
        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private ProblemDetail buildProblemDetail(
            HttpStatus status,
            String type,
            String title,
            String detail,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(URI.create(type));
        problem.setTitle(title);
        if (detail != null && !detail.isBlank()) {
            problem.setDetail(detail);
        }
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }
        return problem;
    }
}
