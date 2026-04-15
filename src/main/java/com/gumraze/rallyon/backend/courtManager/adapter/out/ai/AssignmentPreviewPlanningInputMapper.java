package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AssignmentPreviewPlanningInputMapper {

    public AssignmentPreviewPromptPayload from(CreateFreeGameAssignmentPreviewCommand command) {
        if (command == null) {
            return new AssignmentPreviewPromptPayload(
                    null,
                    Map.of(),
                    Map.of()
            );
        }

        boolean fillEmptySlots =
                command.preferences().existingAssignmentPolicy()
                        == CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS;
        Map<String, Long> compactIdByClientId = createCompactIdByClientId(command.participants());
        Map<Long, String> clientIdByCompactId = reverseAliases(compactIdByClientId);

        AssignmentPreviewPlanningInput planningInput = new AssignmentPreviewPlanningInput(
                command.participants().stream()
                        .map(participant -> new AssignmentPreviewPlanningInput.Participant(
                                compactIdByClientId.get(participant.clientId()),
                                participant.gamesAssigned()
                        ))
                        .toList(),
                command.rounds().stream()
                        .map(round -> new AssignmentPreviewPlanningInput.Round(
                                round.roundNumber(),
                                round.courts().stream()
                                        .map(court -> new AssignmentPreviewPlanningInput.Court(
                                                court.courtNumber(),
                                                mapSlots(court.slots(), fillEmptySlots, compactIdByClientId)
                                        ))
                                        .toList()
                        ))
                        .toList(),
                command.partnerPairs().stream()
                        .map(pair -> new AssignmentPreviewPlanningInput.PartnerPair(
                                compactIdByClientId.get(pair.participantId1()),
                                compactIdByClientId.get(pair.participantId2())
                        ))
                        .toList(),
                mapGuidance(command)
        );

        return new AssignmentPreviewPromptPayload(
                planningInput,
                compactIdByClientId,
                clientIdByCompactId
        );
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
            List<String> slots,
            boolean fillEmptySlots,
            Map<String, Long> compactIdByClientId
    ) {
        return slots.stream()
                .map(participantId -> new AssignmentPreviewPlanningInput.Slot(
                        participantId == null ? null : compactIdByClientId.get(participantId),
                        fillEmptySlots && participantId != null
                ))
                .toList();
    }

    private Map<String, Long> createCompactIdByClientId(
            List<CreateFreeGameAssignmentPreviewCommand.Participant> participants
    ) {
        Map<String, Long> compactIdByClientId = new LinkedHashMap<>();
        long nextId = 1L;
        for (CreateFreeGameAssignmentPreviewCommand.Participant participant : participants) {
            compactIdByClientId.put(participant.clientId(), nextId++);
        }
        return compactIdByClientId;
    }

    private Map<Long, String> reverseAliases(Map<String, Long> compactIdByClientId) {
        Map<Long, String> clientIdByCompactId = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : compactIdByClientId.entrySet()) {
            clientIdByCompactId.put(entry.getValue(), entry.getKey());
        }
        return clientIdByCompactId;
    }
}
