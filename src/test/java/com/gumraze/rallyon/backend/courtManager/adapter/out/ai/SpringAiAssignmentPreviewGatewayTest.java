package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
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
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SpringAiAssignmentPreviewGatewayTest {

    @Mock
    private OpenAiChatModel chatModel;

    @Test
    @DisplayName("Spring AI 응답을 AI 프리뷰 구조로 반환한다.")
    void generate_withValidResponse_returnsAiResponse() {
        // given: Spring AI 응답과 Gateway를 준비한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                        {
                          "rounds": [
                            {
                              "roundNumber": 1,
                              "courts": [
                                {
                                  "courtNumber": 1,
                                  "slots": ["p1", null, null, null]
                                }
                              ]
                            }
                          ],
                          "warnings": []
                        }
                        """));

        // when: AI 프리뷰 생성 수행
        AssignmentPreviewAiResponse result = getGateway().generate(getSingleRoundCommand());

        // then: AI 프리뷰 구조 반환 검증
        then(result).isEqualTo(getSingleRoundAiResponse());
        Prompt prompt = getSingleCapturedPrompt();

        OpenAiChatOptions options = (OpenAiChatOptions) prompt.getOptions();
        then(options.getResponseFormat().getType()).isEqualTo(ResponseFormat.Type.JSON_SCHEMA);
        then(options.getResponseFormat().getJsonSchema().getName()).isEqualTo("assignment_preview");
        then(options.getResponseFormat().getJsonSchema().getStrict()).isTrue();
        Map<String, Object> schema = options.getResponseFormat().getJsonSchema().getSchema();
        then(schema.get("type")).isEqualTo("object");
        then(prompt.getContents()).contains("p1");
    }

    @Test
    @DisplayName("command 직렬화에 실패하면 IllegalStateException을 던진다")
    void generate_whenCommandSerializationFails_throwsIllegalStateException() {
        // given: command 직렬화에 실패하는 gateway를 준비한다.
        ObjectMapper objectMapper = new ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) throws JacksonException {
                throw new JacksonException("serialize failed") {
                };
            }
        };

        // when & then: 직렬화 실패 시 예외 반환 검증
        assertThatThrownBy(() -> getGateway(objectMapper).generate(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI로부터 응답을 읽을 수 없습니다.")
                .hasCauseInstanceOf(JacksonException.class);
    }

    @Test
    @DisplayName("OpenAI 응답 파싱에 실패하면 IllegalStateException을 던진다")
    void generate_whenResponseParsingFails_throwsIllegalStateException() {
        // given: 잘못된 응답을 반환하는 Spring AI와 gateway를 준비한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("not-json"));

        // when & then: 응답 파싱 실패 시 예외와 원인 보존 검증
        assertThatThrownBy(() -> getGateway().generate(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI로부터 응답을 읽을 수 없습니다.")
                .hasCauseInstanceOf(JacksonException.class);
    }

    @Test
    @DisplayName("Prompt에 코트 배정 프리뷰 생성 instruction을 포함한다.")
    void generate_includesPreviewInstructionInPrompt() {
        // given: 정상 응답을 반환하는 Spring AI와 gateway를 준비한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getEmptyPreviewChatResponse());

        // when: AI 프리뷰 생성 수행
        getGateway().generate(null);

        // then: prompt instruction 포함 검증
        then(getSingleCapturedPrompt().getContents()).contains("코트 배정 프리뷰");
    }

    @Test
    @DisplayName("Prompt에 rounds와 warnings를 포함한 결과 반환 instruction을 포함한다.")
    void generate_includesOutputContractInPrompt() {
        // given: 정상 응답을 반환하는 Spring AI와 gateway를 준비한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getEmptyPreviewChatResponse());

        // when: AI 프리뷰 생성 수행
        getGateway().generate(null);

        // then: prompt output contract 포함 검증
        Prompt prompt = getSingleCapturedPrompt();
        then(prompt.getContents()).contains("rounds");
        then(prompt.getContents()).contains("warnings");
        then(prompt.getContents()).contains("JSON만 반환");
    }

    @Test
    @DisplayName("OpenAI 응답이 비어 있으면 IllegalStateException을 던진다")
    void generate_whenResponseIsEmpty_throwsIllegalStateException() {
        // given: 비어 있는 응답을 반환하는 Spring AI와 gateway를 준비한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(new ChatResponse(List.of()));

        // when & then: 비어 있는 응답일 때 예외 반환 검증
        assertThatThrownBy(() -> getGateway().generate(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답이 비어 있습니다.");
    }

    @Test
    @DisplayName("OpenAI 응답 텍스트가 비어 있으면 IllegalStateException을 던진다")
    void generate_whenResponseTextIsBlank_throwsIllegalStateException() {
        // given: 빈 텍스트 응답을 반환하는 Spring AI와 gateway를 준비한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("   "));

        // when & then: 빈 텍스트 응답일 때 예외 반환 검증
        assertThatThrownBy(() -> getGateway().generate(null))
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
                    throws JacksonException {
                throw new JacksonException("schema parse failed") {
                };
            }
        };

        // when & then: schema 파싱 실패 시 예외 반환 검증
        assertThatThrownBy(() -> getGateway(objectMapper).generate(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI로부터 응답을 읽을 수 없습니다.")
                .hasCauseInstanceOf(JacksonException.class);
    }

    @Test
    @DisplayName("입력보다 적은 라운드를 반환하면 invalid output으로 처리한다.")
    void generate_whenResponseHasFewerRoundsThanRequested_throwsIllegalStateException() {
        // given: 2개 라운드를 요청했지만 AI는 1개의 라운드만 반환한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                        {
                          "rounds": [
                            {
                              "roundNumber": 1,
                              "courts": [
                                {
                                  "courtNumber": 1,
                                  "slots": ["p1", null, null, null]
                                }
                              ]
                            }
                          ],
                          "warnings": []
                        }
                        """));

        // when & then: 입력과 다른 라운드 구조 응답은 invalid output으로 거부한다.
        assertThatThrownBy(() -> getGateway().generate(getTwoRoundCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("입력 라운드의 코트 수보다 적게 반환하면 invalid output으로 처리한다.")
    void generate_whenResponseHasFewerCourtsThanRequested_throwsIllegalStateException() {
        // given: 1개 라운드에 2개 코트를 요청했지만 AI는 1개 코트만 반환한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                                {
                                  "rounds": [
                                    {
                                      "roundNumber": 1,
                                      "courts": [
                                        {
                                          "courtNumber": 1,
                                          "slots": ["p1", null, null, null]
                                        }
                                      ]
                                    }
                                  ],
                                  "warnings": []
                                }
                        """));

        CreateFreeGameAssignmentPreviewCommand command =
                new CreateFreeGameAssignmentPreviewCommand(
                        List.of(getParticipant()),
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Round(
                                        1,
                                        List.of(
                                                getCourt(1, Arrays.asList("p1", null, null, null)),
                                                getCourt(2, Arrays.asList(null, null, null, null))
                                        )
                                )
                        ),
                        List.of(),
                        getPreferences()
                );

        // when & then: 입력과 다른 코트 구조 응답은 invalid output으로 거부한다.
        assertThatThrownBy(() -> getGateway().generate(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("빈 슬롯만 채우기 정책에서 기존 슬롯이 변경되면 invalid output으로 처리한다.")
    void generate_whenFillEmptySlotsChangesExistingAssignment_throwsIllegalStateException() {
        // given: 기존에 배정된 p1 슬롯을 AI가 비워 버린다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                    {
                      "rounds": [
                        {
                          "roundNumber": 1,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": [null, null, null, null]
                            }
                          ]
                        }
                      ],
                      "warnings": []
                    }
                    """));

        // when & then: FILL_EMPTY_SLOTS에서는 기존 non-null 슬롯 변경을 거부한다.
        assertThatThrownBy(() -> getGateway().generate(getSingleRoundCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("입력과 다른 roundNumber를 반환하면 invalid output으로 처리한다.")
    void generate_whenResponseHasDifferentRoundNumber_throwsIllegalStateException() {
        // given: 2개 라운드를 요청했지만 두 번째 라운드 번호를 다르게 반환한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                    {
                      "rounds": [
                        {
                          "roundNumber": 1,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": ["p1", null, null, null]
                            }
                          ]
                        },
                        {
                          "roundNumber": 99,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": [null, null, null, null]
                            }
                          ]
                        }
                      ],
                      "warnings": []
                    }
                    """));

        // when & then: 입력과 다른 라운드 번호 응답은 invalid output으로 거부한다.
        assertThatThrownBy(() -> getGateway().generate(getTwoRoundCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("입력과 다른 courtNumber를 반환하면 invalid output으로 처리한다.")
    void generate_whenResponseHasDifferentCourtNumber_throwsIllegalStateException() {
        // given: 1개 라운드에 2개 코트를 요청했지만 두 번째 코트 번호를 다르게 반환한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                    {
                      "rounds": [
                        {
                          "roundNumber": 1,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": ["p1", null, null, null]
                            },
                            {
                              "courtNumber": 99,
                              "slots": [null, null, null, null]
                            }
                          ]
                        }
                      ],
                      "warnings": []
                    }
                    """));

        CreateFreeGameAssignmentPreviewCommand command =
                new CreateFreeGameAssignmentPreviewCommand(
                        List.of(getParticipant()),
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Round(
                                        1,
                                        List.of(
                                                getCourt(1, Arrays.asList("p1", null, null, null)),
                                                getCourt(2, Arrays.asList(null, null, null, null))
                                        )
                                )
                        ),
                        List.of(),
                        getPreferences()
                );

        // when & then: 입력과 다른 코트 번호 응답은 invalid output으로 거부한다.
        assertThatThrownBy(() -> getGateway().generate(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("요청에 없는 participant id를 반환하면 invalid output으로 처리한다.")
    void generate_whenResponseContainsUnknownParticipantId_throwsIllegalStateException() {
        // given: AI가 요청에 없는 participant id를 비어 있던 슬롯에 넣어 반환한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                    {
                      "rounds": [
                        {
                          "roundNumber": 1,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": ["p1", "p999", null, null]
                            }
                          ]
                        }
                      ],
                      "warnings": []
                    }
                    """));

        // when & then: 요청에 없는 participant id 응답은 invalid output으로 거부한다.
        assertThatThrownBy(() -> getGateway().generate(getSingleRoundCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("전체 다시 배정 정책에서는 기존 슬롯 변경을 허용한다.")
    void generate_whenReassignAllChangesExistingAssignment_returnsAiResponse() {
        // given: REASSIGN_ALL에서는 기존 슬롯이 바뀐 응답도 허용되는 재배정 결과를 준비한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                    {
                      "rounds": [
                        {
                          "roundNumber": 1,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": ["p2", "p1", null, null]
                            }
                          ]
                        }
                      ],
                      "warnings": []
                    }
                    """));

        CreateFreeGameAssignmentPreviewCommand command =
                new CreateFreeGameAssignmentPreviewCommand(
                        List.of(
                                getParticipant(),
                                new CreateFreeGameAssignmentPreviewCommand.Participant(
                                        "p2",
                                        "김원호",
                                        Gender.MALE,
                                        20,
                                        Grade.S,
                                        1
                                )
                        ),
                        List.of(getRound(1, Arrays.asList("p1", null, null, null))),
                        List.of(),
                        new CreateFreeGameAssignmentPreviewCommand.Preferences(
                                CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                                CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.REASSIGN_ALL
                        )
                );

        // when: 전체 다시 배정 정책으로 AI 프리뷰를 생성한다.
        AssignmentPreviewAiResponse result = getGateway().generate(command);

        // then: 기존 슬롯이 바뀐 응답도 정상 재배정 결과로 허용한다.
        then(result.rounds().getFirst().courts().getFirst().slots())
                .containsExactly("p2", "p1", null, null);
    }

    @Test
    @DisplayName("같은 라운드에 동일 참가자가 중복 배정되면 invalid output으로 처리한다.")
    void generate_whenResponseDuplicatesParticipantInSameRound_throwsIllegalStateException() {
        // given: AI가 같은 라운드의 서로 다른 코트에 동일 참가자를 중복 배정한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                    {
                      "rounds": [
                        {
                          "roundNumber": 1,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": ["p1", null, null, null]
                            },
                            {
                              "courtNumber": 2,
                              "slots": ["p1", null, null, null]
                            }
                          ]
                        }
                      ],
                      "warnings": []
                    }
                    """));

        CreateFreeGameAssignmentPreviewCommand command =
                new CreateFreeGameAssignmentPreviewCommand(
                        List.of(getParticipant()),
                        List.of(
                                new CreateFreeGameAssignmentPreviewCommand.Round(
                                        1,
                                        List.of(
                                                getCourt(1, Arrays.asList("p1", null, null, null)),
                                                getCourt(2, Arrays.asList(null, null, null, null))
                                        )
                                )
                        ),
                        List.of(),
                        getPreferences()
                );

        // when & then: 같은 라운드 중복 배정 응답은 invalid output으로 거부한다.
        assertThatThrownBy(() -> getGateway().generate(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("전체 다시 배정 정책에서도 입력과 다른 courtNumber를 반환하면 invalid output으로 처리한다.")
    void generate_whenReassignAllHasDifferentCourtNumber_throwsIllegalStateException() {
        // given: REASSIGN_ALL이지만 두 번째 코트 번호를 다르게 반환한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                    {
                      "rounds": [
                        {
                          "roundNumber": 1,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": ["p2", "p1", null, null]
                            },
                            {
                              "courtNumber": 99,
                              "slots": [null, null, null, null]
                            }
                          ]
                        }
                      ],
                      "warnings": []
                    }
                    """));

        CreateFreeGameAssignmentPreviewCommand command =
                new CreateFreeGameAssignmentPreviewCommand(
                        List.of(
                                getParticipant(),
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
                                                getCourt(1, Arrays.asList("p1", null, null, null)),
                                                getCourt(2, Arrays.asList(null, null, null, null))
                                        )
                                )
                        ),
                        List.of(),
                        new CreateFreeGameAssignmentPreviewCommand.Preferences(
                                CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                                CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.REASSIGN_ALL
                        )
                );

        // when & then: REASSIGN_ALL이어도 코트 번호가 다르면 invalid output으로 거부한다.
        assertThatThrownBy(() -> getGateway().generate(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("슬롯 개수가 4가 아니면 invalid output으로 처리한다.")
    void generate_whenResponseHasInvalidSlotCount_throwsIllegalStateException() {
        // given: AI가 4칸이 아닌 슬롯 배열을 반환한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                    {
                      "rounds": [
                        {
                          "roundNumber": 1,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": ["p1", null, null]
                            }
                          ]
                        }
                      ],
                      "warnings": []
                    }
                    """));

        // when & then: 슬롯 개수가 4가 아니면 invalid output으로 거부한다.
        assertThatThrownBy(() -> getGateway().generate(getSingleRoundCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("같은 참가자가 서로 다른 라운드에 배정되는 것은 허용한다.")
    void generate_whenSameParticipantAppearsInDifferentRounds_returnsAiResponse() {
        // given: 같은 참가자가 각기 다른 라운드에 한 번씩 배정된 응답을 준비한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                    {
                      "rounds": [
                        {
                          "roundNumber": 1,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": ["p1", null, null, null]
                            }
                          ]
                        },
                        {
                          "roundNumber": 2,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": ["p1", null, null, null]
                            }
                          ]
                        }
                      ],
                      "warnings": []
                    }
                    """));

        // when: 여러 라운드가 있는 프리뷰를 생성한다.
        AssignmentPreviewAiResponse result = getGateway().generate(getTwoRoundCommand());

        // then: 서로 다른 라운드의 동일 참가자 배정은 정상 응답으로 허용한다.
        then(result.rounds()).hasSize(2);
        then(result.rounds().get(0).courts().get(0).slots())
                .containsExactly("p1", null, null, null);
        then(result.rounds().get(1).courts().get(0).slots())
                .containsExactly("p1", null, null, null);
    }

    @Test
    @DisplayName("첫 번째 응답이 invalid여도 재시도 응답이 유효하면 그 결과를 반환한다.")
    void generate_whenFirstResponseIsInvalidAndRetryResponseIsValid_returnsRetriedResponse() {
        // given: 첫 번째 AI 응답은 라운드가 부족하고, 두 번째 응답은 요청 구조를 모두 만족한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(
                        getChatResponse("""
                            {
                              "rounds": [
                                {
                                  "roundNumber": 1,
                                  "courts": [
                                    {
                                      "courtNumber": 1,
                                      "slots": ["p1", null, null, null]
                                    }
                                  ]
                                }
                              ],
                              "warnings": []
                            }
                            """),
                        getChatResponse("""
                            {
                              "rounds": [
                                {
                                  "roundNumber": 1,
                                  "courts": [
                                    {
                                      "courtNumber": 1,
                                      "slots": ["p1", null, null, null]
                                    }
                                  ]
                                },
                                {
                                  "roundNumber": 2,
                                  "courts": [
                                    {
                                      "courtNumber": 1,
                                      "slots": ["p1", null, null, null]
                                    }
                                  ]
                                }
                              ],
                              "warnings": []
                            }
                            """)
                );

        // when: 2개 라운드 프리뷰 생성을 요청한다.
        AssignmentPreviewAiResponse result = getGateway().generate(getTwoRoundCommand());

        // then: 두 번째 유효 응답을 반환하고 AI를 두 번 호출한다.
        then(result.rounds()).hasSize(2);
        then(result.rounds().get(0).roundNumber()).isEqualTo(1);
        then(result.rounds().get(1).roundNumber()).isEqualTo(2);
        verify(chatModel, times(2)).call(any(Prompt.class));
    }

    @Test
    @DisplayName("첫 번째 응답이 유효하면 재시도하지 않는다.")
    void generate_whenFirstResponseIsValid_doesNotRetry() {
        // given: 첫 번째 AI 응답이 요청 구조를 모두 만족한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                    {
                      "rounds": [
                        {
                          "roundNumber": 1,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": ["p1", null, null, null]
                            }
                          ]
                        },
                        {
                          "roundNumber": 2,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": ["p1", null, null, null]
                            }
                          ]
                        }
                      ],
                      "warnings": []
                    }
                    """));

        // when: 2개 라운드 프리뷰 생성을 요청한다.
        AssignmentPreviewAiResponse result = getGateway().generate(getTwoRoundCommand());

        // then: 첫 번째 응답을 그대로 반환하고 재시도하지 않는다.
        then(result.rounds()).hasSize(2);
        verify(chatModel, times(1)).call(any(Prompt.class));
    }




    /**
     * Helper Methods
     */
    private SpringAiAssignmentPreviewGateway getGateway() {
        return getGateway(new ObjectMapper());
    }

    private SpringAiAssignmentPreviewGateway getGateway(ObjectMapper objectMapper) {
        return new SpringAiAssignmentPreviewGateway(chatModel, objectMapper);
    }

    private ChatResponse getEmptyPreviewChatResponse() {
        return getChatResponse("""
                {
                  "rounds": [],
                  "warnings": []
                }
                """);
    }

    private ChatResponse getChatResponse(String content) {
        return new ChatResponse(List.of(
                new Generation(new AssistantMessage(content))
        ));
    }

    private Prompt getSingleCapturedPrompt() {
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(1)).call(promptCaptor.capture());
        return promptCaptor.getValue();
    }

    private CreateFreeGameAssignmentPreviewCommand getSingleRoundCommand() {
        return new CreateFreeGameAssignmentPreviewCommand(
                List.of(getParticipant()),
                List.of(getRound(1, Arrays.asList("p1", null, null, null))),
                List.of(),
                getPreferences()
        );
    }

    private AssignmentPreviewAiResponse getSingleRoundAiResponse() {
        return new AssignmentPreviewAiResponse(
                List.of(
                        new AssignmentPreviewAiResponse.Round(
                                1,
                                List.of(new AssignmentPreviewAiResponse.Court(
                                        1,
                                        Arrays.asList("p1", null, null, null)
                                ))
                        )
                ),
                List.of()
        );
    }

    private CreateFreeGameAssignmentPreviewCommand getTwoRoundCommand() {
        return new CreateFreeGameAssignmentPreviewCommand(
                List.of(getParticipant()),
                List.of(
                        getRound(1, Arrays.asList("p1", null, null, null)),
                        getRound(2, Arrays.asList(null, null, null, null))
                ),
                List.of(),
                getPreferences()
        );
    }

    private CreateFreeGameAssignmentPreviewCommand.Participant getParticipant() {
        return new CreateFreeGameAssignmentPreviewCommand.Participant(
                "p1",
                "서승재",
                Gender.MALE,
                20,
                Grade.S,
                1
        );
    }

    private CreateFreeGameAssignmentPreviewCommand.Round getRound(int roundNumber, List<String> slots) {
        return new CreateFreeGameAssignmentPreviewCommand.Round(
                roundNumber,
                List.of(new CreateFreeGameAssignmentPreviewCommand.Court(1, slots))
        );
    }

    private CreateFreeGameAssignmentPreviewCommand.Preferences getPreferences() {
        return new CreateFreeGameAssignmentPreviewCommand.Preferences(
                CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
        );
    }

    private CreateFreeGameAssignmentPreviewCommand.Court getCourt(
            int courtNumber,
            List<String> slots
    ) {
        return new CreateFreeGameAssignmentPreviewCommand.Court(courtNumber, slots);
    }
}
