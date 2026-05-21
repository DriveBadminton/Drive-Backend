package com.gumraze.rallyon.backend.courtManager.application.port.in.command;

import java.util.UUID;

public record StartFreeGameMatchCommand(
        UUID organizerId,
        UUID gameId,
        Integer roundNumber,
        Integer courtNumber
) {
}
