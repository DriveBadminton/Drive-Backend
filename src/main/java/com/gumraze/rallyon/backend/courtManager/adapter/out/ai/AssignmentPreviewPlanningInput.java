package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import java.util.List;

public record AssignmentPreviewPlanningInput(
        List<Participant> participants,
        List<Round> rounds,
        List<PartnerPair> partnerPairs,
        Preferences preferences,
        ConstraintGuidance constraintGuidance,
        PolicyGuidance policyGuidance,
        PartnerGuidance partnerGuidance
) {

    public record Participant(
            String id,
            String name,
            String gender,
            Integer ageGroup,
            String grade,
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
            Integer slotIndex,
            String participantId,
            boolean fixed
    ) {
    }

    public record PartnerPair(
            String participantId1,
            String participantId2
    ) {
    }

    public record Preferences(
            String partnerPolicy,
            String existingAssignmentPolicy
    ) {
    }

    public record ConstraintGuidance(
            boolean preserveRoundAndCourtStructure,
            boolean preserveFixedSlots,
            boolean preventSameParticipantDuplicationInRound
    ) {
    }

    public record PolicyGuidance(
            boolean fillEmptySlotsOnly
    ) {
    }

    public record PartnerGuidance(
            boolean preferProvidedPartnerPairs,
            int preferredPairCount
    ) {
    }

}
