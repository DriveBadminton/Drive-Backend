package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.common.exception.ServiceUnavailableException;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobFailureCode;

public class AssignmentPreviewAiServiceUnavailableException
        extends ServiceUnavailableException
        implements AssignmentPreviewAiExecutionFailure {

    private final AssignmentPreviewJobFailureCode failureCode;
    private final String model;
    private final boolean repairAttempted;
    private final Long initialAiElapsedMs;
    private final Long repairAiElapsedMs;
    private final Integer planningInputChars;
    private final Integer promptChars;
    private final Integer responseChars;
    private final Integer maxCompletionTokens;

    public AssignmentPreviewAiServiceUnavailableException(
            String message,
            Throwable cause,
            AssignmentPreviewJobFailureCode failureCode,
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
        this.failureCode = failureCode;
        this.model = model;
        this.repairAttempted = repairAttempted;
        this.initialAiElapsedMs = initialAiElapsedMs;
        this.repairAiElapsedMs = repairAiElapsedMs;
        this.planningInputChars = planningInputChars;
        this.promptChars = promptChars;
        this.responseChars = responseChars;
        this.maxCompletionTokens = maxCompletionTokens;
    }

    public AssignmentPreviewAiServiceUnavailableException(
            String message,
            AssignmentPreviewJobFailureCode failureCode,
            String model,
            boolean repairAttempted,
            Long initialAiElapsedMs,
            Long repairAiElapsedMs,
            Integer planningInputChars,
            Integer promptChars,
            Integer responseChars,
            Integer maxCompletionTokens
    ) {
        super(message);
        this.failureCode = failureCode;
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
        return failureCode;
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
