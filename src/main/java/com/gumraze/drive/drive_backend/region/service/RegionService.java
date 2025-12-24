package com.gumraze.drive.drive_backend.region.service;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;

public interface RegionService {

    // Id로 지역 조회
    boolean existsById(Long district_id);

    // 레퍼런스를 ID로 조회
    RegionDistrict getReferenceById(Long district_id);

}
