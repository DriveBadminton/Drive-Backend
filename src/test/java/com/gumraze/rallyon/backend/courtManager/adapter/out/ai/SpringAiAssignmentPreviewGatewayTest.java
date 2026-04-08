package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class SpringAiAssignmentPreviewGatewayTest {

    @Mock
    private OpenAiChatModel chatModel;

    @Test
    @DisplayName("Spring AI 응답을 AI 프리뷰 구조로 반환한다.")
    void generate_withValidResponse_returnsAiResponse() {
        // given: Spring AI 응답과 Gateway를 준비한다.
        SpringAiAssignmentPreviewGateway gateway =
                new SpringAiAssignmentPreviewGateway(chatModel, new ObjectMapper());

        given(chatModel.call(any(Prompt.class)))
                .willReturn(new ChatResponse(List.of(
                        new Generation(new AssistantMessage(
                                """
                                        {
                                          "rounds": [],
                                          "warnings": []
                                        }
                                        """
                        ))
                )));

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

        // when: AI 프리뷰 생성 수행
        AssignmentPreviewAiResponse result = gateway.generate(command);

        // then: AI 프리뷰 구조 반환 검증
        then(result).isEqualTo(new AssignmentPreviewAiResponse(List.of(), List.of()));
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());

        OpenAiChatOptions options = (OpenAiChatOptions) promptCaptor.getValue().getOptions();
        then(options.getResponseFormat().getType()).isEqualTo(ResponseFormat.Type.JSON_SCHEMA);
        then(options.getResponseFormat().getJsonSchema().getName()).isEqualTo("assignment_preview");
        then(options.getResponseFormat().getJsonSchema().getStrict()).isTrue();
        Map<String, Object> schema = options.getResponseFormat().getJsonSchema().getSchema();
        then(schema.get("type")).isEqualTo("object");
        then(promptCaptor.getValue().getContents()).contains("p1");
    }

    @Test
    @DisplayName("command 직렬화에 실패하면 IllegalStateException을 던진다")
    void generate_whenCommandSerializationFails_throwsIllegalStateException() {
        // given: command 직렬화에 실패하는 gateway를 준비한다.
        ObjectMapper objectMapper = new ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) throws JsonProcessingException {
                throw new JsonProcessingException("serialize failed") {
                };
            }
        };
        SpringAiAssignmentPreviewGateway gateway =
                new SpringAiAssignmentPreviewGateway(chatModel, objectMapper);

        // when & then: 직렬화 실패 시 예외 반환 검증
        assertThatThrownBy(() -> gateway.generate(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI로부터 응답을 읽을 수 없습니다.")
                .hasCauseInstanceOf(JsonProcessingException.class);
    }

    @Test
    @DisplayName("OpenAI 응답 파싱에 실패하면 IllegalStateException을 던진다")
    void generate_whenResponseParsingFails_throwsIllegalStateException() {
        // given: 잘못된 응답을 반환하는 Spring AI와 gateway를 준비한다.
        SpringAiAssignmentPreviewGateway gateway =
                new SpringAiAssignmentPreviewGateway(chatModel, new ObjectMapper());

        given(chatModel.call(any(Prompt.class)))
                .willReturn(new ChatResponse(List.of(
                        new Generation(new AssistantMessage("not-json"))
                )));

        // when & then: 응답 파싱 실패 시 예외와 원인 보존 검증
        assertThatThrownBy(() -> gateway.generate(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI로부터 응답을 읽을 수 없습니다.")
                .hasCauseInstanceOf(JsonProcessingException.class);
    }

    @Test
    @DisplayName("Prompt에 코트 배정 프리뷰 생성 instruction을 포함한다.")
    void generate_includesPreviewInstructionInPrompt() {
        // given: 정상 응답을 반환하는 Spring AI와 gateway를 준비한다.
        SpringAiAssignmentPreviewGateway gateway =
                new SpringAiAssignmentPreviewGateway(chatModel, new ObjectMapper());

        given(chatModel.call(any(Prompt.class)))
                .willReturn(new ChatResponse(List.of(
                        new Generation(new AssistantMessage("""
                                {
                                  "rounds": [],
                                  "warnings": []
                                }
                                """))
                )));

        // when: AI 프리뷰 생성 수행
        gateway.generate(null);

        // then: prompt instruction 포함 검증
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());

        then(promptCaptor.getValue().getContents()).contains("코트 배정 프리뷰");
    }

    @Test
    @DisplayName("Prompt에 rounds와 warnings를 포함한 결과 반환 instruction을 포함한다.")
    void generate_includesOutputContractInPrompt() {
        // given: 정상 응답을 반환하는 Spring AI와 gateway를 준비한다.
        SpringAiAssignmentPreviewGateway gateway =
                new SpringAiAssignmentPreviewGateway(chatModel, new ObjectMapper());

        given(chatModel.call(any(Prompt.class)))
                .willReturn(new ChatResponse(List.of(
                        new Generation(new AssistantMessage("""
                                {
                                  "rounds": [],
                                  "warnings": []
                                }
                                """))
                )));

        // when: AI 프리뷰 생성 수행
        gateway.generate(null);

        // then: prompt output contract 포함 검증
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());

        then(promptCaptor.getValue().getContents()).contains("rounds");
        then(promptCaptor.getValue().getContents()).contains("warnings");
        then(promptCaptor.getValue().getContents()).contains("JSON만 반환");
    }

    @Test
    @DisplayName("OpenAI 응답이 비어 있으면 IllegalStateException을 던진다")
    void generate_whenResponseIsEmpty_throwsIllegalStateException() {
        // given: 비어 있는 응답을 반환하는 Spring AI와 gateway를 준비한다.
        SpringAiAssignmentPreviewGateway gateway =
                new SpringAiAssignmentPreviewGateway(chatModel, new ObjectMapper());

        given(chatModel.call(any(Prompt.class)))
                .willReturn(new ChatResponse(List.of()));

        // when & then: 비어 있는 응답일 때 예외 반환 검증
        assertThatThrownBy(() -> gateway.generate(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답이 비어 있습니다.");
    }

    @Test
    @DisplayName("OpenAI 응답 텍스트가 비어 있으면 IllegalStateException을 던진다")
    void generate_whenResponseTextIsBlank_throwsIllegalStateException() {
        // given: 빈 텍스트 응답을 반환하는 Spring AI와 gateway를 준비한다.
        SpringAiAssignmentPreviewGateway gateway =
                new SpringAiAssignmentPreviewGateway(chatModel, new ObjectMapper());

        given(chatModel.call(any(Prompt.class)))
                .willReturn(new ChatResponse(List.of(
                        new Generation(new AssistantMessage("   "))
                )));

        // when & then: 빈 텍스트 응답일 때 예외 반환 검증
        assertThatThrownBy(() -> gateway.generate(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답이 비어 있습니다.");
    }

    @Test
    @DisplayName("schema 파싱에 실패하면 IllegalStateException을 던진다")
    void generate_whenSchemaParsingFails_throwsIllegalStateException() {
        // given: schema 파싱에 실패하는 gateway를 준비한다.
        ObjectMapper objectMapper = new ObjectMapper() {
            @Override
            public <T> T readValue(String content, TypeReference<T> valueTypeRef)
                    throws JsonProcessingException {
                throw new JsonProcessingException("schema parse failed") {
                };
            }
        };
        SpringAiAssignmentPreviewGateway gateway =
                new SpringAiAssignmentPreviewGateway(chatModel, objectMapper);

        // when & then: schema 파싱 실패 시 예외 반환 검증
        assertThatThrownBy(() -> gateway.generate(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI로부터 응답을 읽을 수 없습니다.")
                .hasCauseInstanceOf(JsonProcessingException.class);
    }





}
