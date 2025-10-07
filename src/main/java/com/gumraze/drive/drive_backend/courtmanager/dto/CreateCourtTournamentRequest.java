package com.gumraze.drive.drive_backend.courtmanager.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "코트 매니저 대회 생성 요청")
public record CreateCourtTournamentRequest(
    @Schema(description = "대회 공개 여부", example = "PUBLIC")
    @NotBlank(message = "visibility is required")
    String visibility,
    @Schema(description = "대회 이름", example = "2025 가을 리그")
    @NotBlank(message = "tournament name is required")
    @Size(max = 255, message = "tournament name must be 255 characters or less")
    String name,
    @Schema(description = "참가자 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "at least one participant is required")
    List<@Valid ParticipantRequest> participants
) {

    @Schema(description = "참가자 정보")
    public record ParticipantRequest(
        @Schema(description = "참가자 성별", example = "남성")
        @NotBlank(message = "participant gender is required")
        String gender,
        @Schema(description = "참가자 급수", example = "B급")
        @NotBlank(message = "participant grade is required")
        String grade,
        @Schema(description = "참가자 이름", example = "홍길동")
        @NotBlank(message = "participant name is required")
        @Size(max = 150, message = "participant name must be 150 characters or less")
        String name
    ) {
    }
}
