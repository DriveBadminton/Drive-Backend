package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameAssignmentPreviewRequest;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class CreateFreeGameAssignmentPreviewCommandMapperTest {

    private final CreateFreeGameAssignmentPreviewCommandMapper mapper =
            new CreateFreeGameAssignmentPreviewCommandMapper();

    @Test
    @DisplayName("assignment preview request를 assignment preview command로 변환한다")
    void toCommand_mapsRequestToCommand() {
        // given
        CreateFreeGameAssignmentPreviewRequest request =
                new CreateFreeGameAssignmentPreviewRequest(
                        List.of(
                                participantRequest(1L, "서승재", Gender.MALE, 20, Grade.S, 1),
                                participantRequest(2L, "김원호", Gender.FEMALE, 30, Grade.A, 0)
                        ),
                        List.of(
                                new CreateFreeGameAssignmentPreviewRequest.RoundRequest(
                                        1,
                                        List.of(
                                                new CreateFreeGameAssignmentPreviewRequest.CourtRequest(
                                                        1,
                                                        Arrays.asList(1L, null, null, null)
                                                ),
                                                new CreateFreeGameAssignmentPreviewRequest.CourtRequest(
                                                        2,
                                                        Arrays.asList(null, null, null, null)
                                                )
                                        )
                                )
                        ),
                        List.of(
                                new CreateFreeGameAssignmentPreviewRequest.PartnerPairRequest(
                                        1L,
                                        2L
                                )
                        ),
                        new CreateFreeGameAssignmentPreviewRequest.PreferencesRequest(
                                CreateFreeGameAssignmentPreviewRequest.PartnerPolicy.PREFER_PARTNERS,
                                CreateFreeGameAssignmentPreviewRequest.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                        )
                );

        // when
        CreateFreeGameAssignmentPreviewCommand command = mapper.toCommand(request);

        // then
        then(command.participants()).hasSize(2);

        then(command.participants().get(0).participantId()).isEqualTo(1L);
        then(command.participants().get(0).gender()).isEqualTo(Gender.MALE);
        then(command.participants().get(0).ageGroup()).isEqualTo(20);
        then(command.participants().get(0).grade()).isEqualTo(Grade.S);
        then(command.participants().get(0).gamesAssigned()).isEqualTo(1);

        then(command.participants().get(1).participantId()).isEqualTo(2L);
        then(command.participants().get(1).gender()).isEqualTo(Gender.FEMALE);
        then(command.participants().get(1).ageGroup()).isEqualTo(30);
        then(command.participants().get(1).grade()).isEqualTo(Grade.A);
        then(command.participants().get(1).gamesAssigned()).isEqualTo(0);

        then(command.rounds()).hasSize(1);
        then(command.rounds().get(0).roundNumber()).isEqualTo(1);
        then(command.rounds().get(0).courts()).hasSize(2);
        then(command.rounds().get(0).courts().get(0).courtNumber()).isEqualTo(1);
        then(command.rounds().get(0).courts().get(0).slots())
                .containsExactly(1L, null, null, null);

        then(command.partnerPairs()).hasSize(1);
        then(command.partnerPairs().get(0).participantId1()).isEqualTo(1L);
        then(command.partnerPairs().get(0).participantId2()).isEqualTo(2L);

        then(command.preferences().partnerPolicy())
                .isEqualTo(CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS);
        then(command.preferences().existingAssignmentPolicy())
                .isEqualTo(CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS);
    }

    @Test
    @DisplayName("partnerPairs가 null이면 빈 리스트로 변환한다")
    void toCommand_whenPartnerPairsIsNull_returnsEmptyList() {
        // given
        CreateFreeGameAssignmentPreviewRequest request =
                new CreateFreeGameAssignmentPreviewRequest(
                        List.of(participantRequest(1L, "서승재", Gender.MALE, 20, Grade.S, 1)),
                        List.of(
                                new CreateFreeGameAssignmentPreviewRequest.RoundRequest(
                                        1,
                                        List.of(
                                                new CreateFreeGameAssignmentPreviewRequest.CourtRequest(
                                                        1,
                                                        Arrays.asList(null, null, null, null)
                                                )
                                        )
                                )
                        ),
                        null,
                        new CreateFreeGameAssignmentPreviewRequest.PreferencesRequest(
                                CreateFreeGameAssignmentPreviewRequest.PartnerPolicy.IGNORE_PARTNERS,
                                CreateFreeGameAssignmentPreviewRequest.ExistingAssignmentPolicy.REASSIGN_ALL
                        )
                );

        // when
        CreateFreeGameAssignmentPreviewCommand command = mapper.toCommand(request);

        // then
        then(command.partnerPairs()).isEmpty();
        then(command.preferences().partnerPolicy())
                .isEqualTo(CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.IGNORE_PARTNERS);
        then(command.preferences().existingAssignmentPolicy())
                .isEqualTo(CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.REASSIGN_ALL);

    }


    // helper method
    private CreateFreeGameAssignmentPreviewRequest.ParticipantRequest participantRequest(
            Long participantId,
            String name,
            Gender gender,
            Integer ageGroup,
            Grade grade,
            Integer gamesAssigned
    ) {
        return new CreateFreeGameAssignmentPreviewRequest.ParticipantRequest(
                participantId,
                name,
                gender,
                ageGroup,
                grade,
                gamesAssigned
        );
    }
}
