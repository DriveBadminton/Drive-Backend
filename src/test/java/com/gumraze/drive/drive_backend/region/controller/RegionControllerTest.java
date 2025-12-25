package com.gumraze.drive.drive_backend.region.controller;

import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.drive.drive_backend.config.SecurityConfig;
import com.gumraze.drive.drive_backend.region.entity.RegionProvince;
import com.gumraze.drive.drive_backend.region.service.RegionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegionController.class)     // RegionController 클래스만 로드
@Import(SecurityConfig.class)
public class RegionControllerTest {

    // Http요청 및 응답을 가짜로 테스트
    @Autowired
    private MockMvc mockMvc;

    // 서비스는 가짜로 주입
    @MockitoBean
    private RegionService regionService;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @Test
    @DisplayName("province 목록 조회 API")
    void get_provinces() throws Exception {
        // given: getProvinces()가 id와 name을 반환한다고 가정
        RegionProvince p1 = mock(RegionProvince.class);
        RegionProvince p2 = mock(RegionProvince.class);

        when(p1.getId()).thenReturn(1L);
        when(p1.getName()).thenReturn("서울특별시");
        when(p2.getId()).thenReturn(2L);
        when(p2.getName()).thenReturn("경기도");

        // regionService 요청 시
        when(regionService.getProvinces()).thenReturn(List.of(p1, p2));

        // when: /regions/provinces 요청
        mockMvc.perform(get("/regions/provinces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("서울특별시"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].name").value("경기도"));
    }
}
