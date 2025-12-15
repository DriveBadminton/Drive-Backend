package com.gumraze.drive.drive_backend.auth.controller;

import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.service.AuthService;
import com.gumraze.drive.drive_backend.auth.service.OAuthLoginResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // OAuth 로그인 API
    @PostMapping("/login")
    public ResponseEntity<OAuthLoginResult> login(
            @RequestBody OAuthLoginRequestDto request
    ) {
        OAuthLoginResult result = authService.login(request);
        return ResponseEntity.ok(result);
    }
}
