package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.StartFreeGameUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.StartFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.support.FreeGameAccessSupport;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StartFreeGameService implements StartFreeGameUseCase {

    private final LoadFreeGamePort loadFreeGamePort;
    private final SaveFreeGamePort saveFreeGamePort;

    @Override
    public UpdateFreeGameResponse start(StartFreeGameCommand command) {
        FreeGame freeGame = FreeGameAccessSupport.loadOrganizerGame(
                loadFreeGamePort,
                command.organizerId(),
                command.gameId()
        );

        freeGame.start();
        saveFreeGamePort.save(freeGame);
        return UpdateFreeGameResponse.from(freeGame);
    }
}
