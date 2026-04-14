package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobFailureCode;

public class AssignmentPreviewAiInvalidResponseException
        extends IllegalStateException
        implements AssignmentPreviewAiExecutionFailure {

    private final String model;
    private final boolean repairAttempted;
    private final Long initialAiElapsedMs;
    private final Long repairAiElapsedMs;
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
