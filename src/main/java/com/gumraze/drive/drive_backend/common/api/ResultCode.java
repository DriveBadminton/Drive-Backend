package com.gumraze.drive.drive_backend.common.api;

import org.springframework.http.HttpStatus;

public enum ResultCode {

    /* ===== Success ===== */
    OK(HttpStatus.OK, "요청 성공"),
    CREATED(HttpStatus.CREATED, "생성 성공"),
    OAUTH_LOGIN_SUCCESS(HttpStatus.CREATED, "OAuth 로그인 성공"),
    OAUTH_REFRESH_TOKEN_SUCCESS(HttpStatus.OK, "액세스 토큰 리프레시 성공"),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 성공"),

    /* ===== Client Error ===== */
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),

    /* ===== Server Error ===== */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ResultCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String defaultMessage() {
        return defaultMessage;
    }

    public boolean isSuccess() {
        return httpStatus.is2xxSuccessful();
    }
}
