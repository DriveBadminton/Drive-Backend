package com.gumraze.rallyon.backend.courtManager.constants;

/**
 * AI 자동 배정 preview job의 실패 사유 코드다.
 */
public enum AssignmentPreviewJobFailureCode {
    TIMEOUT,
    INVALID_OUTPUT,
    SERVICE_UNAVAILABLE,
    UNEXPECTED_ERROR,
    WORKER_RESTARTED
}
