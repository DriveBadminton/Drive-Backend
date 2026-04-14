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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpringAiAssignmentPreviewGatewayPromptTest extends SpringAiAssignmentPreviewGatewayTestSupport {

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
        then(options.getTemperature()).isEqualTo(0.0d);
        then(options.getMaxCompletionTokens()).isEqualTo(1200);
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
        then(getSingleCapturedPrompt().getContents()).contains("코트 배정 preview");
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
    @DisplayName("초기 prompt에 배정 우선순위와 warning 규칙을 포함한다.")
    void generate_includesAssignmentPriorityAndWarningRulesInPrompt() {
        // given: 정상 응답을 반환하는 Spring AI와 gateway를 준비한다.
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

        // when: AI 프리뷰 생성을 수행한다.
        getGateway().generate(getSingleRoundCommand());

        // then: prompt에 핵심 배정 규칙과 warning 정책이 포함된다.
        String prompt = getSingleCapturedPrompt().getContents();
        then(prompt).contains("규칙:");
        then(prompt).contains("같은 라운드 중복 참가자 금지");
        then(prompt).contains("이전 라운드 전체 복제 금지");
        then(prompt).contains("동일한 court-level 4인 배치 반복 금지");
        then(prompt).contains("null만 최대한 채우기");
        then(prompt).contains("partnerPairs 우선");
        then(prompt).contains("PARTIAL_ASSIGNMENT");
        then(prompt).contains("PARTNER_CONSTRAINT_PARTIAL");
        then(prompt).contains("warnings 비우지 않기");
    }

    @Test
    @DisplayName("OpenAI 응답이 비어 있으면 IllegalStateException을 던진다")
    void generate_whenResponseIsEmpty_throwsIllegalStateException() {
        // given: 비어 있는 응답을 반환하는 Spring AI와 gateway를 준비한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(new org.springframework.ai.chat.model.ChatResponse(List.of()));

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
                                      "slots": ["p2", null, null, null]
                                    }
                                  ]
                                }
                              ],
                              "warnings": []
                            }
                            """)
                );

        // when: 2개 라운드 프리뷰 생성을 요청한다.
        AssignmentPreviewAiResponse result = getGateway().generate(getTwoRoundVariedCommand());

        // then: 두 번째 유효 응답을 반환하고 AI를 두 번 호출한다.
        then(result.rounds()).hasSize(2);
        then(result.rounds().get(0).roundNumber()).isEqualTo(1);
        then(result.rounds().get(1).roundNumber()).isEqualTo(2);
        then(result.rounds().get(1).courts().getFirst().slots()).containsExactly("p2", null, null, null);
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
                              "slots": ["p2", null, null, null]
                            }
                          ]
                        }
                      ],
                      "warnings": []
                    }
                    """));

        // when: 2개 라운드 프리뷰 생성을 요청한다.
        AssignmentPreviewAiResponse result = getGateway().generate(getTwoRoundVariedCommand());

        // then: 첫 번째 응답을 그대로 반환하고 재시도하지 않는다.
        then(result.rounds()).hasSize(2);
        then(result.rounds().get(1).courts().getFirst().slots()).containsExactly("p2", null, null, null);
        verify(chatModel, times(1)).call(any(Prompt.class));
    }

    @Test
    @DisplayName("빈 슬롯만 채우기 정책이면 planning input의 fixed slot 정보를 prompt에 포함한다.")
    void generate_whenFillEmptySlots_includesPlanningInputSlotMetadataInPrompt() {
        // given: 정상 응답을 반환하는 Spring AI와 fill-empty-slots command를 준비한다.
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

        // when: AI 프리뷰 생성을 수행한다.
        getGateway().generate(getSingleRoundCommand());

        // then: prompt에는 participantId와 fixed 정보가 포함된다.
        String prompt = getSingleCapturedPrompt().getContents();
        then(prompt).contains("\"participantId\":\"p1\"");
        then(prompt).contains("\"fixed\":true");
        then(prompt).contains("\"fixed\":false");
    }

    @Test
    @DisplayName("재시도 prompt도 planning input의 fixed slot 정보를 포함한다.")
    void generate_whenRetrying_usesPlanningInputInRepairPrompt() {
        // given: 첫 번째 응답은 invalid이고 두 번째 응답은 유효하다.
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
                                      "slots": ["p2", null, null, null]
                                    }
                                  ]
                                }
                              ],
                              "warnings": []
                            }
                            """)
                );

        // when: 재시도가 필요한 프리뷰 생성을 수행한다.
        getGateway().generate(getTwoRoundVariedCommand());

        // then: 두 번째 prompt도 planning input 구조를 포함한다.
        Prompt repairPrompt = getCapturedPrompts(2).get(1);
        then(repairPrompt.getContents()).contains("이전 응답은 요청 구조와 제약을 만족하지 못했습니다.");
        then(repairPrompt.getContents()).contains("\"fixed\":true");
        then(repairPrompt.getContents()).contains("\"preserveFixedSlots\":true");
        then(repairPrompt.getContents()).contains("\"fillEmptySlotsOnly\":true");
        then(repairPrompt.getContents()).contains("\"preferProvidedPartnerPairs\":true");
        then(repairPrompt.getContents()).contains("\"preferredPairCount\":0");
    }

    @Test
    @DisplayName("재시도 prompt에 구조 유지와 warning 규칙을 다시 명시한다.")
    void generate_whenRetrying_includesAssignmentPriorityAndWarningRulesInRepairPrompt() {
        // given: 첫 번째 응답은 invalid이고 두 번째 응답은 유효하다.
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
                                      "slots": ["p2", null, null, null]
                                    }
                                  ]
                                }
                              ],
                              "warnings": []
                            }
                            """)
                );

        // when: 재시도가 필요한 프리뷰 생성을 수행한다.
        getGateway().generate(getTwoRoundVariedCommand());

        // then: 재시도 prompt에 구조/경고 규칙이 포함된다.
        String repairPrompt = getCapturedPrompts(2).get(1).getContents();
        then(repairPrompt).contains("다시 생성하세요.");
        then(repairPrompt).contains("같은 라운드 중복 참가자 금지");
        then(repairPrompt).contains("이전 라운드 전체 복제 금지");
        then(repairPrompt).contains("동일한 court-level 4인 배치 반복 금지");
        then(repairPrompt).contains("PARTIAL_ASSIGNMENT");
        then(repairPrompt).contains("PARTNER_CONSTRAINT_PARTIAL");
        then(repairPrompt).contains("warnings 비우지 않기");
    }

    @Test
    @DisplayName("동일한 round layout 반복은 재시도 prompt에 구체적인 실패 사유를 포함한다.")
    void generate_whenRoundLayoutIsRepeated_includesSpecificRepairReason() {
        // given: 첫 번째 응답은 모든 라운드를 동일하게 복제하고, 두 번째 응답은 라운드별로 다르게 반환한다.
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
                                      "slots": ["p2", null, null, null]
                                    }
                                  ]
                                }
                              ],
                              "warnings": []
                            }
                            """)
                );

        // when: round 복제가 포함된 프리뷰 생성을 수행한다.
        getGateway().generate(getTwoRoundVariedCommand());

        // then: repair prompt에 round 복제 실패 사유가 포함된다.
        String repairPrompt = getCapturedPrompts(2).get(1).getContents();
        then(repairPrompt).contains("실패 사유:");
        then(repairPrompt).contains("The previous output copied the same round layout across multiple rounds.");
    }

    @Test
    @DisplayName("prompt에는 planning input의 slim participant 필드만 포함된다.")
    void generate_whenUsingPlanningInput_includesSlimParticipantFieldsInPrompt() {
        // given: 정상 응답을 반환하는 Spring AI와 preview command를 준비한다.
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

        // when: AI 프리뷰 생성을 수행한다.
        getGateway().generate(getSingleRoundCommand());

        // then: prompt에는 clientId / gamesAssigned만 포함되고 name 등은 제외된다.
        String prompt = getSingleCapturedPrompt().getContents();
        then(prompt).contains("\"clientId\":\"p1\"");
        then(prompt).contains("\"gamesAssigned\":1");
        then(prompt).contains("\"participantId\":\"p1\"");
        then(prompt).doesNotContain("\"name\":");
        then(prompt).doesNotContain("\"gender\":");
        then(prompt).doesNotContain("\"ageGroup\":");
        then(prompt).doesNotContain("\"grade\":");
    }

    @Test
    @DisplayName("JSON 파싱 실패는 재시도하지 않고 바로 실패한다.")
    void generate_whenResponseParsingFails_doesNotRetry() {
        // given: 첫 번째 AI 응답이 JSON 파싱 자체에 실패한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("not-json"));

        // when & then: 파싱 실패는 재시도 없이 바로 실패한다.
        assertThatThrownBy(() -> getGateway().generate(getSingleRoundCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI로부터 응답을 읽을 수 없습니다.");
        verify(chatModel, times(1)).call(any(Prompt.class));
    }

    @Test
    @DisplayName("OpenAI 응답이 비어 있으면 재시도하지 않고 바로 실패한다.")
    void generate_whenResponseIsEmpty_doesNotRetry() {
        // given: 첫 번째 AI 응답 자체가 비어 있다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(new org.springframework.ai.chat.model.ChatResponse(List.of()));

        // when & then: 비어 있는 응답은 재시도 없이 바로 실패한다.
        assertThatThrownBy(() -> getGateway().generate(getSingleRoundCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답이 비어 있습니다.");
        verify(chatModel, times(1)).call(any(Prompt.class));
    }

    @Test
    @DisplayName("OpenAI 응답 텍스트가 비어 있으면 재시도하지 않고 바로 실패한다.")
    void generate_whenResponseTextIsBlank_doesNotRetry() {
        // given: 첫 번째 AI 응답 텍스트가 blank다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("   "));

        // when & then: blank 응답은 재시도 없이 바로 실패한다.
        assertThatThrownBy(() -> getGateway().generate(getSingleRoundCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답이 비어 있습니다.");
        verify(chatModel, times(1)).call(any(Prompt.class));
    }

    @Test
    @DisplayName("planning input의 constraint guidance를 초기 prompt에 포함한다.")
    void generate_includesConstraintGuidanceInInitialPrompt() {
        // given: 정상 응답을 반환하는 Spring AI와 fill-empty-slots command를 준비한다.
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

        // when: AI 프리뷰 생성을 수행한다.
        getGateway().generate(getSingleRoundCommand());

        // then: prompt에 unified guidance가 포함된다.
        String prompt = getSingleCapturedPrompt().getContents();
        then(prompt).contains("\"guidance\"");
        then(prompt).contains("\"preserveFixedSlots\":true");
        then(prompt).contains("\"fillEmptySlotsOnly\":true");
    }

    @Test
    @DisplayName("planning input의 policy와 partner guidance를 초기 prompt에 포함한다.")
    void generate_includesPolicyAndPartnerGuidanceInInitialPrompt() {
        // given: 정상 응답을 반환하는 Spring AI와 파트너 pair가 있는 preview command를 준비한다.
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
                getCommand(
                        List.of(
                                getParticipant(),
                                getParticipant("p2", "김원호", 0)
                        ),
                        List.of(getRound(1, java.util.Arrays.asList("p1", null, null, null))),
                        List.of(
                                getPartnerPair("p1", "p2")
                        ),
                        getPreferences()
                );

        // when: AI 프리뷰 생성을 수행한다.
        getGateway().generate(command);

        // then: prompt에 unified guidance의 정책/파트너 필드가 포함된다.
        String prompt = getSingleCapturedPrompt().getContents();
        then(prompt).contains("\"guidance\"");
        then(prompt).contains("\"preserveFixedSlots\":true");
        then(prompt).contains("\"fillEmptySlotsOnly\":true");
        then(prompt).contains("\"preferProvidedPartnerPairs\":true");
        then(prompt).contains("\"preferredPairCount\":1");
    }

    @Test
    @DisplayName("파트너 무시와 전체 재배정 정책이면 negative guidance를 초기 prompt에 포함한다.")
    void generate_includesNegativeGuidanceInInitialPrompt() {
        // given: partner pair는 있지만 정책상 무시하는 preview command를 준비한다.
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

        CreateFreeGameAssignmentPreviewCommand command = getCommand(
                List.of(
                        getParticipant(),
                        getParticipant("p2", "김원호", 0)
                ),
                List.of(getRound(1, java.util.Arrays.asList("p1", null, null, null))),
                List.of(getPartnerPair("p1", "p2")),
                getPreferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.IGNORE_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.REASSIGN_ALL
                )
        );

        // when: AI 프리뷰 생성을 수행한다.
        getGateway().generate(command);

        // then: prompt에 unified guidance의 false/0 값이 포함된다.
        String prompt = getSingleCapturedPrompt().getContents();
        then(prompt).contains("\"guidance\"");
        then(prompt).contains("\"preserveFixedSlots\":false");
        then(prompt).contains("\"fillEmptySlotsOnly\":false");
        then(prompt).contains("\"preferProvidedPartnerPairs\":false");
        then(prompt).contains("\"preferredPairCount\":0");
    }
}
