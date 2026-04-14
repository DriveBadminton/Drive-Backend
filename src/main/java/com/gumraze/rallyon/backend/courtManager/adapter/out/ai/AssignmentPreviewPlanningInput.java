package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import java.util.List;

public record AssignmentPreviewPlanningInput(
        List<Participant> participants,
        List<Round> rounds,
        List<PartnerPair> partnerPairs,
        Guidance guidance
) {

    public record Participant(
            String clientId,
            Integer gamesAssigned
    ) {
    }

    public record Round(
            Integer roundNumber,
            List<Court> courts
    ) {
    }

    public record Court(
            Integer courtNumber,
            List<Slot> slots
    ) {
    }

    public record Slot(
            String participantId,
            boolean fixed
    ) {
    }

    public record PartnerPair(
            String participantId1,
            String participantId2
    ) {
    }

    public record Guidance(
            boolean preserveFixedSlots,
            boolean fillEmptySlotsOnly,
            boolean preferProvidedPartnerPairs,
            int preferredPairCount
    ) {
    }

}
