package com.gumraze.rallyon.backend.courtManager.application.port.in.command;

import java.util.UUID;

public record StartFreeGameCommand(
        UUID organizerId,
        UUID gameId
) {
}
