package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameRequest;
import org.springframework.stereotype.Component;

@Component
public class CreateFreeGameCommandMapper {

    public CreateFreeGameCommand toCommand(CreateFreeGameRequest request) {
        return new CreateFreeGameCommand(
                request.title(),
                request.matchRecordMode(),
                request.gradeType(),
                request.courtCount(),
                request.roundCount(),
                request.scheduledAt(),
                request.location(),
                request.managerIds(),
                request.participants() == null ? null : request.participants().stream()
                        .map(participant -> new CreateFreeGameCommand.Participant(
                                participant.participantId(),
                                participant.accountId(),
                                participant.originalName(),
                                participant.gender(),
                                participant.grade(),
                                participant.ageGroup()
                        ))
                        .toList(),
                request.rounds() == null ? null : request.rounds().stream()
                        .map(round -> new CreateFreeGameCommand.Round(
                                round.roundNumber(),
                                round.courts().stream()
                                        .map(court -> new CreateFreeGameCommand.Court(
                                                court.courtNumber(),
                                                court.slots()
                                        ))
                                        .toList()
                        ))
                        .toList()
        );
    }
}
