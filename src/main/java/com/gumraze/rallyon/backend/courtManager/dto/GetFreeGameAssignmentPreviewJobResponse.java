package com.gumraze.rallyon.backend.courtManager.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetFreeGameAssignmentPreviewJobResponse(
        UUID jobId,
        String status,
        CreateFreeGameAssignmentPreviewResponse preview,
        FailureResponse failure,
        LocalDateTime submittedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
    public record FailureResponse(
            String code,
            String message
    ) {
    }
}
