package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.common.exception.ServiceUnavailableException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult;
import com.gumraze.rallyon.backend.courtManager.application.port.out.result.FreeGameAssignmentPreviewGeneration;
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
public class AssignmentPreviewAiAdapterTest {

    @Mock private AssignmentPreviewAiGateway aiGateway;
    @InjectMocks private AssignmentPreviewAiAdapter adapter;

    @Test
    @DisplayName("AI 응답을 코트 배정 프리뷰 결과로 변환한다.")
    void generate_withValidAiResponse_returnsPreviewResult() {
        // given: 프리뷰 생성 입력과 AI 응답 준비
        CreateFreeGameAssignmentPreviewCommand command = previewCommand();
        AssignmentPreviewAiResponse aiResponse = previewAiResponse();
        AssignmentPreviewAiGenerationResult gatewayResult =
                new AssignmentPreviewAiGenerationResult(
                        aiResponse,
                        "gpt-5-mini",
                        false,
                        1200L,
                        null,
                        320,
                        640,
                        180,
                        1200
                );

        given(aiGateway.generateExecution(command)).willReturn(gatewayResult);

        // when: AI 프리뷰 생성 수행
        CreateFreeGameAssignmentPreviewResult result = adapter.generate(command);

        // then: 첫 라운드 수 검증
        then(result.rounds()).hasSize(1);
        then(result.rounds().getFirst().roundNumber()).isEqualTo(1);
        then(result.rounds().getFirst().courts()).hasSize(1);
        then(result.rounds().getFirst().courts().getFirst().courtNumber()).isEqualTo(1);
        then(result.rounds().getFirst().courts().getFirst().slots()).hasSize(4);
        then(result.rounds().getFirst().courts().getFirst().slots())
                .containsExactly("p1", "p2", null, null);
        then(result.warnings()).hasSize(1);
        then(result.warnings().getFirst().code()).isEqualTo("PARTIAL_ASSIGNMENT");
        then(result.warnings().getFirst().message()).isEqualTo("일부 슬롯은 비어 있습니다.");
        verify(aiGateway).generateExecution(command);

    }

    @Test
    @DisplayName("AI 호출이 실패하면 서비스 불가 예외를 던진다.")
    void generate_whenAiClientFails_throwsServiceUnavailableException() {
        // given: 프리뷰 생성 입력과 AI 호출 실패 준비
        CreateFreeGameAssignmentPreviewCommand command = previewCommand();

        given(aiGateway.generateExecution(command))
                .willThrow(new RuntimeException("LLM Model Timeout"));

        // when & then: 서비스 불가 예외 반환 검증
        assertThatThrownBy(() -> adapter.generate(command))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("AI 코트 배정 프리뷰를 현재 생성할 수 없습니다.")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("AI 실행 메타데이터를 application generation으로 변환한다.")
    void generateExecution_withValidAiResponse_returnsGeneration() {
        CreateFreeGameAssignmentPreviewCommand command = previewCommand();
        AssignmentPreviewAiGenerationResult gatewayResult =
                new AssignmentPreviewAiGenerationResult(
                        previewAiResponse(),
                        "gpt-5-mini",
                        true,
                        900L,
                        800L,
                        300,
                        580,
                        170,
                        1200
                );

        given(aiGateway.generateExecution(command)).willReturn(gatewayResult);

        FreeGameAssignmentPreviewGeneration result = adapter.generateExecution(command);

        then(result.model()).isEqualTo("gpt-5-mini");
        then(result.repairAttempted()).isTrue();
        then(result.initialAiElapsedMs()).isEqualTo(900L);
        then(result.repairAiElapsedMs()).isEqualTo(800L);
        then(result.planningInputChars()).isEqualTo(300);
        then(result.promptChars()).isEqualTo(580);
        then(result.responseChars()).isEqualTo(170);
        then(result.maxCompletionTokens()).isEqualTo(1200);
        then(result.preview().rounds().getFirst().courts().getFirst().slots())
                .containsExactly("p1", "p2", null, null);
        verify(aiGateway).generateExecution(command);
    }

    private CreateFreeGameAssignmentPreviewCommand previewCommand() {
        return new CreateFreeGameAssignmentPreviewCommand(
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
                List.of(
                        new CreateFreeGameAssignmentPreviewCommand.PartnerPairs(
                                "p1", "p2"
                        )
                ),
                new CreateFreeGameAssignmentPreviewCommand.Preferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );
    }

    private AssignmentPreviewAiResponse previewAiResponse() {
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
