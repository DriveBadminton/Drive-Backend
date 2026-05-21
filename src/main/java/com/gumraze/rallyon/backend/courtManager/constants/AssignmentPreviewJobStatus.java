package com.gumraze.rallyon.backend.courtManager.constants;

/**
 * AI 자동 배정 preview job의 처리 상태다.
 */
public enum AssignmentPreviewJobStatus {
    QUEUED,
    RUNNING,
    SUCCEEDED,
    FAILED;

    public boolean isActive() {
        return this == QUEUED || this == RUNNING;
    }
}
