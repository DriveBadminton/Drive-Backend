package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

public record AssignmentPreviewAiGenerationResult(
        AssignmentPreviewAiResponse response,
        String model,
        boolean repairAttempted,
        Long initialAiElapsedMs,
        Long repairAiElapsedMs,
        boolean emptyResponseRetryAttempted,
        Long emptyResponseRetryElapsedMs,
        Integer qualityRepairAttemptCount,
        Long qualityRepairElapsedMsTotal,
        java.util.List<String> qualityRepairReasons,
        Integer theoreticalMaxFilledSlots,
        Integer actualFilledSlotsAfterInitial,
        Integer bestValidFilledSlots,
        java.util.List<String> bestValidWarningCodes,
        Integer planningInputChars,
        Integer promptChars,
        Integer responseChars,
        Integer maxCompletionTokens
) {
}
