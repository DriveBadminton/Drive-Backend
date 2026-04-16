package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameAssignmentPreviewRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreateFreeGameAssignmentPreviewCommandMapper {

    public CreateFreeGameAssignmentPreviewCommand toCommand(CreateFreeGameAssignmentPreviewRequest request) {
        return new CreateFreeGameAssignmentPreviewCommand(
                request.participants().stream()
                        .map(participant -> new CreateFreeGameAssignmentPreviewCommand.Participant(
                                participant.participantId(),
                                participant.gender(),
                                participant.ageGroup(),
                                participant.grade(),
                                participant.gamesAssigned()
                        ))
                        .toList(),
                request.rounds().stream()
                        .map(round -> new CreateFreeGameAssignmentPreviewCommand.Round(
                                round.roundNumber(),
                                round.courts().stream()
                                        .map(court -> new CreateFreeGameAssignmentPreviewCommand.Court(
                                                court.courtNumber(),
                                                court.slots()
                                        ))
                                        .toList()
                        ))
                        .toList(),
                request.partnerPairs() == null ? List.of() : request.partnerPairs().stream()
                        .map(pair -> new CreateFreeGameAssignmentPreviewCommand.PartnerPairs(
                                pair.participantId1(),
                                pair.participantId2()
                        ))
                        .toList(),
                new CreateFreeGameAssignmentPreviewCommand.Preferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.valueOf(
                                request.preferences().partnerPolicy().name()
                        ),
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.valueOf(
                                request.preferences().existingAssignmentPolicy().name()
                        )
                )
        );
    }
}
