package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.AddFreeGameParticipantUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.AddFreeGameParticipantCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.AddGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AddFreeGameParticipantService implements AddFreeGameParticipantUseCase {

    private final LoadFreeGamePort loadFreeGamePort;
    private final AddGameParticipantPort addGameParticipantPort;

    @Override
    public UUID add(UUID organizerId, UUID gameId, AddFreeGameParticipantCommand command) {
        FreeGame freeGame = loadFreeGamePort.loadGameById(gameId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 자유게임입니다. gameId: " + gameId));

        if (!freeGame.getOrganizerAccountId().equals(organizerId)) {
            throw new ForbiddenException("게임의 organizer가 아닙니다. gameId: " + gameId);
        }

        if (freeGame.getGameStatus() == GameStatus.COMPLETED) {
            throw new IllegalArgumentException("완료된 자유게임에는 참가자를 추가할 수 없습니다.");
        }

        GameParticipant savedParticipant = addGameParticipantPort.add(freeGame, command);
        return savedParticipant.getId();
    }
}
