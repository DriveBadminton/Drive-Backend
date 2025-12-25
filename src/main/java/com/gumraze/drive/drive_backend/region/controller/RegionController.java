package com.gumraze.drive.drive_backend.region.controller;

import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.region.dto.RegionDistrictResponseDto;
import com.gumraze.drive.drive_backend.region.dto.RegionProvinceResponseDto;
import com.gumraze.drive.drive_backend.region.service.RegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/regions")
public class RegionController {

    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }


    @GetMapping(value = "/provinces")
    public ResponseEntity<ApiResponse<List<RegionProvinceResponseDto>>> getProvinces() {

        List<RegionProvinceResponseDto> body = regionService.getProvinces().stream()
                .map(p -> new RegionProvinceResponseDto(
                        p.getId(),
                        p.getName()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(body));
    }

    @GetMapping(value = "/{provinceId}/districts")
    public ResponseEntity<ApiResponse<List<RegionDistrictResponseDto>>> getDistricts(
            @PathVariable Long provinceId
    ) {
        List<RegionDistrictResponseDto> body = regionService.getDistricts(provinceId).stream()
                .map(d -> new RegionDistrictResponseDto(
                        d.getId(),
                        d.getName()
                ))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(body));
    }
}
