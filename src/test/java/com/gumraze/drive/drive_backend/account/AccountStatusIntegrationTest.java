package com.gumraze.drive.drive_backend.account;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.access-token.secret=THIS_IS_A_TEST_SECRET_KEY_THAT_IS_LONG_ENOUGH_32BYTES_MINIMUM",
        "jwt.access-token.expiration-ms=3600000"
})
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class AccountStatusIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인한 사용자가 상태 조회 API 호출 시, 200 반환")
    void get_account_status_returns_user_status_and_profile_flag() throws Exception {
        // given: OAuth 로그인
        OAuthLoginRequestDto request = new OAuthLoginRequestDto(
                AuthProvider.DUMMY,
                "auth-code",
                "https://test.com"
        );

        MvcResult loginResult = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper
                .readTree(loginResponseBody)
                .get("data")
                .get("accessToken")
                .asText();

        // when: /account/status 호출
        MvcResult statusResult = mockMvc.perform(
                        get("/account/status")
                                .header("Authorization", "Bearer " + accessToken)

                )
                .andExpect(status().isOk())
                .andReturn();

        // then: 200 OK, userId, status, hasProfile=false 반환
        JsonNode json = objectMapper
                .readTree(statusResult.getResponse().getContentAsString())
                .get("data");


        assertThat(json.get("userId").asLong()).isNotNull();
        assertThat(json.get("status").asText()).isEqualTo("PENDING");
        assertThat(json.get("hasProfile").asBoolean()).isFalse();
    }
}
