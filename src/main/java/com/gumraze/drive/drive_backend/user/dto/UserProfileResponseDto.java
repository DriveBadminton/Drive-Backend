package com.gumraze.drive.drive_backend.user.dto;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.region.entity.RegionProvince;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.constants.UserRole;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.entity.UserProfile;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserProfileResponseDto {

    Long id;
    UserRole role;
    UserStatus status;

    Grade regionalGrade;
    Grade nationalGrade;
    String provinceName;
    String districtName;

    /**
     * Create a UserProfileResponseDto populated from a User and an optional UserProfile.
     *
     * @param user    the source User whose id, role, and status will populate the DTO
     * @param profile the optional UserProfile; when null (or when its region/province are null),
     *                regionalGrade, nationalGrade, provinceName, and districtName will be null
     * @return        a UserProfileResponseDto containing user fields and profile-derived grades and region names
     */
    public static UserProfileResponseDto from(
            User user,
            UserProfile profile
    ) {
        RegionDistrict district = profile != null ? profile.getRegionDistrict() : null;
        RegionProvince province = district != null ? district.getProvince() : null;

        return UserProfileResponseDto.builder()
                .id(user.getId())
                .role(user.getRole())
                .status(user.getStatus())
                .regionalGrade(profile != null ? profile.getRegionalGrade() : null)
                .nationalGrade(profile != null ? profile.getNationalGrade() : null)
                .provinceName(province != null ? province.getName() : null)
                .districtName(district != null ? district.getName() : null)
                .build();
    }
}