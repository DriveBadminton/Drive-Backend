package com.gumraze.drive.drive_backend.auth.oauth.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserResponse(
        @JsonProperty("sub")
        String sub
) {
}
