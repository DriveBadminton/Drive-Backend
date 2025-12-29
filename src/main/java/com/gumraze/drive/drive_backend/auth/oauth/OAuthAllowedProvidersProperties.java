package com.gumraze.drive.drive_backend.auth.oauth;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth")
public class OAuthAllowedProvidersProperties {
    private List<AuthProvider> allowedProviders = List.of(AuthProvider.KAKAO);
}
