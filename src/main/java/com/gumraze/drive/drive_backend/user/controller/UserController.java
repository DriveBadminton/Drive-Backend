package com.gumraze.drive.drive_backend.user.controller;

import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.region.Region;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateRequest;
import com.gumraze.drive.drive_backend.user.dto.UserProfileResponseDto;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.entity.UserGradeHistory;
import com.gumraze.drive.drive_backend.user.entity.UserProfile;
import com.gumraze.drive.drive_backend.user.repository.JpaUserGradeHistoryRepository;
import com.gumraze.drive.drive_backend.user.repository.JpaUserProfileRepository;
import com.gumraze.drive.drive_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JpaUserProfileRepository jpaUserProfileRepository;
    private final JpaUserGradeHistoryRepository jpaUserGradeHistoryRepository;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> me() {

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Long userId = (Long) authentication.getPrincipal();

        return userService.findById(userId)
                .map(UserProfileResponseDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<?>> createProfile(
            @RequestBody UserProfileCreateRequest request
    ) {
        // Spring Security가 저장해둔 인증된 사용자 정보를 가져오는 메서드
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Long userId = (Long) authentication.getPrincipal();

        User user = userService.findById(userId).orElseThrow();

        // Region은 id만 필요 -> Proxy사용
        Region region = new Region();
        Field idField = ReflectionUtils.findField(Region.class, "id");
        ReflectionUtils.makeAccessible(idField);
        ReflectionUtils.setField(idField, region, request.regionId());

        // UserProfile 생성
        UserProfile profile = new UserProfile(
                userId,
                request.nickname(),
                request.grade(),
                region
        );

        jpaUserProfileRepository.save(profile);

        // UserGradeHistory 초기 생성
        jpaUserGradeHistoryRepository.save(
                new UserGradeHistory(user, request.grade())
        );
        // User 상채 ACTIVE 전환
        user.setStatus(UserStatus.ACTIVE);

        return ResponseEntity.ok(
                ApiResponse.success("프로필 생성 완료", null)
        );
    }
}
