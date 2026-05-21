package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * AI 코트 배정 프리뷰 생성을 위한 요청이다.
 *
 * <p>프론트는 현재 자유게임 생성 화면에서 관리 중인 참가자, 코트 슬롯,
 * 파트너 관계, 배정 선호 정책을 이 요청으로 전달한다.
 *
 * <p>이 요청은 최종 자유게임 저장을 위한 payload가 아니라,
 * AI가 배정 초안을 계산하기 위한 입력 데이터다.
 *
 * @param participants 현재 화면에서 배정 가능한 전체 참가자 목록
 * @param rounds 현재 라운드/코트/슬롯 배정 상태
 * @param partnerPairs 사용자가 지정한 파트너 pair 목록, 없으면 null 또는 빈 리스트일 수 있음
 * @param preferences 사용자가 선택한 AI 배정 선호 정책
 */
public record CreateFreeGameAssignmentPreviewRequest(
        @NotEmpty
        @Valid
        List<ParticipantRequest> participants,

        @NotEmpty
        @Valid
        List<RoundRequest> rounds,

        @Valid
        List<PartnerPairRequest> partnerPairs,

        @NotNull
        @Valid
        PreferencesRequest preferences
) {
    /**
     * 현재 화면에서 배정 가능한 참가자 정보다.
     * {@code gamesAssigned}는 현재 프론트 화면에서 이미 배정된 경기 수를 뜻한다.
     *
     * @param participantId 프론트 화면에서 참가자를 식별하는 임시 ID
     * @param gender 참가자 성별
     * @param ageGroup 참가자 연령대
     * @param grade 참가자 급수
     * @param gamesAssigned 현재 화면 기준으로 이미 배정된 경기 수
     */
    public record ParticipantRequest(
            @NotNull
            Long participantId,

            @NotNull
            Gender gender,

            @NotNull
            @Min(10)
            @Max(70)
            Integer ageGroup,

            @NotNull
            Grade grade,

            @NotNull
            @Min(0)
            Integer gamesAssigned
    ) {
        public ParticipantRequest(
                Long participantId,
                String unusedName,
                Gender gender,
                Integer ageGroup,
                Grade grade,
                Integer gamesAssigned
        ) {
            this(participantId, gender, ageGroup, grade, gamesAssigned);
        }
    }

    /**
     * 한 라운드에 대한 현재 화면 상태다.
     *
     * <p>roundNumber를 기준으로 프론트의 라운드 순서를 유지하고,
     * 그 안의 courts는 각 코트 슬롯의 현재 배정 상태를 그대로 전달한다.
     *
     * @param roundNumber 프론트 화면에서의 라운드 순번
     * @param courts 해당 라운드에 속한 코트와 슬롯 상태
     */
    public record RoundRequest(
            @NotNull
            @Min(1)
            Integer roundNumber,

            @NotEmpty
            @Valid
            List<CourtRequest> courts
    ) {
    }

    /**
     * 한 코트의 슬롯 상태다.
     *
     * <p>slots는 항상 4칸이며,
     * 각 값은 participants에 포함된 participantId를 참조한다.
     *
     * <p>null은 아직 비어 있는 슬롯을 뜻한다.
     *
     * @param courtNumber 라운드 안에서의 코트 순번
     * @param slots 코트의 4개 슬롯 상태, 각 값은 participant의 participantId 또는 null
     */
    public record CourtRequest(
            @NotNull
            @Min(1)
            Integer courtNumber,

            @NotNull
            @Size(min = 4, max = 4)
            List<Long> slots
    ) {
    }

    /**
     * 순서가 없는 파트너 쌍이다.
     *
     * <p>participantId1, participantId2는 화면 좌우 배치를 뜻하지 않고,
     * 하나의 pair를 표현하기 위한 두 식별자일 뿐이다.
     *
     * @param participantId1 파트너 pair의 첫 번째 참가자 ID
     * @param participantId2 파트너 pair의 두 번째 참가자 ID
     */
    public record PartnerPairRequest(
            @NotNull
            Long participantId1,

            @NotNull
            Long participantId2
    ) {
    }

    /**
     * 사용자가 선택한 AI 배정 선호 정책이다.
     *
     * <p>현재 프론트에서 직접 조정하는 정책만 이 요청에 포함한다.
     * 서버의 고정 정책은 별도로 적용하되, v1 응답에는 포함하지 않는다.
     *
     * @param partnerPolicy 파트너 pair를 우선적으로 같은 코트 슬롯에 반영할지 여부
     * @param existingAssignmentPolicy 현재 슬롯 배정을 유지할지, 전체를 다시 배정할지 여부
     */
    public record PreferencesRequest(
            @NotNull
            PartnerPolicy partnerPolicy,

            @NotNull
            ExistingAssignmentPolicy existingAssignmentPolicy
    ) {
    }

    /**
     * 설정된 파트너 쌍을 우선적으로 같은 코트 슬롯에 반영할지 나타낸다.
     */
    public enum PartnerPolicy {
        PREFER_PARTNERS,
        IGNORE_PARTNERS
    }

    /**
     * 현재 화면에서 이미 배정된 슬롯을 유지할지, 전체를 다시 배정할지 나타낸다.
     */
    public enum ExistingAssignmentPolicy {
        FILL_EMPTY_SLOTS,
        REASSIGN_ALL
    }
}
