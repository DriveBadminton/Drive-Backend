package com.gumraze.rallyon.backend.courtManager.controller;

import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.AssignmentPreviewAiResponse;
import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.OpenAiAssignmentPreviewClient;
import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.OpenAiAssignmentPreviewGateway;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class OpenAiAssignmentPreviewClientTest {

    @Mock
    private OpenAiAssignmentPreviewGateway gateway;

    @InjectMocks
    private OpenAiAssignmentPreviewClient client;

    @Test
    @DisplayName("OpenAI 응답을 AI 프리뷰 구조롤 반환한다.")
    void generate_withValidResponse_returnsAiResponse() {
        // given: 프리뷰 생성 입력과 OpenAi 응답 준비
        CreateFreeGameAssignmentPreviewCommand command = previewCommand();

        AssignmentPreviewAiResponse expected = previewResponse();

        given(gateway.generate(command))
                .willReturn(expected);

        // when: OpenAI assignment preview 생성 수행
        AssignmentPreviewAiResponse response = client.generate(command);

        // then: OpenAI 응답 반환 검증
        then(response).isEqualTo(expected);
        verify(gateway).generate(command);

    }

    @Test
    @DisplayName("OpenAI gateway 호출이 실패하면 예외를 그대로 전달한다.")
    void generate_whenGatewayFails_throwsRuntimeException() {
        // given: 프리뷰 생성 입력과 OpenAI gateway 실패 준비
        CreateFreeGameAssignmentPreviewCommand command = previewCommand();

        given(gateway.generate(command))
                .willThrow(new RuntimeException("OpenAI timeout"));

        // when & then: gateway 예외 전달 검증
        assertThatThrownBy(() -> client.generate(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("OpenAI timeout");
    }


    private CreateFreeGameAssignmentPreviewCommand previewCommand() {
        return new CreateFreeGameAssignmentPreviewCommand(
                List.of(
                        new CreateFreeGameAssignmentPreviewCommand.Participant(
                                "p1",
                                "서승재",
                                Gender.MALE,
                                20,
                                Grade.SS,
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
    }

    private AssignmentPreviewAiResponse previewResponse() {
        return new AssignmentPreviewAiResponse(
                List.of(
                        new AssignmentPreviewAiResponse.Round(
                                1,
                                List.of(
                                        new AssignmentPreviewAiResponse.Court(
                                                1,
                                                Arrays.asList("p1", "p2", null, null)
                                        )
                                )
                        )
                ),
                List.of(
                        new AssignmentPreviewAiResponse.Warning(
                                "PARTIAL_ASSIGNMENT",
                                "일부 슬롯은 비어 있습니다."
                        )
                )
        );
    }

}
