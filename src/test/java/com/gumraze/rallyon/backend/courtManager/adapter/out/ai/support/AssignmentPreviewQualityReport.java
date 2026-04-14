package com.gumraze.rallyon.backend.courtManager.adapter.out.ai.support;

import java.util.List;

public record AssignmentPreviewQualityReport(
        int filledSlotsBefore,
        int filledSlotsAfter,
        int filledSlotDelta,
        boolean duplicateParticipantsInAnyRound,
        int unknownParticipantCount,
        int changedExistingAssignmentsCount,
        int requestedPartnerPairCount,
        int satisfiedPartnerPairCount,
        int remainingEmptySlotCount,
        boolean repeatedRoundLayoutAcrossRounds,
        List<String> warningCodes,
        boolean pass,
        List<FailureReason> failureReasons
) {

    public enum FailureReason {
        DUPLICATE_PARTICIPANT_IN_ROUND,
        UNKNOWN_PARTICIPANT,
        EXISTING_ASSIGNMENT_CHANGED,
        NEGATIVE_FILLED_SLOT_DELTA,
        REPEATED_ROUND_LAYOUT,
        MISSING_WARNING_FOR_UNCHANGED_EMPTY_SLOTS,
        MISSING_PARTNER_CONSTRAINT_WARNING
    }
}
