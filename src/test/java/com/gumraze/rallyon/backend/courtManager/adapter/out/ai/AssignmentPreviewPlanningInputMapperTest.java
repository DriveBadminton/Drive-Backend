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
    @DisplayName("빈 슬롯만 채우기 정책이면 기존 슬롯은 fixed true, 빈 슬롯은 fixed false로 변환한다.")
    void from_whenFillEmptySlots_mapsFixedFlags() {
        // given: 기존 슬롯 1개와 빈 슬롯 3개가 있는 preview command를 준비한다.
        CreateFreeGameAssignmentPreviewCommand command =
                new CreateFreeGameAssignmentPreviewCommand(
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Participant(
                                        "p1",
                                        "서승재",
                                        Gender.MALE,
                                        20,
                                        Grade.S,
                                        1
                                )
                        ),
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Round(
                                        1,
                                        List.of(
                                                new CreateFreeGameAssignmentPreviewCommand.Court(
                                                        1,
                                                        Arrays.asList("p1", null, null, null)
                                                )
                                        )
                                )
                        ),
                        List.of(),
                        new CreateFreeGameAssignmentPreviewCommand.Preferences(
                                CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                                CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                        )
                );

        // when: planning input으로 변환한다.
        AssignmentPreviewPlanningInput result = mapper.from(command);

        // then: 기존 non-null 슬롯만 fixed true가 된다.
        then(result.rounds().getFirst().courts().getFirst().slots())
                .containsExactly(
                        new AssignmentPreviewPlanningInput.Slot(0, "p1", true),
                        new AssignmentPreviewPlanningInput.Slot(1, null, false),
                        new AssignmentPreviewPlanningInput.Slot(2, null, false),
                        new AssignmentPreviewPlanningInput.Slot(3, null, false)
                );
        then(result.preferences().existingAssignmentPolicy()).isEqualTo("FILL_EMPTY_SLOTS");
    }


    @Test
    @DisplayName("전체 다시 배정 정책이면 기존 슬롯도 fixed false로 변환한다.")
    void from_whenReassignAll_mapsAllSlotsAsMutable() {
        // given: 기존 슬롯 1개가 있지만 전체 다시 배정 정책인 preview command를 준비한다.
        CreateFreeGameAssignmentPreviewCommand command =
                new CreateFreeGameAssignmentPreviewCommand(
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Participant(
                                        "p1",
                                        "서승재",
                                        Gender.MALE,
                                        20,
                                        Grade.S,
                                        1
                                )
                        ),
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Round(
                                        1,
                                        List.of(
                                                new CreateFreeGameAssignmentPreviewCommand.Court(
                                                        1,
                                                        Arrays.asList("p1", null, null, null)
                                                )
                                        )
                                )
                        ),
                        List.of(),
                        new CreateFreeGameAssignmentPreviewCommand.Preferences(
                                CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                                CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.REASSIGN_ALL
                        )
                );

        // when: planning input으로 변환한다.
        AssignmentPreviewPlanningInput result = mapper.from(command);

        // then: 기존 non-null 슬롯도 fixed false가 된다.
        then(result.rounds().getFirst().courts().getFirst().slots())
                .containsExactly(
                        new AssignmentPreviewPlanningInput.Slot(0, "p1", false),
                        new AssignmentPreviewPlanningInput.Slot(1, null, false),
                        new AssignmentPreviewPlanningInput.Slot(2, null, false),
                        new AssignmentPreviewPlanningInput.Slot(3, null, false)
                );
        then(result.preferences().existingAssignmentPolicy()).isEqualTo("REASSIGN_ALL");
    }

    @Test
    @DisplayName("빈 슬롯만 채우기 정책이면 constraint guidance를 함께 변환한다.")
    void from_whenFillEmptySlots_mapsConstraintGuidance() {
        // given: 빈 슬롯만 채우기 정책의 preview command를 준비한다.
        CreateFreeGameAssignmentPreviewCommand command =
                new CreateFreeGameAssignmentPreviewCommand(
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Participant(
                                        "p1",
                                        "서승재",
                                        Gender.MALE,
                                        20,
                                        Grade.S,
                                        1
                                )
                        ),
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Round(
                                        1,
                                        List.of(
                                                new CreateFreeGameAssignmentPreviewCommand.Court(
                                                        1,
                                                        Arrays.asList("p1", null, null, null)
                                                )
                                        )
                                )
                        ),
                        List.of(),
                        new CreateFreeGameAssignmentPreviewCommand.Preferences(
                                CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                                CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                        )
                );

        // when: planning input으로 변환한다.
        AssignmentPreviewPlanningInput result = mapper.from(command);

        // then: 구조 유지와 고정 슬롯 유지 제약이 구조화된 guidance에 반영된다.
        then(result.constraintGuidance()).isEqualTo(
                new AssignmentPreviewPlanningInput.ConstraintGuidance(true, true, true)
        );
    }

    @Test
    @DisplayName("정책과 파트너 정보를 guidance 구조로 변환한다.")
    void from_mapsPolicyAndPartnerGuidance() {
        // given: fixed slot과 선호 파트너가 있는 preview command를 준비한다.
        CreateFreeGameAssignmentPreviewCommand command =
                new CreateFreeGameAssignmentPreviewCommand(
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Participant(
                                        "p1",
                                        "서승재",
                                        Gender.MALE,
                                        20,
                                        Grade.S,
                                        1
                                ),
                                new CreateFreeGameAssignmentPreviewCommand.Participant(
                                        "p2",
                                        "김원호",
                                        Gender.MALE,
                                        20,
                                        Grade.S,
                                        0
                                )
                        ),
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Round(
                                        1,
                                        List.of(
                                                new CreateFreeGameAssignmentPreviewCommand.Court(
                                                        1,
                                                        Arrays.asList("p1", null, null, null)
                                                )
                                        )
                                )
                        ),
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.PartnerPairs("p1", "p2")
                        ),
                        new CreateFreeGameAssignmentPreviewCommand.Preferences(
                                CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                                CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                        )
                );

        // when: planning input으로 변환한다.
        AssignmentPreviewPlanningInput result = mapper.from(command);

        // then: 정책과 파트너 의미가 guidance 구조에 반영된다.
        then(result.constraintGuidance()).isEqualTo(
                new AssignmentPreviewPlanningInput.ConstraintGuidance(true, true, true)
        );
        then(result.policyGuidance()).isEqualTo(new AssignmentPreviewPlanningInput.PolicyGuidance(true));
        then(result.partnerGuidance()).isEqualTo(
                new AssignmentPreviewPlanningInput.PartnerGuidance(true, 1)
        );
    }

    @Test
    @DisplayName("파트너 무시와 전체 재배정 정책이면 negative guidance로 변환한다.")
    void from_whenIgnoringPartnersAndReassignAll_mapsNegativeGuidance() {
        // given: partner pair는 있지만 정책상 무시하고, 기존 슬롯도 유지하지 않는 preview command를 준비한다.
        CreateFreeGameAssignmentPreviewCommand command =
                new CreateFreeGameAssignmentPreviewCommand(
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Participant(
                                        "p1",
                                        "서승재",
                                        Gender.MALE,
                                        20,
                                        Grade.S,
                                        1
                                ),
                                new CreateFreeGameAssignmentPreviewCommand.Participant(
                                        "p2",
                                        "김원호",
                                        Gender.MALE,
                                        20,
                                        Grade.S,
                                        0
                                )
                        ),
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Round(
                                        1,
                                        List.of(
                                                new CreateFreeGameAssignmentPreviewCommand.Court(
                                                        1,
                                                        Arrays.asList("p1", null, null, null)
                                                )
                                        )
                                )
                        ),
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.PartnerPairs("p1", "p2")
                        ),
                        new CreateFreeGameAssignmentPreviewCommand.Preferences(
                                CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.IGNORE_PARTNERS,
                                CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.REASSIGN_ALL
                        )
                );

        // when: planning input으로 변환한다.
        AssignmentPreviewPlanningInput result = mapper.from(command);

        // then: fixed slot 유지와 partner 선호가 모두 false가 된다.
        then(result.constraintGuidance()).isEqualTo(
                new AssignmentPreviewPlanningInput.ConstraintGuidance(true, false, true)
        );
        then(result.policyGuidance()).isEqualTo(new AssignmentPreviewPlanningInput.PolicyGuidance(false));
        then(result.partnerGuidance()).isEqualTo(
                new AssignmentPreviewPlanningInput.PartnerGuidance(false, 0)
        );
    }

    @Test
    @DisplayName("전체 다시 배정 정책이면 constraint guidance에서 fixed slot 유지가 false다.")
    void from_whenReassignAll_setsFixedSlotConstraintToFalse() {
        // given: 전체 다시 배정 정책의 preview command를 준비한다.
        CreateFreeGameAssignmentPreviewCommand command =
                new CreateFreeGameAssignmentPreviewCommand(
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Participant(
                                        "p1",
                                        "서승재",
                                        Gender.MALE,
                                        20,
                                        Grade.S,
                                        1
                                )
                        ),
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Round(
                                        1,
                                        List.of(
                                                new CreateFreeGameAssignmentPreviewCommand.Court(
                                                        1,
                                                        Arrays.asList("p1", null, null, null)
                                                )
                                        )
                                )
                        ),
                        List.of(),
                        new CreateFreeGameAssignmentPreviewCommand.Preferences(
                                CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                                CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.REASSIGN_ALL
                        )
                );

        // when: planning input으로 변환한다.
        AssignmentPreviewPlanningInput result = mapper.from(command);

        // then: fixed slot 유지 제약은 false로 내려간다.
        then(result.constraintGuidance()).isEqualTo(
                new AssignmentPreviewPlanningInput.ConstraintGuidance(true, false, true)
        );
    }





}
