package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

public record AssignmentPreviewAiGenerationResult(
        AssignmentPreviewAiResponse response,
        String model,
        boolean repairAttempted,
        Long initialAiElapsedMs,
        Long repairAiElapsedMs,
        Integer planningInputChars,
        Integer promptChars,
        Integer responseChars,
        Integer maxCompletionTokens
) {
}
