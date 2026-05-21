package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;
import com.gumraze.rallyon.backend.courtManager.application.port.out.GenerateFreeGameAssignmentPreviewPort;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CreateFreeGameAssignmentPreviewServiceTest {

    @Mock
    private GenerateFreeGameAssignmentPreviewPort generateFreeGameAssignmentPreviewPort;

    @InjectMocks
    private CreateFreeGameAssignmentPreviewService service;

    @Test
    @DisplayName("AI 코트 배정 프리뷰를 생성한다")
    void create_returnsPreviewResult() {
        // given
        CreateFreeGameAssignmentPreviewCommand command =
                new CreateFreeGameAssignmentPreviewCommand(
                        List.of(
                                participant(1L, "서승재", 1)
                        ),
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Round(
                                        1,
                                        List.of(
                                                new CreateFreeGameAssignmentPreviewCommand.Court(
                                                        1,
                                                        Arrays.asList(1L, null, null, null)
                                                )
                                        )
                                )
                        ),
                        List.of(),
                        getPreferences()
                );
        CreateFreeGameAssignmentPreviewResult result =
                new CreateFreeGameAssignmentPreviewResult(
                        List.of(),
                        List.of()
                );

        given(generateFreeGameAssignmentPreviewPort.generate(command)).willReturn(result);

        // when
        CreateFreeGameAssignmentPreviewResult preview = service.create(command);

        // then
        then(preview).isEqualTo(result);
        verify(generateFreeGameAssignmentPreviewPort).generate(command);
    }

    private static CreateFreeGameAssignmentPreviewCommand.Preferences getPreferences() {
        return new CreateFreeGameAssignmentPreviewCommand.Preferences(
                CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
        );
    }

    // helper method
    private CreateFreeGameAssignmentPreviewCommand.Participant participant(
            Long participantId,
            String name,
            Integer gamesAssigned
    ) {
        return new CreateFreeGameAssignmentPreviewCommand.Participant(
                participantId,
                name,
                Gender.MALE,
                20,
                Grade.S,
                gamesAssigned
        );

    }

}
