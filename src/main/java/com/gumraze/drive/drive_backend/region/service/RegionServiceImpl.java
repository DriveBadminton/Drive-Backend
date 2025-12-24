package com.gumraze.drive.drive_backend.region.service;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.region.repository.JpaRegionDistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final JpaRegionDistrictRepository jpaRegionDistrictRepository;

    @Override
    public boolean existsById(Long district_id) {
        return jpaRegionDistrictRepository.existsById(district_id);
    }

    @Override
    public RegionDistrict getReferenceById(Long district_id) {
        return jpaRegionDistrictRepository.getReferenceById(district_id);
    }
}
