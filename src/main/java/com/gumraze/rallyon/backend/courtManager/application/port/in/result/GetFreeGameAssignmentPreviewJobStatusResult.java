package com.gumraze.rallyon.backend.courtManager.application.port.in.result;

import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI 자동 배정 preview job 상태 조회 결과다.
 *
 * @param jobId preview job ID
 * @param status 현재 preview job 상태
 * @param preview 성공 시 preview 결과
 * @param failure 실패 시 오류 정보
 * @param submittedAt 제출 시각
 * @param startedAt 실행 시작 시각
 * @param completedAt 완료 시각
 */
public record GetFreeGameAssignmentPreviewJobStatusResult(
        UUID jobId,
        AssignmentPreviewJobStatus status,
        CreateFreeGameAssignmentPreviewResult preview,
        Failure failure,
        LocalDateTime submittedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
    /**
     * preview job 실패 정보다.
     *
     * @param code 실패 코드
     * @param message 사용자 표시 메시지
     */
    public record Failure(
            String code,
            String message
    ) {
    }
}
