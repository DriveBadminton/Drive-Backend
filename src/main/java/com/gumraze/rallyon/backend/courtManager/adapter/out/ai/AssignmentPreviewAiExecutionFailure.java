package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobFailureCode;

public interface AssignmentPreviewAiExecutionFailure {

    AssignmentPreviewJobFailureCode getFailureCode();

    String getModel();

    boolean isRepairAttempted();

    Long getInitialAiElapsedMs();

    Long getRepairAiElapsedMs();

    Integer getPlanningInputChars();

    Integer getPromptChars();

    Integer getResponseChars();

    Integer getMaxCompletionTokens();
}
