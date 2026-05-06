package com.gumraze.rallyon.backend.user.dto;

import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.user.constants.UserStatus;

import java.util.UUID;

public record UserMeResponse(
        UUID accountId,
        UserStatus status,
        String profileImageUrl,
        String nickname,
        AuthProvider provider
) {
}
