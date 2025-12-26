package com.gumraze.drive.drive_backend.user;

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
import org.springframework.test.context.jdbc.Sql;
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
@Sql(
        statements = {
                "INSERT INTO region_province (id, name, code, created_at, updated_at) VALUES (1, '서울특별시', '09', NOW(), NOW())",
                "INSERT INTO region_district (id, province_id, name, code, created_at, updated_at) VALUES (1, 1, '강남구', '1001', NOW(), NOW())"
        }
)
public class UserProfileOnboardingIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("PENDING 사용자가 프로필을 생성하면 ACTIVE 상태로 전환된다.")
    void create_profile_activates_pending_user() throws Exception {
        // given: OAuth 로그인 (PENDING 사용자 생성)
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
                .andExpect(status().isCreated())
                .andReturn();

        String accessToken = objectMapper
                .readTree(loginResult.getResponse().getContentAsString())
                .get("data")
                .get("accessToken")
                .asText();

        // when: 프로필 생성 요청
        String requestBody = """
                {
                    "nickname": "kim",
                    "districtId": 1,
                    "grade": "초심",
                    "birth": "19980925",
                    "gender": "MALE"
                }
                """;

        mockMvc.perform(
                        post("/users/me/profile")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andReturn();

        // then: account status가 ACTIVE + has Profile = true
        MvcResult statusResult = mockMvc.perform(
                        get("/users/me")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(statusResult.getResponse().getContentAsString())
                .get("data");

        assertThat(data.get("status").asText()).isEqualTo("ACTIVE");
    }
}
