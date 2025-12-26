package com.gumraze.drive.drive_backend.region.service;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.region.entity.RegionProvince;

import java.util.List;
import java.util.Optional;

public interface RegionService {

    // Id로 시/군/구가 존재하는지 조회
    boolean existsByDistrictId(Long district_id);

    // 시/도 지역 정보 조회
    List<RegionProvince> getProvinces();

    List<RegionDistrict> getDistricts(Long province_id);

    // 시/군/구의 지역 정보 조회
    Optional<RegionDistrict> findDistrictsById(Long district_id);
}
