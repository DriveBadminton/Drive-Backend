package com.gumraze.rallyon.backend.courtManager.entity;

import com.gumraze.rallyon.backend.common.persistence.MutableAuditEntity;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobFailureCode;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "assignment_preview_jobs",
        indexes = {
                @Index(name = "idx_assignment_preview_jobs_requester_status", columnList = "requester_account_id,status"),
                @Index(name = "idx_assignment_preview_jobs_submitted_at", columnList = "submitted_at")
        }
)
public class AssignmentPreviewJob extends MutableAuditEntity {

    @Id
    private UUID id;

    @Column(name = "requester_account_id", nullable = false)
    private UUID requesterAccountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AssignmentPreviewJobStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_command_json", nullable = false)
    private CreateFreeGameAssignmentPreviewCommand requestCommand;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_preview_json")
    private CreateFreeGameAssignmentPreviewResult resultPreview;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_code", length = 64)
    private AssignmentPreviewJobFailureCode failureCode;

    @Column(name = "failure_message", length = 255)
    private String failureMessage;

    @Column(nullable = false, length = 64)
    private String model;

    @Column(name = "repair_attempted", nullable = false)
    private boolean repairAttempted;

    @Column(name = "queue_wait_ms")
    private Long queueWaitMs;

    @Column(name = "initial_ai_elapsed_ms")
    private Long initialAiElapsedMs;

    @Column(name = "repair_ai_elapsed_ms")
    private Long repairAiElapsedMs;

    @Column(name = "execution_elapsed_ms")
    private Long executionElapsedMs;

    @Column(name = "total_elapsed_ms")
    private Long totalElapsedMs;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AssignmentPreviewJob() {
    }

    public static AssignmentPreviewJob queue(
            UUID requesterAccountId,
            CreateFreeGameAssignmentPreviewCommand requestCommand,
            String model
    ) {
        AssignmentPreviewJob job = new AssignmentPreviewJob();
        job.id = UUID.randomUUID();
        job.requesterAccountId = requesterAccountId;
        job.status = AssignmentPreviewJobStatus.QUEUED;
        job.requestCommand = requestCommand;
        job.model = model;
        job.submittedAt = LocalDateTime.now();
        job.repairAttempted = false;
        return job;
    }

    public void markRunning(LocalDateTime startedAt, long queueWaitMs) {
        status = AssignmentPreviewJobStatus.RUNNING;
        this.startedAt = startedAt;
        this.queueWaitMs = queueWaitMs;
    }

    public void markSucceeded(
            CreateFreeGameAssignmentPreviewResult resultPreview,
            String model,
            boolean repairAttempted,
            Long initialAiElapsedMs,
            Long repairAiElapsedMs,
            LocalDateTime completedAt,
            long executionElapsedMs,
            long totalElapsedMs
    ) {
        status = AssignmentPreviewJobStatus.SUCCEEDED;
        this.resultPreview = resultPreview;
        this.failureCode = null;
        this.failureMessage = null;
        this.model = model;
        this.repairAttempted = repairAttempted;
        this.initialAiElapsedMs = initialAiElapsedMs;
        this.repairAiElapsedMs = repairAiElapsedMs;
        this.completedAt = completedAt;
        this.executionElapsedMs = executionElapsedMs;
        this.totalElapsedMs = totalElapsedMs;
    }

    public void markFailed(
            AssignmentPreviewJobFailureCode failureCode,
            String failureMessage,
            String model,
            boolean repairAttempted,
            Long initialAiElapsedMs,
            Long repairAiElapsedMs,
            LocalDateTime completedAt,
            long executionElapsedMs,
            long totalElapsedMs
    ) {
        status = AssignmentPreviewJobStatus.FAILED;
        this.resultPreview = null;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.model = model;
        this.repairAttempted = repairAttempted;
        this.initialAiElapsedMs = initialAiElapsedMs;
        this.repairAiElapsedMs = repairAiElapsedMs;
        this.completedAt = completedAt;
        this.executionElapsedMs = executionElapsedMs;
        this.totalElapsedMs = totalElapsedMs;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRequesterAccountId() {
        return requesterAccountId;
    }

    public AssignmentPreviewJobStatus getStatus() {
        return status;
    }

    public CreateFreeGameAssignmentPreviewCommand getRequestCommand() {
        return requestCommand;
    }

    public CreateFreeGameAssignmentPreviewResult getResultPreview() {
        return resultPreview;
    }

    public AssignmentPreviewJobFailureCode getFailureCode() {
        return failureCode;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public String getModel() {
        return model;
    }

    public boolean isRepairAttempted() {
        return repairAttempted;
    }

    public Long getQueueWaitMs() {
        return queueWaitMs;
    }

    public Long getInitialAiElapsedMs() {
        return initialAiElapsedMs;
    }

    public Long getRepairAiElapsedMs() {
        return repairAiElapsedMs;
    }

    public Long getExecutionElapsedMs() {
        return executionElapsedMs;
    }

    public Long getTotalElapsedMs() {
        return totalElapsedMs;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    protected void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    protected void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
