package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import java.util.List;

public record AssignmentPreviewPlanningInput(
        List<Participant> participants,
        List<Round> rounds,
        List<PartnerPair> partnerPairs,
        Guidance guidance
) {

    public record Participant(
            Long id,
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
            Long participantId,
            boolean fixed
    ) {
    }

    public record PartnerPair(
            Long participantId1,
            Long participantId2
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
