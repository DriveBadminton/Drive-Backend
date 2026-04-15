package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.ServiceUnavailableException;
import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.AssignmentPreviewAiExecutionFailure;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;
import com.gumraze.rallyon.backend.courtManager.application.port.out.GenerateFreeGameAssignmentPreviewExecutionPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.ManageAssignmentPreviewJobPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.result.FreeGameAssignmentPreviewGeneration;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobFailureCode;
import com.gumraze.rallyon.backend.courtManager.entity.AssignmentPreviewJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessFreeGameAssignmentPreviewJobService {

    private final ManageAssignmentPreviewJobPort manageAssignmentPreviewJobPort;
    private final GenerateFreeGameAssignmentPreviewExecutionPort generateFreeGameAssignmentPreviewExecutionPort;

    @Async("assignmentPreviewExecutor")
    public void processAsync(UUID jobId) {
        process(jobId);
    }

    void process(UUID jobId) {
        AssignmentPreviewJob queuedJob = manageAssignmentPreviewJobPort.findById(jobId).orElse(null);
        if (queuedJob == null) {
            log.warn("[ASSIGNMENT_PREVIEW_JOB] event=MISSING jobId={}", jobId);
            return;
        }

        LocalDateTime startedAt = LocalDateTime.now();
        long queueWaitMs = Duration.between(queuedJob.getSubmittedAt(), startedAt).toMillis();
        queuedJob.markRunning(startedAt, queueWaitMs);
        AssignmentPreviewJob runningJob = manageAssignmentPreviewJobPort.save(queuedJob);
        logStarted(runningJob);

        long executionStartNanos = System.nanoTime();

        try {
            FreeGameAssignmentPreviewGeneration generation =
                    generateFreeGameAssignmentPreviewExecutionPort.generateExecution(
                            runningJob.getRequestCommand()
                    );

            LocalDateTime completedAt = LocalDateTime.now();
            long executionElapsedMs = elapsedMillis(executionStartNanos);
            long totalElapsedMs = Duration.between(runningJob.getSubmittedAt(), completedAt).toMillis();

            runningJob.markSucceeded(
                    generation.preview(),
                    generation.model(),
                    generation.repairAttempted(),
                    generation.initialAiElapsedMs(),
                    generation.repairAiElapsedMs(),
                    completedAt,
                    executionElapsedMs,
                    totalElapsedMs
            );
            AssignmentPreviewJob succeededJob = manageAssignmentPreviewJobPort.save(runningJob);
            logAttempts(succeededJob);
            logSucceeded(succeededJob, generation);
        } catch (RuntimeException ex) {
            FailureMetadata failureMetadata = failureMetadata(ex, runningJob.getModel());
            LocalDateTime completedAt = LocalDateTime.now();
            long executionElapsedMs = elapsedMillis(executionStartNanos);
            long totalElapsedMs = Duration.between(runningJob.getSubmittedAt(), completedAt).toMillis();

            runningJob.markFailed(
                    failureMetadata.failureCode(),
                    failureMessage(ex, failureMetadata.failureCode()),
                    failureMetadata.model(),
                    failureMetadata.repairAttempted(),
                    failureMetadata.initialAiElapsedMs(),
                    failureMetadata.repairAiElapsedMs(),
                    completedAt,
                    executionElapsedMs,
                    totalElapsedMs
            );
            AssignmentPreviewJob failedJob = manageAssignmentPreviewJobPort.save(runningJob);
            logAttempts(failedJob);
            logFailed(failedJob, ex, failureMetadata);
        }
    }

    private void logStarted(AssignmentPreviewJob job) {
        CreateFreeGameAssignmentPreviewCommand command = job.getRequestCommand();
        log.info(
                "[ASSIGNMENT_PREVIEW_JOB] event=STARTED jobId={} accountId={} model={} participantCount={} roundCount={} courtCount={} filledSlotCount={} partnerPairCount={} partnerPolicy={} existingAssignmentPolicy={} queueWaitMs={} retryEnabled={} qualityRepairAttemptCount={} qualityRepairReasons={} theoreticalMaxFilledSlots={} actualFilledSlotsAfterInitial={} bestValidFilledSlots={} bestValidWarningCodes={}",
                job.getId(),
                job.getRequesterAccountId(),
                job.getModel(),
                command.participants().size(),
                command.rounds().size(),
                maxCourtCount(command),
                filledSlotCount(command),
                command.partnerPairs().size(),
                command.preferences().partnerPolicy(),
                command.preferences().existingAssignmentPolicy(),
                job.getQueueWaitMs(),
                false,
                0,
                List.of(),
                theoreticalMaxFilledSlots(command),
                null,
                null,
                List.of()
        );
    }

    private void logAttempts(AssignmentPreviewJob job) {
        if (job.getInitialAiElapsedMs() != null) {
            log.info(
                    "[ASSIGNMENT_PREVIEW_JOB] event=AI_ATTEMPT_FINISHED jobId={} accountId={} attempt=initial model={} elapsedMs={} repairAttempted={}",
                    job.getId(),
                    job.getRequesterAccountId(),
                    job.getModel(),
                    job.getInitialAiElapsedMs(),
                    job.isRepairAttempted()
            );
        }

        if (job.isRepairAttempted() && job.getRepairAiElapsedMs() != null) {
            log.info(
                    "[ASSIGNMENT_PREVIEW_JOB] event=AI_ATTEMPT_FINISHED jobId={} accountId={} attempt=repair model={} elapsedMs={} repairAttempted={}",
                    job.getId(),
                    job.getRequesterAccountId(),
                    job.getModel(),
                    job.getRepairAiElapsedMs(),
                    true
            );
        }
    }

    private void logSucceeded(
            AssignmentPreviewJob job,
            FreeGameAssignmentPreviewGeneration generation
    ) {
        List<String> warningCodes = job.getResultPreview() == null
                ? List.of()
                : job.getResultPreview().warnings().stream()
                        .map(CreateFreeGameAssignmentPreviewResult.Warning::code)
                        .toList();

        log.info(
                "[ASSIGNMENT_PREVIEW_JOB] event=SUCCEEDED jobId={} accountId={} model={} repairAttempted={} queueWaitMs={} initialAiElapsedMs={} repairAiElapsedMs={} executionElapsedMs={} totalElapsedMs={} planningInputChars={} promptChars={} responseChars={} maxCompletionTokens={} emptyResponseRetryAttempted={} emptyResponseRetryElapsedMs={} qualityRepairAttemptCount={} qualityRepairElapsedMsTotal={} qualityRepairReasons={} theoreticalMaxFilledSlots={} actualFilledSlotsAfterInitial={} bestValidFilledSlots={} bestValidWarningCodes={} warningCodes={}",
                job.getId(),
                job.getRequesterAccountId(),
                job.getModel(),
                job.isRepairAttempted(),
                job.getQueueWaitMs(),
                job.getInitialAiElapsedMs(),
                job.getRepairAiElapsedMs(),
                job.getExecutionElapsedMs(),
                job.getTotalElapsedMs(),
                generation.planningInputChars(),
                generation.promptChars(),
                generation.responseChars(),
                generation.maxCompletionTokens(),
                generation.emptyResponseRetryAttempted(),
                generation.emptyResponseRetryElapsedMs(),
                generation.qualityRepairAttemptCount(),
                generation.qualityRepairElapsedMsTotal(),
                generation.qualityRepairReasons(),
                generation.theoreticalMaxFilledSlots(),
                generation.actualFilledSlotsAfterInitial(),
                generation.bestValidFilledSlots(),
                generation.bestValidWarningCodes(),
                warningCodes
        );
    }

    private void logFailed(
            AssignmentPreviewJob job,
            RuntimeException ex,
            FailureMetadata failureMetadata
    ) {
        log.warn(
                "[ASSIGNMENT_PREVIEW_JOB] event=FAILED jobId={} accountId={} model={} repairAttempted={} queueWaitMs={} initialAiElapsedMs={} repairAiElapsedMs={} executionElapsedMs={} totalElapsedMs={} planningInputChars={} promptChars={} responseChars={} maxCompletionTokens={} emptyResponseRetryAttempted={} emptyResponseRetryElapsedMs={} qualityRepairAttemptCount={} qualityRepairElapsedMsTotal={} qualityRepairReasons={} theoreticalMaxFilledSlots={} actualFilledSlotsAfterInitial={} bestValidFilledSlots={} bestValidWarningCodes={} failureCode={} exceptionClass={}",
                job.getId(),
                job.getRequesterAccountId(),
                job.getModel(),
                job.isRepairAttempted(),
                job.getQueueWaitMs(),
                job.getInitialAiElapsedMs(),
                job.getRepairAiElapsedMs(),
                job.getExecutionElapsedMs(),
                job.getTotalElapsedMs(),
                failureMetadata.planningInputChars(),
                failureMetadata.promptChars(),
                failureMetadata.responseChars(),
                failureMetadata.maxCompletionTokens(),
                failureMetadata.emptyResponseRetryAttempted(),
                failureMetadata.emptyResponseRetryElapsedMs(),
                failureMetadata.qualityRepairAttemptCount(),
                failureMetadata.qualityRepairElapsedMsTotal(),
                failureMetadata.qualityRepairReasons(),
                failureMetadata.theoreticalMaxFilledSlots(),
                failureMetadata.actualFilledSlotsAfterInitial(),
                failureMetadata.bestValidFilledSlots(),
                failureMetadata.bestValidWarningCodes(),
                failureMetadata.failureCode(),
                ex.getClass().getSimpleName()
        );
    }

    private FailureMetadata failureMetadata(RuntimeException ex, String fallbackModel) {
        if (ex instanceof AssignmentPreviewAiExecutionFailure failure) {
            return new FailureMetadata(
                    failure.getFailureCode(),
                    failure.getModel(),
                    failure.isRepairAttempted(),
                    failure.getInitialAiElapsedMs(),
                    failure.getRepairAiElapsedMs(),
                    failure.isEmptyResponseRetryAttempted(),
                    failure.getEmptyResponseRetryElapsedMs(),
                    failure.getQualityRepairAttemptCount(),
                    failure.getQualityRepairElapsedMsTotal(),
                    failure.getQualityRepairReasons(),
                    failure.getTheoreticalMaxFilledSlots(),
                    failure.getActualFilledSlotsAfterInitial(),
                    failure.getBestValidFilledSlots(),
                    failure.getBestValidWarningCodes(),
                    failure.getPlanningInputChars(),
                    failure.getPromptChars(),
                    failure.getResponseChars(),
                    failure.getMaxCompletionTokens()
            );
        }

        if (ex instanceof ServiceUnavailableException serviceUnavailableException) {
            AssignmentPreviewJobFailureCode failureCode =
                    serviceUnavailableException.getMessage() != null
                            && serviceUnavailableException.getMessage().contains("시간이 초과")
                            ? AssignmentPreviewJobFailureCode.TIMEOUT
                            : AssignmentPreviewJobFailureCode.SERVICE_UNAVAILABLE;
            return new FailureMetadata(
                    failureCode,
                    fallbackModel,
                    false,
                    null,
                    null,
                    false,
                    null,
                    null,
                    null,
                    List.of(),
                    null,
                    null,
                    null,
                    List.of(),
                    null,
                    null,
                    null,
                    null
            );
        }

        if (ex instanceof IllegalStateException) {
            return new FailureMetadata(
                    AssignmentPreviewJobFailureCode.INVALID_OUTPUT,
                    fallbackModel,
                    false,
                    null,
                    null,
                    false,
                    null,
                    null,
                    null,
                    List.of(),
                    null,
                    null,
                    null,
                    List.of(),
                    null,
                    null,
                    null,
                    null
            );
        }

        return new FailureMetadata(
                AssignmentPreviewJobFailureCode.UNEXPECTED_ERROR,
                fallbackModel,
                false,
                null,
                null,
                false,
                null,
                null,
                null,
                List.of(),
                null,
                null,
                null,
                List.of(),
                null,
                null,
                null,
                null
        );
    }

    private String failureMessage(RuntimeException ex, AssignmentPreviewJobFailureCode failureCode) {
        return switch (failureCode) {
            case TIMEOUT -> "AI 자동 배정 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.";
            case INVALID_OUTPUT -> "AI 자동 배정 결과를 처리하지 못했어요. 다시 시도해주세요.";
            case SERVICE_UNAVAILABLE -> ex.getMessage() == null || ex.getMessage().isBlank()
                    ? "AI 코트 배정 프리뷰를 현재 생성할 수 없습니다."
                    : ex.getMessage();
            case WORKER_RESTARTED -> "서버가 재시작되어 자동 배정 작업을 다시 요청해주세요.";
            case UNEXPECTED_ERROR -> "자동 배정을 완료하지 못했어요. 잠시 후 다시 시도해주세요.";
        };
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
                .filter(Objects::nonNull)
                .count();
    }

    private int theoreticalMaxFilledSlots(CreateFreeGameAssignmentPreviewCommand command) {
        int participantCount = command.participants().size();
        int total = 0;
        for (CreateFreeGameAssignmentPreviewCommand.Round round : command.rounds()) {
            int requestedFilled = 0;
            int requestedNull = 0;
            for (CreateFreeGameAssignmentPreviewCommand.Court court : round.courts()) {
                for (String slot : court.slots()) {
                    if (slot == null) {
                        requestedNull++;
                    } else {
                        requestedFilled++;
                    }
                }
            }
            total += requestedFilled + Math.min(requestedNull, Math.max(participantCount - requestedFilled, 0));
        }
        return total;
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private record FailureMetadata(
            AssignmentPreviewJobFailureCode failureCode,
            String model,
            boolean repairAttempted,
            Long initialAiElapsedMs,
            Long repairAiElapsedMs,
            boolean emptyResponseRetryAttempted,
            Long emptyResponseRetryElapsedMs,
            Integer qualityRepairAttemptCount,
            Long qualityRepairElapsedMsTotal,
            List<String> qualityRepairReasons,
            Integer theoreticalMaxFilledSlots,
            Integer actualFilledSlotsAfterInitial,
            Integer bestValidFilledSlots,
            List<String> bestValidWarningCodes,
            Integer planningInputChars,
            Integer promptChars,
            Integer responseChars,
            Integer maxCompletionTokens
    ) {
    }
}
