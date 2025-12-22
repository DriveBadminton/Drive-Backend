package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.region.Region;
import org.springframework.stereotype.Service;

@Service
public interface RegionService {

    // Id로 지역 조회
    boolean existsById(Long regionId);

    // 레퍼런스를 ID로 조회
    Region getReferenceById(Long regionId);

}
