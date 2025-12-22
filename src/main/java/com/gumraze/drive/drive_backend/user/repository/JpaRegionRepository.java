package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRegionRepository extends JpaRepository<Region, Long> {
}
