package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import java.util.Set;

public record AssignmentPreviewPromptPayload(
        AssignmentPreviewPlanningInput planningInput
) {

    public Set<Long> participantIds() {
        if (planningInput == null || planningInput.participants() == null) {
            return Set.of();
        }

        return planningInput.participants().stream()
                .map(AssignmentPreviewPlanningInput.Participant::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
