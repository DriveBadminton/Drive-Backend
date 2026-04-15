package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public record AssignmentPreviewPromptPayload(
        AssignmentPreviewPlanningInput planningInput,
        Map<String, Long> compactIdByClientId,
        Map<Long, String> clientIdByCompactId
) {

    public AssignmentPreviewPromptPayload {
        compactIdByClientId = Map.copyOf(new LinkedHashMap<>(compactIdByClientId));
        clientIdByCompactId = Map.copyOf(new LinkedHashMap<>(clientIdByCompactId));
    }

    public Long toCompactId(String clientId) {
        return compactIdByClientId.get(clientId);
    }

    public String toClientId(Long compactId) {
        return clientIdByCompactId.get(compactId);
    }

    public Set<Long> compactParticipantIds() {
        return clientIdByCompactId.keySet();
    }
}
