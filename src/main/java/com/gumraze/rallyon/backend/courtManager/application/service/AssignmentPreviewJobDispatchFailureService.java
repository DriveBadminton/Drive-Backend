package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.out.ManageAssignmentPreviewJobPort;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobFailureCode;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobStatus;
import com.gumraze.rallyon.backend.courtManager.entity.AssignmentPreviewJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentPreviewJobDispatchFailureService {

    private final ManageAssignmentPreviewJobPort manageAssignmentPreviewJobPort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markQueuedJobAsDispatchFailed(UUID jobId) {
        manageAssignmentPreviewJobPort.findById(jobId)
                .filter(job -> job.getStatus() == AssignmentPreviewJobStatus.QUEUED)
                .ifPresent(this::markFailedAndSave);
    }

    private void markFailedAndSave(AssignmentPreviewJob job) {
        LocalDateTime completedAt = LocalDateTime.now();
        long totalElapsedMs = Duration.between(job.getSubmittedAt(), completedAt).toMillis();

        job.markFailed(
                AssignmentPreviewJobFailureCode.SERVICE_UNAVAILABLE,
                "자동 배정 작업을 시작하지 못했어요. 잠시 후 다시 시도해주세요.",
                job.getModel(),
                false,
                null,
                null,
                completedAt,
                0L,
                totalElapsedMs
        );
        manageAssignmentPreviewJobPort.save(job);
    }
}
