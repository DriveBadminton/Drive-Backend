package com.gumraze.drive.drive_backend.region.service;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.region.entity.RegionProvince;
import com.gumraze.drive.drive_backend.region.repository.RegionDistrictRepository;
import com.gumraze.drive.drive_backend.region.repository.RegionProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionDistrictRepository regionDistrictRepository;
    private final RegionProvinceRepository regionProvinceRepository;

    @Override
    public boolean existsByDistrictId(Long district_id) {
        return regionDistrictRepository.existsById(district_id);
    }

    @Override
    public List<RegionProvince> getProvinces() {
        return regionProvinceRepository.findAll().stream()
                .sorted(Comparator.comparing(RegionProvince::getId))
                .toList();
    }

    @Override
    public List<RegionDistrict> getDistricts(Long province_id) {
        return regionDistrictRepository.findAllByProvinceId(province_id).stream()
                .sorted(Comparator.comparing(RegionDistrict::getId))
                .toList();
    }

    @Override
    public Optional<RegionDistrict> findDistrictsById(Long district_id) {
        return regionDistrictRepository.findById(district_id);
    }
}
