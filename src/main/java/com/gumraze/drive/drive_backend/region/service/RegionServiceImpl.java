package com.gumraze.drive.drive_backend.region.service;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.region.entity.RegionProvince;
import com.gumraze.drive.drive_backend.region.repository.JpaRegionDistrictRepository;
import com.gumraze.drive.drive_backend.region.repository.JpaRegionProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final JpaRegionDistrictRepository jpaRegionDistrictRepository;
    private final JpaRegionProvinceRepository jpaRegionProvinceRepository;

    @Override
    public boolean existsByDistrictId(Long district_id) {
        return jpaRegionDistrictRepository.existsById(district_id);
    }

    @Override
    public List<RegionProvince> getProvinces() {
        return jpaRegionProvinceRepository.findAll().stream()
                .sorted(Comparator.comparing(RegionProvince::getId))
                .toList();
    }

    @Override
    public List<RegionDistrict> getDistricts(Long province_id) {
        return jpaRegionDistrictRepository.findAllByProvinceId(province_id).stream()
                .sorted(Comparator.comparing(RegionDistrict::getId))
                .toList();
    }

    @Override
    public Optional<RegionDistrict> findDistrictsById(Long district_id) {
        return jpaRegionDistrictRepository.findById(district_id);
    }
}
