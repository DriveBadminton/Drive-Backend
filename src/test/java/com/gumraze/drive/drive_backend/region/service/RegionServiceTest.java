package com.gumraze.drive.drive_backend.region.service;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.region.entity.RegionProvince;
import com.gumraze.drive.drive_backend.region.repository.RegionDistrictRepository;
import com.gumraze.drive.drive_backend.region.repository.RegionProvinceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// JUnit5에서 Mockito 기능을 활성화하는 역할
@ExtendWith(MockitoExtension.class)
class RegionServiceTest {

    // 가짜 객체 생성
    @Mock private RegionProvinceRepository regionProvinceRepository;
    @Mock private RegionDistrictRepository regionDistrictRepository;

    @InjectMocks    // 테스트 대상 클래스의 실제 인스턴스를 만들고, 그 안에 @Mock으로 만든 객체를 자동 주입함.
    private RegionServiceImpl regionService;

    @Test
    @DisplayName("province 목록을 id 오름차순으로 조회한다.")
    void get_provinces_asc() {
        // given: 섞인 순서의 province mock 리스트
        // mock으로 가짜 객체를 생성
        RegionProvince p1 = mock(RegionProvince.class);
        RegionProvince p2 = mock(RegionProvince.class);
        RegionProvince p3 = mock(RegionProvince.class);

        // 가짜 객체의 동작을 미리 설정
        when(p1.getId()).thenReturn(1L);
        when(p2.getId()).thenReturn(2L);
        when(p3.getId()).thenReturn(3L);

        // repo는 정렬되지 않은 리스트를 반환한다고 가정함.
        when(regionProvinceRepository.findAll())
                .thenReturn(List.of(p2, p1, p3));

        // when: province 목록 조회 메서드를 호출 할 때,
        List<RegionProvince> result = regionService.getProvinces();

        // then: 반환된 리스트가 id 오름차순으로 정렬되어 있음.
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(3L, result.get(2).getId());
    }

    @Test
    @DisplayName("provinceId를 받아서 district 목록을 id 오름차순으로 조회한다.")
    void get_districts_asc() {
        // given: district 목록이 정렬이 되어있는지 모르는 상태로 주어짐.
        RegionDistrict d1 = mock(RegionDistrict.class);
        RegionDistrict d2 = mock(RegionDistrict.class);
        RegionDistrict d3 = mock(RegionDistrict.class);

        when(d1.getId()).thenReturn(1L);
        when(d2.getId()).thenReturn(2L);
        when(d3.getId()).thenReturn(3L);

        // jpa repo에서 정렬되지 않은 리스트를 반환한다고 가정함.
        when(regionDistrictRepository.findAllByProvinceId(1L))
                .thenReturn(List.of(d3, d1, d2));

        // when: provinceId를 받아서 district 목록을 id 오름차순으로 조회함.
        List<RegionDistrict> result = regionService.getDistricts(1L);

        // then: 정렬된 데이터가 내려옴.
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(3L, result.get(2).getId());
    }
}