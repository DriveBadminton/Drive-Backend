package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import java.util.Map;
import java.util.List;

public interface SaveGameParticipantPort {

    Map<Long, GameParticipant> saveAll(
            FreeGame freeGame,
            List<CreateFreeGameCommand.Participant> participants
    );

}
