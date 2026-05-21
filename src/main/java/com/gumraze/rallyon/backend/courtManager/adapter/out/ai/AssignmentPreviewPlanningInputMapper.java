package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssignmentPreviewPlanningInputMapper {

    public AssignmentPreviewPromptPayload from(CreateFreeGameAssignmentPreviewCommand command) {
        if (command == null) {
            return new AssignmentPreviewPromptPayload(
                    null
            );
        }

        boolean fillEmptySlots =
                command.preferences().existingAssignmentPolicy()
                        == CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS;
        AssignmentPreviewPlanningInput planningInput = new AssignmentPreviewPlanningInput(
                command.participants().stream()
                        .map(participant -> new AssignmentPreviewPlanningInput.Participant(
                                participant.participantId(),
                                participant.gamesAssigned()
                        ))
                        .toList(),
                command.rounds().stream()
                        .map(round -> new AssignmentPreviewPlanningInput.Round(
                                round.roundNumber(),
                                round.courts().stream()
                                        .map(court -> new AssignmentPreviewPlanningInput.Court(
                                                court.courtNumber(),
                                                mapSlots(court.slots(), fillEmptySlots)
                                        ))
                                        .toList()
                        ))
                        .toList(),
                command.partnerPairs().stream()
                        .map(pair -> new AssignmentPreviewPlanningInput.PartnerPair(
                                pair.participantId1(),
                                pair.participantId2()
                        ))
                        .toList(),
                mapGuidance(command)
        );

        return new AssignmentPreviewPromptPayload(planningInput);
    }

    private AssignmentPreviewPlanningInput.Guidance mapGuidance(
            CreateFreeGameAssignmentPreviewCommand command
    ) {
        boolean preserveFixedSlots =
                command.preferences().existingAssignmentPolicy()
                        == CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS;
        boolean preferProvidedPartnerPairs =
                command.preferences().partnerPolicy()
                        == CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS;
        int preferredPairCount = preferProvidedPartnerPairs ? command.partnerPairs().size() : 0;

        return new AssignmentPreviewPlanningInput.Guidance(
                preserveFixedSlots,
                preserveFixedSlots,
                preferProvidedPartnerPairs,
                preferredPairCount
        );
    }

    private List<AssignmentPreviewPlanningInput.Slot> mapSlots(
            List<Long> slots,
            boolean fillEmptySlots
    ) {
        return slots.stream()
                .map(participantId -> new AssignmentPreviewPlanningInput.Slot(
                        participantId,
                        fillEmptySlots && participantId != null
                ))
                .toList();
    }
}
