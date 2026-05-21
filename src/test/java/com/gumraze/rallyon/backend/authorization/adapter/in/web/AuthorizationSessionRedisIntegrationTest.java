package com.gumraze.rallyon.backend.authorization.adapter.in.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.AccountRepository;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.LocalCredentialRepository;
import com.gumraze.rallyon.backend.identity.application.port.out.PasswordHasherPort;
import com.gumraze.rallyon.backend.identity.entity.Account;
import com.gumraze.rallyon.backend.identity.entity.LocalCredential;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.enabled=true",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.session.store-type=redis",
                "spring.session.redis.namespace=rallyon:test:session",
                "server.forward-headers-strategy=framework"
        }
)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AuthorizationSessionRedisIntegrationTest {

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("rallyon")
            .withUsername("rallyon")
            .withPassword("rallyon");

    @DynamicPropertySource
    static void configureContainers(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
        registry.add("spring.data.redis.ssl.enabled", () -> false);
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LocalCredentialRepository localCredentialRepository;

    @Autowired
    private PasswordHasherPort passwordHasherPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void clearState() {
        Set<String> keys = stringRedisTemplate.keys("rallyon:test:session:*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
        jdbcTemplate.update("DELETE FROM oauth2_authorization_consent");
        jdbcTemplate.update("DELETE FROM oauth2_authorization");
        localCredentialRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Redis-backed session은 JSESSIONID 쿠키로 auth session을 복원한다")
    void currentSession_usesRedisBackedSession() throws Exception {
        HttpExchangeResult createSession = postJson(
                "/identity/sessions",
                """
                        {"screen":"signup","returnTo":"/profile/setup"}
                        """,
                null
        );

        assertThat(createSession.statusCode()).isEqualTo(200);
        Map<String, Object> createSessionBody = json(createSession.body());
        assertThat(createSessionBody.get("nextUrl")).isEqualTo("/signup?returnTo=/profile/setup");

        String sessionCookie = sessionCookie(createSession);

        HttpExchangeResult currentSession = get("/identity/sessions/current", sessionCookie);
        assertThat(currentSession.statusCode()).isEqualTo(200);

        Map<String, Object> currentSessionBody = json(currentSession.body());
        assertThat(currentSessionBody.get("hasSession")).isEqualTo(true);
        assertThat(currentSessionBody.get("returnTo")).isEqualTo("/profile/setup");
        assertThat(currentSessionBody.get("screen")).isEqualTo("signup");

        Set<String> redisKeys = stringRedisTemplate.keys("*");
        assertThat(redisKeys).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Redis-backed security context는 authorize 요청을 login 대신 callback으로 진행시킨다")
    void authorizeRequest_usesRedisBackedSecurityContext() throws Exception {
        String email = "redis-session@rallyon.test";
        String password = "password123!";
        seedLocalCredential(email, password);

        HttpExchangeResult sessionStart = postJson(
                "/identity/sessions",
                """
                        {"screen":"login","returnTo":"/court-manager"}
                        """,
                null
        );
        assertThat(sessionStart.statusCode()).isEqualTo(200);

        String sessionCookie = sessionCookie(sessionStart);

        HttpExchangeResult localLogin = postForm(
                "/identity/sessions/local",
                List.of(
                        new BasicNameValuePair("email", email),
                        new BasicNameValuePair("password", password),
                        new BasicNameValuePair("returnTo", "/court-manager")
                ),
                sessionCookie
        );
        assertThat(localLogin.statusCode()).isEqualTo(302);

        String authorizeUrl = header(localLogin, HttpHeaders.LOCATION);
        assertThat(authorizeUrl).contains("/oauth2/authorize");

        URI authorizeUri = URI.create(authorizeUrl);
        HttpExchangeResult authorizeResponse = get(
                authorizeUri.getRawPath() + "?" + authorizeUri.getRawQuery(),
                sessionCookie
        );
        assertThat(authorizeResponse.statusCode()).isEqualTo(302);
        assertThat(header(authorizeResponse, HttpHeaders.LOCATION))
                .startsWith("https://auth.rallyon.test/identity/session/callback?code=")
                .contains("&state=");
    }

    private void seedLocalCredential(String email, String rawPassword) {
        Account account = accountRepository.save(Account.create());
        LocalCredential credential = LocalCredential.issue(
                account,
                email,
                passwordHasherPort.hash(rawPassword)
        );
        localCredentialRepository.save(credential);
    }

    private HttpExchangeResult get(String pathAndQuery, String sessionCookie) throws Exception {
        HttpGet request = new HttpGet(baseUrl() + pathAndQuery);
        request.addHeader(HttpHeaders.HOST, "auth.rallyon.test");
        if (sessionCookie != null) {
            request.addHeader(HttpHeaders.COOKIE, sessionCookie);
        }
        return execute(request);
    }

    private HttpExchangeResult postJson(String path, String body, String sessionCookie) throws Exception {
        HttpPost request = new HttpPost(baseUrl() + path);
        request.addHeader(HttpHeaders.HOST, "auth.rallyon.test");
        request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (sessionCookie != null) {
            request.addHeader(HttpHeaders.COOKIE, sessionCookie);
        }
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        return execute(request);
    }

    private HttpExchangeResult postForm(String path, List<NameValuePair> form, String sessionCookie) throws Exception {
        HttpPost request = new HttpPost(baseUrl() + path);
        request.addHeader(HttpHeaders.HOST, "auth.rallyon.test");
        request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        if (sessionCookie != null) {
            request.addHeader(HttpHeaders.COOKIE, sessionCookie);
        }
        request.setEntity(new UrlEncodedFormEntity(form));
        return execute(request);
    }

    private HttpExchangeResult execute(org.apache.hc.core5.http.ClassicHttpRequest request) throws Exception {
        RequestConfig requestConfig = RequestConfig.custom()
                .setRedirectsEnabled(false)
                .build();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .disableRedirectHandling()
                .build();
             var response = client.execute(request)) {
            HttpEntity entity = response.getEntity();
            String body = entity == null ? "" : EntityUtils.toString(entity, StandardCharsets.UTF_8);
            return new HttpExchangeResult(
                    response.getCode(),
                    response.getHeaders(),
                    body
            );
        }
    }

    private Map<String, Object> json(String body) throws Exception {
        return objectMapper.readValue(body, new TypeReference<>() {
        });
    }

    private String sessionCookie(HttpExchangeResult result) {
        String setCookie = header(result, "Set-Cookie");
        assertThat(setCookie).startsWith("JSESSIONID=");
        return setCookie.substring(0, setCookie.indexOf(';'));
    }

    private String header(HttpExchangeResult result, String name) {
        return Arrays.stream(result.headers())
                .filter(header -> header.getName().equalsIgnoreCase(name))
                .findFirst()
                .map(Header::getValue)
                .orElse(null);
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + port;
    }

    private record HttpExchangeResult(
            int statusCode,
            Header[] headers,
            String body
    ) {
    }
}
