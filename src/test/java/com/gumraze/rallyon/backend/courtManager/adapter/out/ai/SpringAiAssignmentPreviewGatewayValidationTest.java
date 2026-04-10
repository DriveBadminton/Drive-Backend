package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpringAiAssignmentPreviewGatewayValidationTest extends SpringAiAssignmentPreviewGatewayTestSupport {

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
                getCommand(
                        List.of(getParticipant()),
                        List.of(
                                getRound(
                                        1,
                                        getCourt(1, Arrays.asList("p1", null, null, null)),
                                        getCourt(2, Arrays.asList(null, null, null, null))
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
                getCommand(
                        List.of(getParticipant()),
                        List.of(
                                getRound(
                                        1,
                                        getCourt(1, Arrays.asList("p1", null, null, null)),
                                        getCourt(2, Arrays.asList(null, null, null, null))
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
                getCommand(
                        List.of(
                                getParticipant(),
                                getParticipant("p2", "김원호", 1)
                        ),
                        List.of(getRound(1, Arrays.asList("p1", null, null, null))),
                        List.of(),
                        getReassignAllPreferences()
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
                getCommand(
                        List.of(getParticipant()),
                        List.of(
                                getRound(
                                        1,
                                        getCourt(1, Arrays.asList("p1", null, null, null)),
                                        getCourt(2, Arrays.asList(null, null, null, null))
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
                getCommand(
                        List.of(
                                getParticipant(),
                                getParticipant("p2", "김원호", 1)
                        ),
                        List.of(
                                getRound(
                                        1,
                                        getCourt(1, Arrays.asList("p1", null, null, null)),
                                        getCourt(2, Arrays.asList(null, null, null, null))
                                )
                        ),
                        List.of(),
                        getReassignAllPreferences()
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
    @DisplayName("rounds가 null이면 invalid output으로 처리한다.")
    void generate_whenResponseHasNullRounds_throwsIllegalStateException() {
        // given: AI가 rounds를 null로 반환한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                    {
                      "rounds": null,
                      "warnings": []
                    }
                    """));

        // when & then: null rounds 응답은 invalid output으로 거부한다.
        assertThatThrownBy(() -> getGateway().generate(getSingleRoundCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("slots가 null이면 invalid output으로 처리한다.")
    void generate_whenResponseHasNullSlots_throwsIllegalStateException() {
        // given: AI가 코트의 slots를 null로 반환한다.
        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                    {
                      "rounds": [
                        {
                          "roundNumber": 1,
                          "courts": [
                            {
                              "courtNumber": 1,
                              "slots": null
                            }
                          ]
                        }
                      ],
                      "warnings": []
                    }
                    """));

        // when & then: null slots 응답은 invalid output으로 거부한다.
        assertThatThrownBy(() -> getGateway().generate(getSingleRoundCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("warnings가 null이면 invalid output으로 처리한다.")
    void generate_whenResponseHasNullWarnings_throwsIllegalStateException() {
        // given: AI가 warnings를 null로 반환한다.
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
                      "warnings": null
                    }
                    """));

        // when & then: null warnings 응답은 invalid output으로 거부한다.
        assertThatThrownBy(() -> getGateway().generate(getSingleRoundCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("warning code가 null이면 invalid output으로 처리한다.")
    void generate_whenResponseHasWarningWithNullCode_throwsIllegalStateException() {
        // given: AI가 code가 null인 warning을 반환한다.
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
                      "warnings": [
                        {
                          "code": null,
                          "message": "일부 슬롯은 비어 있습니다."
                        }
                      ]
                    }
                    """));

        // when & then: null warning code 응답은 invalid output으로 거부한다.
        assertThatThrownBy(() -> getGateway().generate(getSingleRoundCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("재시도 응답도 invalid면 최종적으로 실패한다.")
    void generate_whenRetryResponseIsStillInvalid_throwsIllegalStateException() {
        // given: 첫 번째와 두 번째 AI 응답 모두 요청 구조를 만족하지 못한다.
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
                                }
                              ],
                              "warnings": []
                            }
                            """)
                );

        // when & then: 재시도 후에도 invalid면 최종적으로 실패한다.
        assertThatThrownBy(() -> getGateway().generate(getTwoRoundCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OpenAI 응답 구조가 요청과 일치하지 않습니다.");
        verify(chatModel, times(2)).call(any(Prompt.class));
    }
}
