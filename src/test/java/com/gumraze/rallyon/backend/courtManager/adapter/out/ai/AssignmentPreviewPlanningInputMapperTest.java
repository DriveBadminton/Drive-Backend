package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class AssignmentPreviewPlanningInputMapperTest {

    private final AssignmentPreviewPlanningInputMapper mapper =
            new AssignmentPreviewPlanningInputMapper();

    @Test
    @DisplayName("planning input에는 participant의 clientId와 gamesAssigned만 포함한다.")
    void from_mapsSlimParticipantPayload() {
        AssignmentPreviewPlanningInput result = mapper.from(fillEmptySlotsCommand());

        then(result.participants()).containsExactly(
                new AssignmentPreviewPlanningInput.Participant("p1", 1)
        );
    }

    @Test
    @DisplayName("빈 슬롯만 채우기 정책이면 기존 슬롯은 fixed true로 변환한다.")
    void from_whenFillEmptySlots_mapsFixedFlags() {
        AssignmentPreviewPlanningInput result = mapper.from(fillEmptySlotsCommand());

        then(result.rounds().getFirst().courts().getFirst().slots())
                .containsExactly(
                        new AssignmentPreviewPlanningInput.Slot("p1", true),
                        new AssignmentPreviewPlanningInput.Slot(null, false),
                        new AssignmentPreviewPlanningInput.Slot(null, false),
                        new AssignmentPreviewPlanningInput.Slot(null, false)
                );
    }

    @Test
    @DisplayName("전체 다시 배정 정책이면 모든 슬롯은 fixed false로 변환한다.")
    void from_whenReassignAll_mapsAllSlotsAsMutable() {
        AssignmentPreviewPlanningInput result = mapper.from(reassignAllCommand());

        then(result.rounds().getFirst().courts().getFirst().slots())
                .containsExactly(
                        new AssignmentPreviewPlanningInput.Slot("p1", false),
                        new AssignmentPreviewPlanningInput.Slot(null, false),
                        new AssignmentPreviewPlanningInput.Slot(null, false),
                        new AssignmentPreviewPlanningInput.Slot(null, false)
                );
    }

    @Test
    @DisplayName("정책과 파트너 정보는 guidance 하나로 압축한다.")
    void from_mapsUnifiedGuidance() {
        AssignmentPreviewPlanningInput result = mapper.from(fillEmptySlotsWithPartnerCommand());

        then(result.guidance()).isEqualTo(
                new AssignmentPreviewPlanningInput.Guidance(true, true, true, 1)
        );
    }

    @Test
    @DisplayName("파트너 무시와 전체 재배정 정책이면 negative guidance로 변환한다.")
    void from_whenIgnoringPartnersAndReassignAll_mapsNegativeGuidance() {
        AssignmentPreviewPlanningInput result = mapper.from(ignorePartnersAndReassignAllCommand());

        then(result.guidance()).isEqualTo(
                new AssignmentPreviewPlanningInput.Guidance(false, false, false, 0)
        );
    }

    private CreateFreeGameAssignmentPreviewCommand fillEmptySlotsCommand() {
        return new CreateFreeGameAssignmentPreviewCommand(
                List.of(participant("p1", "서승재", 1)),
                List.of(roundWithSingleCourt("p1")),
                List.of(),
                new CreateFreeGameAssignmentPreviewCommand.Preferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );
    }

    private CreateFreeGameAssignmentPreviewCommand fillEmptySlotsWithPartnerCommand() {
        return new CreateFreeGameAssignmentPreviewCommand(
                List.of(
                        participant("p1", "서승재", 1),
                        participant("p2", "김원호", 0)
                ),
                List.of(roundWithSingleCourt("p1")),
                List.of(new CreateFreeGameAssignmentPreviewCommand.PartnerPairs("p1", "p2")),
                new CreateFreeGameAssignmentPreviewCommand.Preferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );
    }

    private CreateFreeGameAssignmentPreviewCommand reassignAllCommand() {
        return new CreateFreeGameAssignmentPreviewCommand(
                List.of(participant("p1", "서승재", 1)),
                List.of(roundWithSingleCourt("p1")),
                List.of(),
                new CreateFreeGameAssignmentPreviewCommand.Preferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.REASSIGN_ALL
                )
        );
    }

    private CreateFreeGameAssignmentPreviewCommand ignorePartnersAndReassignAllCommand() {
        return new CreateFreeGameAssignmentPreviewCommand(
                List.of(
                        participant("p1", "서승재", 1),
                        participant("p2", "김원호", 0)
                ),
                List.of(roundWithSingleCourt("p1")),
                List.of(new CreateFreeGameAssignmentPreviewCommand.PartnerPairs("p1", "p2")),
                new CreateFreeGameAssignmentPreviewCommand.Preferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.IGNORE_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.REASSIGN_ALL
                )
        );
    }

    private CreateFreeGameAssignmentPreviewCommand.Participant participant(
            String clientId,
            String name,
            int gamesAssigned
    ) {
        return new CreateFreeGameAssignmentPreviewCommand.Participant(
                clientId,
                name,
                Gender.MALE,
                20,
                Grade.S,
                gamesAssigned
        );
    }

    private CreateFreeGameAssignmentPreviewCommand.Round roundWithSingleCourt(String firstSlotParticipantId) {
        return new CreateFreeGameAssignmentPreviewCommand.Round(
                1,
                List.of(
                        new CreateFreeGameAssignmentPreviewCommand.Court(
                                1,
                                Arrays.asList(firstSlotParticipantId, null, null, null)
                        )
                )
        );
    }
}
