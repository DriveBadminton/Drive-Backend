package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.region.Region;
import com.gumraze.drive.drive_backend.user.repository.JpaRegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {
    private final JpaRegionRepository jpaRegionRepository;

    @Override
    public boolean existsById(Long regionId) {
        return jpaRegionRepository.existsById(regionId);
    }

    @Override
    public Region getReferenceById(Long regionId) {
        return jpaRegionRepository.getReferenceById(regionId);
    }
}
