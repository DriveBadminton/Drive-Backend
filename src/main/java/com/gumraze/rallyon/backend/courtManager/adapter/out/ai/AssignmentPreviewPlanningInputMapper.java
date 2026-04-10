package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
public class AssignmentPreviewPlanningInputMapper {

    public AssignmentPreviewPlanningInput from(CreateFreeGameAssignmentPreviewCommand command) {
        boolean fillEmptySlots =
                command.preferences().existingAssignmentPolicy()
                        == CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS;

        return new AssignmentPreviewPlanningInput(
                command.participants().stream()
                        .map(participant -> new AssignmentPreviewPlanningInput.Participant(
                                participant.clientId(),
                                participant.name(),
                                participant.gender().name(),
                                participant.ageGroup(),
                                participant.grade().name(),
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
                new AssignmentPreviewPlanningInput.Preferences(
                        command.preferences().partnerPolicy().name(),
                        command.preferences().existingAssignmentPolicy().name()
                ),
                mapConstraintGuidance(command),
                mapPolicyGuidance(command),
                mapPartnerGuidance(command)
        );
    }

    private AssignmentPreviewPlanningInput.ConstraintGuidance mapConstraintGuidance(
            CreateFreeGameAssignmentPreviewCommand command
    ) {
        boolean preserveFixedSlots =
                command.preferences().existingAssignmentPolicy()
                        == CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS;

        return new AssignmentPreviewPlanningInput.ConstraintGuidance(
                true,
                preserveFixedSlots,
                true
        );
    }


    private List<AssignmentPreviewPlanningInput.Slot> mapSlots(
            List<String> slots,
            boolean fillEmptySlots
    ) {
        return IntStream.range(0, slots.size())
                .mapToObj(index -> {
                    String participantId = slots.get(index);
                    boolean fixed = fillEmptySlots && participantId != null;
                    return new AssignmentPreviewPlanningInput.Slot(index, participantId, fixed);
                })
                .toList();
    }

    private AssignmentPreviewPlanningInput.PolicyGuidance mapPolicyGuidance(
            CreateFreeGameAssignmentPreviewCommand command
    ) {
        return new AssignmentPreviewPlanningInput.PolicyGuidance(
                command.preferences().existingAssignmentPolicy()
                        == CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
        );
    }

    private AssignmentPreviewPlanningInput.PartnerGuidance mapPartnerGuidance(
            CreateFreeGameAssignmentPreviewCommand command
    ) {
        boolean preferProvidedPartnerPairs =
                command.preferences().partnerPolicy()
                        == CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS;

        int preferredPairCount = preferProvidedPartnerPairs ? command.partnerPairs().size() : 0;

        return new AssignmentPreviewPlanningInput.PartnerGuidance(
                preferProvidedPartnerPairs,
                preferredPairCount
        );
    }

}
