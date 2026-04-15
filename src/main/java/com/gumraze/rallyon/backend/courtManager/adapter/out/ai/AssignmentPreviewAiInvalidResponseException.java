package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobFailureCode;

import java.util.List;

public class AssignmentPreviewAiInvalidResponseException
        extends IllegalStateException
        implements AssignmentPreviewAiExecutionFailure {

    private final String model;
    private final boolean repairAttempted;
    private final Long initialAiElapsedMs;
    private final Long repairAiElapsedMs;
    private final boolean emptyResponseRetryAttempted;
    private final Long emptyResponseRetryElapsedMs;
    private final Integer qualityRepairAttemptCount;
    private final Long qualityRepairElapsedMsTotal;
    private final List<String> qualityRepairReasons;
    private final Integer theoreticalMaxFilledSlots;
    private final Integer actualFilledSlotsAfterInitial;
    private final Integer bestValidFilledSlots;
    private final List<String> bestValidWarningCodes;
    private final Integer planningInputChars;
    private final Integer promptChars;
    private final Integer responseChars;
    private final Integer maxCompletionTokens;

    public AssignmentPreviewAiInvalidResponseException(
            String message,
            Throwable cause,
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
        super(message, cause);
        this.model = model;
        this.repairAttempted = repairAttempted;
        this.initialAiElapsedMs = initialAiElapsedMs;
        this.repairAiElapsedMs = repairAiElapsedMs;
        this.emptyResponseRetryAttempted = emptyResponseRetryAttempted;
        this.emptyResponseRetryElapsedMs = emptyResponseRetryElapsedMs;
        this.qualityRepairAttemptCount = qualityRepairAttemptCount;
        this.qualityRepairElapsedMsTotal = qualityRepairElapsedMsTotal;
        this.qualityRepairReasons = qualityRepairReasons == null ? List.of() : List.copyOf(qualityRepairReasons);
        this.theoreticalMaxFilledSlots = theoreticalMaxFilledSlots;
        this.actualFilledSlotsAfterInitial = actualFilledSlotsAfterInitial;
        this.bestValidFilledSlots = bestValidFilledSlots;
        this.bestValidWarningCodes = bestValidWarningCodes == null ? List.of() : List.copyOf(bestValidWarningCodes);
        this.planningInputChars = planningInputChars;
        this.promptChars = promptChars;
        this.responseChars = responseChars;
        this.maxCompletionTokens = maxCompletionTokens;
    }

    @Override
    public AssignmentPreviewJobFailureCode getFailureCode() {
        return AssignmentPreviewJobFailureCode.INVALID_OUTPUT;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public boolean isRepairAttempted() {
        return repairAttempted;
    }

    @Override
    public Long getInitialAiElapsedMs() {
        return initialAiElapsedMs;
    }

    @Override
    public Long getRepairAiElapsedMs() {
        return repairAiElapsedMs;
    }

    @Override
    public boolean isEmptyResponseRetryAttempted() {
        return emptyResponseRetryAttempted;
    }

    @Override
    public Long getEmptyResponseRetryElapsedMs() {
        return emptyResponseRetryElapsedMs;
    }

    @Override
    public Integer getQualityRepairAttemptCount() {
        return qualityRepairAttemptCount;
    }

    @Override
    public Long getQualityRepairElapsedMsTotal() {
        return qualityRepairElapsedMsTotal;
    }

    @Override
    public List<String> getQualityRepairReasons() {
        return qualityRepairReasons;
    }

    @Override
    public Integer getTheoreticalMaxFilledSlots() {
        return theoreticalMaxFilledSlots;
    }

    @Override
    public Integer getActualFilledSlotsAfterInitial() {
        return actualFilledSlotsAfterInitial;
    }

    @Override
    public Integer getBestValidFilledSlots() {
        return bestValidFilledSlots;
    }

    @Override
    public List<String> getBestValidWarningCodes() {
        return bestValidWarningCodes;
    }

    @Override
    public Integer getPlanningInputChars() {
        return planningInputChars;
    }

    @Override
    public Integer getPromptChars() {
        return promptChars;
    }

    @Override
    public Integer getResponseChars() {
        return responseChars;
    }

    @Override
    public Integer getMaxCompletionTokens() {
        return maxCompletionTokens;
    }
}
