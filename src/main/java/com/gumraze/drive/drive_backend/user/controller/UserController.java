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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Users", description = "사용자 프로필 API")
public class UserController {

    private final UserService userService;
    private final JpaUserProfileRepository jpaUserProfileRepository;
    private final JpaUserGradeHistoryRepository jpaUserGradeHistoryRepository;

    @GetMapping("/me")
    @Operation(
            summary = "내 프로필 상태 조회",
            description = "Access Token 기반으로 사용자 정보를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(
            summary = "프로필 생성",
            description = "닉네임/지역/등급을 입력해 프로필을 생성하고 계정을 ACTIVE로 전환합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserProfileCreateRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "nickname": "kim",
                                              "regionId": 1,
                                              "grade": "초심"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 생성 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 검증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
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
