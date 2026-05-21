package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobFailureCode;

public interface AssignmentPreviewAiExecutionFailure {

    AssignmentPreviewJobFailureCode getFailureCode();

    String getModel();

    boolean isRepairAttempted();

    Long getInitialAiElapsedMs();

    Long getRepairAiElapsedMs();

    boolean isEmptyResponseRetryAttempted();

    Long getEmptyResponseRetryElapsedMs();

    Integer getQualityRepairAttemptCount();

    Long getQualityRepairElapsedMsTotal();

    java.util.List<String> getQualityRepairReasons();

    Integer getTheoreticalMaxFilledSlots();

    Integer getActualFilledSlotsAfterInitial();

    Integer getBestValidFilledSlots();

    java.util.List<String> getBestValidWarningCodes();

    Integer getPlanningInputChars();

    Integer getPromptChars();

    Integer getResponseChars();

    Integer getMaxCompletionTokens();
}
