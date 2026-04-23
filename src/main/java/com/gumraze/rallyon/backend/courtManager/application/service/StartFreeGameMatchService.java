package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.StartFreeGameMatchUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.StartFreeGameMatchCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameMatchPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.ManageFreeGameRoundMatchPort;
import com.gumraze.rallyon.backend.courtManager.application.support.FreeGameAccessSupport;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StartFreeGameMatchService implements StartFreeGameMatchUseCase {

    private final LoadFreeGamePort loadFreeGamePort;
    private final LoadFreeGameMatchPort loadFreeGameMatchPort;
    private final ManageFreeGameRoundMatchPort manageFreeGameRoundMatchPort;

    @Override
    public void start(StartFreeGameMatchCommand command) {
        FreeGame freeGame = FreeGameAccessSupport.loadOrganizerGame(
                loadFreeGamePort,
                command.organizerId(),
                command.gameId()
        );
        if (freeGame.getGameStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("진행 중인 자유게임의 매치만 시작할 수 있습니다.");
        }

        FreeGameMatch match = loadMatch(command);
        match.getRound().start();
        match.start();
        manageFreeGameRoundMatchPort.saveRound(match.getRound());
        manageFreeGameRoundMatchPort.saveMatch(match);
    }

    private FreeGameMatch loadMatch(StartFreeGameMatchCommand command) {
        return loadFreeGameMatchPort.loadMatchByGameIdAndRoundNumberAndCourtNumber(
                        command.gameId(),
                        command.roundNumber(),
                        command.courtNumber()
                )
                .orElseThrow(() -> new NotFoundException(
                        "존재하지 않는 매치입니다. gameId: " + command.gameId()
                                + ", roundNumber: " + command.roundNumber()
                                + ", courtNumber: " + command.courtNumber()
                ));
    }
}
