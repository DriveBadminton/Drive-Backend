package com.gumraze.drive.drive_backend.auth.token;

import java.util.Optional;

public interface AccessTokenValidator {

    Optional<Long> validateAndGetUserId(String accessToken);

}
