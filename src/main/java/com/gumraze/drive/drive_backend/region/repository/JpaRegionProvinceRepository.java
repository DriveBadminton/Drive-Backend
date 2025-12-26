package com.gumraze.drive.drive_backend.region.repository;

import com.gumraze.drive.drive_backend.region.entity.RegionProvince;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaRegionProvinceRepository extends JpaRepository<RegionProvince, Long> {
    /***
     * id 오름차순으로 정렬된 시/도 데이터 조회
     */
    List<RegionProvince> findAllByOrderById();
}
