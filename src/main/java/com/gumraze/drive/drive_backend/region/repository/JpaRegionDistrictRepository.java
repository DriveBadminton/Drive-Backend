package com.gumraze.drive.drive_backend.region.repository;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaRegionDistrictRepository extends JpaRepository<RegionDistrict, Long> {
    // province Id로 district 조회
    List<RegionDistrict> findAllByProvinceId(Long province_id);
}
