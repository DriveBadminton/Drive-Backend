package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.ConflictException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetFreeGameAssignmentPreviewStatusUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.SubmitFreeGameAssignmentPreviewUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.GetFreeGameAssignmentPreviewJobStatusResult;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.SubmitFreeGameAssignmentPreviewJobResult;
import com.gumraze.rallyon.backend.courtManager.application.port.out.ManageAssignmentPreviewJobPort;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobFailureCode;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobStatus;
import com.gumraze.rallyon.backend.courtManager.entity.AssignmentPreviewJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentPreviewJobService implements
        SubmitFreeGameAssignmentPreviewUseCase,
        GetFreeGameAssignmentPreviewStatusUseCase {

    private static final int DEFAULT_POLL_AFTER_MS = 1000;
    private static final List<AssignmentPreviewJobStatus> ACTIVE_JOB_STATUSES = List.of(
            AssignmentPreviewJobStatus.QUEUED,
            AssignmentPreviewJobStatus.RUNNING
    );

    private final ManageAssignmentPreviewJobPort manageAssignmentPreviewJobPort;
    private final ProcessFreeGameAssignmentPreviewJobService processFreeGameAssignmentPreviewJobService;
    private final AssignmentPreviewJobDispatchFailureService assignmentPreviewJobDispatchFailureService;

    @Value("${spring.ai.openai.chat.options.model:gpt-5-mini}")
    private String assignmentPreviewModel = "gpt-5-mini";

    @Override
    @Transactional
    public SubmitFreeGameAssignmentPreviewJobResult submit(
            UUID accountId,
            CreateFreeGameAssignmentPreviewCommand command
    ) {
        if (manageAssignmentPreviewJobPort.existsByRequesterAccountIdAndStatusIn(
                accountId,
                ACTIVE_JOB_STATUSES
        )) {
            throw new ConflictException("이미 자동 배정이 진행 중이에요. 잠시만 기다려주세요.");
        }

        AssignmentPreviewJob job;
        try {
            job = manageAssignmentPreviewJobPort.save(
                    AssignmentPreviewJob.queue(accountId, command, assignmentPreviewModel)
            );
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("이미 자동 배정이 진행 중이에요. 잠시만 기다려주세요.");
        }

        logSubmitted(job);
        UUID jobId = job.getId();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    dispatchProcessing(jobId);
                }
            });
        } else {
            dispatchProcessing(jobId);
        }
        return new SubmitFreeGameAssignmentPreviewJobResult(
                jobId,
                job.getStatus(),
                DEFAULT_POLL_AFTER_MS
        );
    }

    @Override
    public GetFreeGameAssignmentPreviewJobStatusResult getStatus(UUID accountId, UUID jobId) {
        AssignmentPreviewJob job = manageAssignmentPreviewJobPort.findByIdAndRequesterAccountId(jobId, accountId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 자동 배정 작업입니다. jobId: " + jobId));

        return new GetFreeGameAssignmentPreviewJobStatusResult(
                job.getId(),
                job.getStatus(),
                job.getResultPreview(),
                job.getFailureCode() == null ? null : new GetFreeGameAssignmentPreviewJobStatusResult.Failure(
                        job.getFailureCode().name(),
                        job.getFailureMessage()
                ),
                job.getSubmittedAt(),
                job.getStartedAt(),
                job.getCompletedAt()
        );
    }

    @Transactional
    public int recoverInterruptedJobs() {
        List<AssignmentPreviewJob> activeJobs =
                manageAssignmentPreviewJobPort.findAllByStatusIn(ACTIVE_JOB_STATUSES);
        LocalDateTime completedAt = LocalDateTime.now();

        for (AssignmentPreviewJob job : activeJobs) {
            long executionElapsedMs = job.getStartedAt() == null
                    ? 0L
                    : java.time.Duration.between(job.getStartedAt(), completedAt).toMillis();
            long totalElapsedMs = java.time.Duration.between(job.getSubmittedAt(), completedAt).toMillis();

            job.markFailed(
                    AssignmentPreviewJobFailureCode.WORKER_RESTARTED,
                    "서버가 재시작되어 자동 배정 작업을 다시 요청해주세요.",
                    job.getModel(),
                    job.isRepairAttempted(),
                    job.getInitialAiElapsedMs(),
                    job.getRepairAiElapsedMs(),
                    completedAt,
                    executionElapsedMs,
                    totalElapsedMs
            );
            manageAssignmentPreviewJobPort.save(job);
        }

        if (!activeJobs.isEmpty()) {
            log.info(
                    "[ASSIGNMENT_PREVIEW_JOB] event=RECOVERED interruptedJobCount={}",
                    activeJobs.size()
            );
        }
        return activeJobs.size();
    }

    private void logSubmitted(AssignmentPreviewJob job) {
        CreateFreeGameAssignmentPreviewCommand command = job.getRequestCommand();
        log.info(
                "[ASSIGNMENT_PREVIEW_JOB] event=SUBMITTED jobId={} accountId={} model={} participantCount={} roundCount={} courtCount={} filledSlotCount={} partnerPairCount={} partnerPolicy={} existingAssignmentPolicy={}",
                job.getId(),
                job.getRequesterAccountId(),
                job.getModel(),
                command.participants().size(),
                command.rounds().size(),
                maxCourtCount(command),
                filledSlotCount(command),
                command.partnerPairs().size(),
                command.preferences().partnerPolicy(),
                command.preferences().existingAssignmentPolicy()
        );
    }

    private int maxCourtCount(CreateFreeGameAssignmentPreviewCommand command) {
        return command.rounds().stream()
                .mapToInt(round -> round.courts().size())
                .max()
                .orElse(0);
    }

    private long filledSlotCount(CreateFreeGameAssignmentPreviewCommand command) {
        return command.rounds().stream()
                .flatMap(round -> round.courts().stream())
                .flatMap(court -> court.slots().stream())
                .filter(java.util.Objects::nonNull)
                .count();
    }

    private void dispatchProcessing(UUID jobId) {
        try {
            processFreeGameAssignmentPreviewJobService.processAsync(jobId);
        } catch (RuntimeException ex) {
            assignmentPreviewJobDispatchFailureService.markQueuedJobAsDispatchFailed(jobId);
            logDispatchFailed(jobId, ex);
        }
    }

    private void logDispatchFailed(UUID jobId, RuntimeException ex) {
        manageAssignmentPreviewJobPort.findById(jobId).ifPresent(job -> log.warn(
                "[ASSIGNMENT_PREVIEW_JOB] event=DISPATCH_FAILED jobId={} accountId={} model={} failureCode={} exceptionClass={} totalElapsedMs={}",
                job.getId(),
                job.getRequesterAccountId(),
                job.getModel(),
                AssignmentPreviewJobFailureCode.SERVICE_UNAVAILABLE,
                ex.getClass().getSimpleName(),
                job.getTotalElapsedMs()
        ));
    }
}
