package com.gumraze.rallyon.backend.courtManager.dto;

import java.util.UUID;

public record CreateFreeGameAssignmentPreviewJobResponse(
        UUID jobId,
        String status,
        Integer pollAfterMs
) {
}
