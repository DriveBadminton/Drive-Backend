package com.gumraze.drive.drive_backend.auth.oauth;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class OAuthClientResolver {

    private final Map<AuthProvider, OAuthClient> clients =
            new EnumMap<>(AuthProvider.class);

    // Spring이 주입한 OAuthClient 빈 목록에서 ProviderAwareOAuthClient만 고른뒤,
    // AuthProvider를 키로 EnumMap에 등록함.
    // 즉, 제공자별로 사용할 클라이언트를 초기 매핑하는 생성자임.
    public OAuthClientResolver(List<OAuthClient> clientBeans) {
        for (OAuthClient client : clientBeans) {
            if (client instanceof ProviderAwareOAuthClient aware) {
                clients.put(aware.supports(), client);
            }
        }
    }

    // 요청된 provider 키로 매핑된 OAuthClient를 반환함.
    public OAuthClient resolve(AuthProvider provider) {
        OAuthClient client = clients.get(provider);
        if (client == null) {
            throw new IllegalArgumentException("지원되지 않는 OAuth 클라이언트: " + provider);
        }
        return client;
    }
}
