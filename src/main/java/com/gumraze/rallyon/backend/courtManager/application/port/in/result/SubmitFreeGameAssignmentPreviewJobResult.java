package com.gumraze.rallyon.backend.courtManager.application.port.in.result;

import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobStatus;

import java.util.UUID;

/**
 * AI 자동 배정 preview job 제출 결과다.
 *
 * @param jobId 제출된 preview job ID
 * @param status 제출 직후 preview job 상태
 * @param pollAfterMs 권장 polling 간격
 */
public record SubmitFreeGameAssignmentPreviewJobResult(
        UUID jobId,
        AssignmentPreviewJobStatus status,
        int pollAfterMs
) {
}
