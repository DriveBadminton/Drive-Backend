package com.gumraze.drive.drive_backend.region.repository;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRegionDistrictRepository extends JpaRepository<RegionDistrict, Long> {
}
