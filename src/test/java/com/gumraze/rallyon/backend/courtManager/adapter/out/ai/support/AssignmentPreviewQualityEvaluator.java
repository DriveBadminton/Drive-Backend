package com.gumraze.rallyon.backend.courtManager.adapter.out.ai.support;

import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.AssignmentPreviewAiResponse;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AssignmentPreviewQualityEvaluator {

    private static final String PARTNER_CONSTRAINT_PARTIAL = "PARTNER_CONSTRAINT_PARTIAL";

    public AssignmentPreviewQualityReport evaluate(
            CreateFreeGameAssignmentPreviewCommand command,
            AssignmentPreviewAiResponse response
    ) {
        Set<String> participantIds = safeList(command.participants()).stream()
                .map(CreateFreeGameAssignmentPreviewCommand.Participant::clientId)
                .collect(Collectors.toSet());

        int filledSlotsBefore = countFilledSlotsInCommand(command);
        int filledSlotsAfter = countFilledSlotsInResponse(response);
        int filledSlotDelta = filledSlotsAfter - filledSlotsBefore;
        boolean duplicateParticipantsInAnyRound = hasDuplicateParticipantsInAnyRound(response);
        int unknownParticipantCount = countUnknownParticipants(response, participantIds);
        int changedExistingAssignmentsCount = countChangedExistingAssignments(command, response);
        int requestedPartnerPairCount = safeList(command.partnerPairs()).size();
        int satisfiedPartnerPairCount = countSatisfiedPartnerPairs(command, response);
        int remainingEmptySlotCount = countRemainingEmptySlots(response);
        List<String> warningCodes = extractWarningCodes(response);

        List<AssignmentPreviewQualityReport.FailureReason> failureReasons = new ArrayList<>();

        if (duplicateParticipantsInAnyRound) {
            failureReasons.add(AssignmentPreviewQualityReport.FailureReason.DUPLICATE_PARTICIPANT_IN_ROUND);
        }

        if (unknownParticipantCount > 0) {
            failureReasons.add(AssignmentPreviewQualityReport.FailureReason.UNKNOWN_PARTICIPANT);
        }

        if (isFillEmptySlotsPolicy(command) && changedExistingAssignmentsCount > 0) {
            failureReasons.add(AssignmentPreviewQualityReport.FailureReason.EXISTING_ASSIGNMENT_CHANGED);
        }

        if (filledSlotDelta < 0) {
            failureReasons.add(AssignmentPreviewQualityReport.FailureReason.NEGATIVE_FILLED_SLOT_DELTA);
        }

        if (remainingEmptySlotCount > 0 && filledSlotDelta <= 0 && warningCodes.isEmpty()) {
            failureReasons.add(
                    AssignmentPreviewQualityReport.FailureReason.MISSING_WARNING_FOR_UNCHANGED_EMPTY_SLOTS
            );
        }

        if (isPreferPartnersPolicy(command)
                && requestedPartnerPairCount > satisfiedPartnerPairCount
                && !warningCodes.contains(PARTNER_CONSTRAINT_PARTIAL)) {
            failureReasons.add(AssignmentPreviewQualityReport.FailureReason.MISSING_PARTNER_CONSTRAINT_WARNING);
        }

        return new AssignmentPreviewQualityReport(
                filledSlotsBefore,
                filledSlotsAfter,
                filledSlotDelta,
                duplicateParticipantsInAnyRound,
                unknownParticipantCount,
                changedExistingAssignmentsCount,
                requestedPartnerPairCount,
                satisfiedPartnerPairCount,
                remainingEmptySlotCount,
                warningCodes,
                failureReasons.isEmpty(),
                List.copyOf(failureReasons)
        );
    }

    private int countFilledSlotsInCommand(CreateFreeGameAssignmentPreviewCommand command) {
        return safeList(command.rounds()).stream()
                .flatMap(round -> safeList(round.courts()).stream())
                .flatMap(court -> safeList(court.slots()).stream())
                .mapToInt(slot -> slot == null ? 0 : 1)
                .sum();
    }

    private int countFilledSlotsInResponse(AssignmentPreviewAiResponse response) {
        return safeList(response.rounds()).stream()
                .flatMap(round -> safeList(round.courts()).stream())
                .flatMap(court -> safeList(court.slots()).stream())
                .mapToInt(slot -> slot == null ? 0 : 1)
                .sum();
    }

    private boolean hasDuplicateParticipantsInAnyRound(AssignmentPreviewAiResponse response) {
        for (AssignmentPreviewAiResponse.Round round : safeList(response.rounds())) {
            Set<String> assignedParticipants = new HashSet<>();
            for (AssignmentPreviewAiResponse.Court court : safeList(round.courts())) {
                for (String slot : safeList(court.slots())) {
                    if (slot != null && !assignedParticipants.add(slot)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private int countUnknownParticipants(
            AssignmentPreviewAiResponse response,
            Set<String> participantIds
    ) {
        return safeList(response.rounds()).stream()
                .flatMap(round -> safeList(round.courts()).stream())
                .flatMap(court -> safeList(court.slots()).stream())
                .mapToInt(slot -> slot != null && !participantIds.contains(slot) ? 1 : 0)
                .sum();
    }

    private int countChangedExistingAssignments(
            CreateFreeGameAssignmentPreviewCommand command,
            AssignmentPreviewAiResponse response
    ) {
        int changedAssignments = 0;
        List<CreateFreeGameAssignmentPreviewCommand.Round> requestedRounds = safeList(command.rounds());

        for (int roundIndex = 0; roundIndex < requestedRounds.size(); roundIndex++) {
            CreateFreeGameAssignmentPreviewCommand.Round requestedRound = requestedRounds.get(roundIndex);

            for (int courtIndex = 0; courtIndex < safeList(requestedRound.courts()).size(); courtIndex++) {
                CreateFreeGameAssignmentPreviewCommand.Court requestedCourt = requestedRound.courts().get(courtIndex);

                for (int slotIndex = 0; slotIndex < safeList(requestedCourt.slots()).size(); slotIndex++) {
                    String requestedParticipantId = requestedCourt.slots().get(slotIndex);
                    if (requestedParticipantId == null) {
                        continue;
                    }

                    String responseParticipantId = getResponseSlot(response, roundIndex, courtIndex, slotIndex);
                    if (!requestedParticipantId.equals(responseParticipantId)) {
                        changedAssignments++;
                    }
                }
            }
        }

        return changedAssignments;
    }

    private int countSatisfiedPartnerPairs(
            CreateFreeGameAssignmentPreviewCommand command,
            AssignmentPreviewAiResponse response
    ) {
        return (int) safeList(command.partnerPairs()).stream()
                .filter(pair -> isPartnerPairSatisfied(pair, response))
                .count();
    }

    private boolean isPartnerPairSatisfied(
            CreateFreeGameAssignmentPreviewCommand.PartnerPairs pair,
            AssignmentPreviewAiResponse response
    ) {
        return safeList(response.rounds()).stream()
                .flatMap(round -> safeList(round.courts()).stream())
                .anyMatch(court -> {
                    List<String> slots = safeList(court.slots());
                    return slots.contains(pair.participantId1()) && slots.contains(pair.participantId2());
                });
    }

    private int countRemainingEmptySlots(AssignmentPreviewAiResponse response) {
        return safeList(response.rounds()).stream()
                .flatMap(round -> safeList(round.courts()).stream())
                .flatMap(court -> safeList(court.slots()).stream())
                .mapToInt(slot -> slot == null ? 1 : 0)
                .sum();
    }

    private List<String> extractWarningCodes(AssignmentPreviewAiResponse response) {
        return safeList(response.warnings()).stream()
                .map(AssignmentPreviewAiResponse.Warning::code)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private boolean isFillEmptySlotsPolicy(CreateFreeGameAssignmentPreviewCommand command) {
        return command.preferences() != null
                && command.preferences().existingAssignmentPolicy()
                == CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS;
    }

    private boolean isPreferPartnersPolicy(CreateFreeGameAssignmentPreviewCommand command) {
        return command.preferences() != null
                && command.preferences().partnerPolicy()
                == CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS;
    }

    private String getResponseSlot(
            AssignmentPreviewAiResponse response,
            int roundIndex,
            int courtIndex,
            int slotIndex
    ) {
        List<AssignmentPreviewAiResponse.Round> rounds = safeList(response.rounds());
        if (roundIndex >= rounds.size()) {
            return null;
        }

        List<AssignmentPreviewAiResponse.Court> courts = safeList(rounds.get(roundIndex).courts());
        if (courtIndex >= courts.size()) {
            return null;
        }

        List<String> slots = safeList(courts.get(courtIndex).slots());
        if (slotIndex >= slots.size()) {
            return null;
        }

        return slots.get(slotIndex);
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}
