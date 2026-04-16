package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.domain.ParticipantDisplayNamePolicy;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameParticipantRepository;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SaveGameParticipantPersistenceAdapter implements SaveGameParticipantPort {

    private final GameParticipantRepository gameParticipantRepository;
    private final AccountRepository accountRepository;

    @Override
    public Map<Long, GameParticipant> saveAll(
            FreeGame freeGame,
            List<CreateFreeGameCommand.Participant> participants
    ) {
        if (participants == null || participants.isEmpty()) {
            return Map.of();
        }

        Map<Long, GameParticipant> participantsByParticipantId = new LinkedHashMap<>();

        for (CreateFreeGameCommand.Participant participant : participants) {
            if (participant.accountId() != null) {
                accountRepository.findById(participant.accountId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("존재하지 않는 accountId입니다. :" + participant.accountId()));
            }

            GameParticipant toSave = GameParticipant.create(
                    freeGame,
                    participant.accountId(),
                    participant.originalName(),
                    ParticipantDisplayNamePolicy.resolve(
                            participant.originalName(),
                            participant.gender(),
                            participant.grade(),
                            participant.ageGroup(),
                            participantsByParticipantId.values()
                    ),
                    participant.gender(),
                    participant.grade(),
                    participant.ageGroup()
            );

            GameParticipant savedParticipant = gameParticipantRepository.save(toSave);
            participantsByParticipantId.put(participant.participantId(), savedParticipant);
        }
        return participantsByParticipantId;
    }
}
