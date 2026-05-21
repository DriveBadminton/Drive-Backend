package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.CreateFreeGameUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.IssueShareCodePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadAccountPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGameSettingPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.domain.FreeGameScheduleValidator;
import com.gumraze.rallyon.backend.courtManager.domain.assignment.CourtAssignment;
import com.gumraze.rallyon.backend.courtManager.domain.assignment.RoundAssignment;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateFreeGameService implements CreateFreeGameUseCase {

    private final LoadAccountPort loadAccountPort;
    private final IssueShareCodePort issueShareCodePort;
    private final SaveFreeGamePort saveFreeGamePort;
    private final SaveFreeGameSettingPort saveFreeGameSettingPort;
    private final SaveGameParticipantPort saveGameParticipantPort;
    private final SaveFreeGameRoundPort saveFreeGameRoundPort;
    private final FreeGameScheduleValidator freeGameScheduleValidator;

    @Override
    public UUID create(UUID organizerId, CreateFreeGameCommand command) {
        if (!loadAccountPort.existsById(organizerId)) {
            throw new IllegalArgumentException("존재하지 않는 organizer 입니다.");
        }

        MatchRecordMode matchRecordMode = command.matchRecordMode() == null
                ? MatchRecordMode.STATUS_ONLY
                : command.matchRecordMode();

        validateManagerIds(organizerId, command.managerIds());
        String shareCode = issueShareCodePort.issue();
        var scheduledAt = freeGameScheduleValidator.parseRequiredFuture(command.scheduledAt());

        FreeGame freeGame = FreeGame.create(
                command.title(),
                organizerId,
                command.gradeType(),
                matchRecordMode,
                shareCode,
                scheduledAt,
                command.location()
        );

        FreeGame savedGame = saveFreeGamePort.save(freeGame);
        saveFreeGameSettingPort.save(savedGame, command.courtCount(), command.roundCount());

        Map<Long, GameParticipant> participantsByParticipantId =
                saveGameParticipantPort.saveAll(savedGame, command.participants());

        List<RoundAssignment> roundAssignments = toRoundAssignments(
                command.rounds(),
                participantsByParticipantId
        );

        saveFreeGameRoundPort.saveAll(savedGame, roundAssignments);

        return savedGame.getId();
    }

    private void validateManagerIds(UUID organizerId, List<UUID> managerIds) {
        if (managerIds == null) {
            return;
        }

        if (managerIds.size() > 2) {
            throw new IllegalArgumentException("managerIds는 최대 2명까지 가능합니다.");
        }

        if (managerIds.contains(organizerId)) {
            throw new IllegalArgumentException("게임 생성자는 managerIds에 포함될 수 없습니다.");
        }

        for (UUID managerId : managerIds) {
            if (!loadAccountPort.existsById(managerId)) {
                throw new IllegalArgumentException("존재하지 않는 managerId입니다. :" + managerId);
            }
        }
    }

    private List<RoundAssignment> toRoundAssignments(
            List<CreateFreeGameCommand.Round> rounds,
            Map<Long, GameParticipant> participantsByParticipantId
    ) {
        if (rounds == null || rounds.isEmpty()) {
            return List.of();
        }

        return rounds.stream()
                .map(round -> new RoundAssignment(
                        round.roundNumber(),
                        round.courts().stream()
                                .map(court -> new CourtAssignment(
                                        court.courtNumber(),
                                        resolveParticipantId(participantsByParticipantId, court.slots().get(0)),
                                        resolveParticipantId(participantsByParticipantId, court.slots().get(1)),
                                        resolveParticipantId(participantsByParticipantId, court.slots().get(2)),
                                        resolveParticipantId(participantsByParticipantId, court.slots().get(3))
                                ))
                                .toList()
                ))
                .toList();
    }

    private UUID resolveParticipantId(
            Map<Long, GameParticipant> participantsByParticipantId,
            Long participantId
    ) {
        if (participantId == null) {
            return null;
        }

        GameParticipant participant = participantsByParticipantId.get(participantId);
        if (participant == null) {
            throw new IllegalArgumentException("참가자 매핑에 실패했습니다. participantId: " + participantId);
        }
        return participant.getId();
    }
}
