package com.gumraze.rallyon.backend.courtManager.application.port.in.command;

import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.Gender;

import java.util.List;

/**
 * AI 코트 배정 프리뷰 유스케이스의 application 입력이다.
 *
 * @param participants AI가 배정 대상으로 판단할 전체 참가자 목록
 * @param rounds 현재 라운드/코트/슬롯 배정 상태
 * @param partnerPairs 사용자가 지정한 파트너 pair 목록
 * @param preferences AI 배정 시 적용할 사용자 선택 정책
 */
public record CreateFreeGameAssignmentPreviewCommand (
        List<Participant> participants,
        List<Round> rounds,
        List<PartnerPairs> partnerPairs,
        Preferences preferences
) {
    /**
     * AI가 배정 대상으로 판단할 참가자 정보다.
     *
     * @param participantId application 내부에서 슬롯 배정 식별에 사용할 참가자 ID
     * @param gender 참가자 성별
     * @param ageGroup 참가자 연령대
     * @param grade 참가자 급수
     * @param gamesAssigned 현재 기준으로 이미 배정된 경기 수
     */
    public record Participant(
            Long participantId,
            Gender gender,
            Integer ageGroup,
            Grade grade,
            Integer gamesAssigned
    ) {
        public Participant(
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
     * 한 라운드의 현재 배정 상태다.
     *
     * @param roundNumber 라운드 순번
     * @param courts 해당 라운드에 속한 코트와 슬롯 상태
     */
    public record Round(
            Integer roundNumber,
            List<Court> courts
    ) {}

    /**
     * 한 코트의 현재 슬롯 상태다.
     *
     * @param courtNumber 코트 순번
     * @param slots 각 슬롯에 배정된 participant participantId 목록
     */
    public record Court(
            Integer courtNumber,
            List<Long> slots
    ) {}

    /**
     * 순서가 없는 파트너 pair다.
     *
     * @param participantId1 파트너 pair의 첫 번째 참가자 ID
     * @param participantId2 파트너 pair의 두 번째 참가자 ID
     */
    public record PartnerPairs(
            Long participantId1,
            Long participantId2
    ) {}

    /**
     * AI 배정 시 적용할 사용자 선택 정책이다.
     *
     * @param partnerPolicy 파트너 pair를 우선 반영할지 여부
     * @param existingAssignmentPolicy 현재 슬롯을 유지할지, 전체를 다시 배정할지 여부
     */
    public record Preferences(
            PartnerPolicy partnerPolicy,
            ExistingAssignmentPolicy existingAssignmentPolicy
    ) {
    }

    public enum PartnerPolicy {
        PREFER_PARTNERS,
        IGNORE_PARTNERS
    }

    public enum ExistingAssignmentPolicy {
        FILL_EMPTY_SLOTS,
        REASSIGN_ALL
    }
}
