package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpringAiAssignmentPreviewGatewayFillCoverageTest extends SpringAiAssignmentPreviewGatewayTestSupport {

    @Test
    @DisplayName("FILL_EMPTY_SLOTS에서 최초 응답이 under-filled면 품질 개선 시도를 수행한다")
    void generateExecution_whenInitialResponseIsUnderFilled_attemptsQualityRepair() {
        CreateFreeGameAssignmentPreviewCommand command = getCommand(
                List.of(
                        getParticipant("p1", "p1", 0),
                        getParticipant("p2", "p2", 0),
                        getParticipant("p3", "p3", 0),
                        getParticipant("p4", "p4", 0)
                ),
                List.of(getRound(1, Arrays.asList("p1", null, null, null))),
                List.of(),
                getPreferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.IGNORE_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );

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
                                          "slots": ["p1", "p2", null, null]
                                        }
                                      ]
                                    }
                                  ],
                                  "warnings": [
                                    {"code": "PARTIAL_ASSIGNMENT", "message": "partial"}
                                  ]
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
                                          "slots": ["p1", "p2", "p3", "p4"]
                                        }
                                      ]
                                    }
                                  ],
                                  "warnings": []
                                }
                                """)
                );

        AssignmentPreviewAiGenerationResult result = getGateway().generateExecution(command);

        then(result.qualityRepairAttemptCount()).isEqualTo(1);
        then(result.qualityRepairElapsedMsTotal()).isNotNull();
        then(result.qualityRepairReasons()).containsExactly("UNDER_FILLED");
        then(result.theoreticalMaxFilledSlots()).isEqualTo(4);
        then(result.actualFilledSlotsAfterInitial()).isEqualTo(2);
        then(result.bestValidFilledSlots()).isEqualTo(4);
        then(result.bestValidWarningCodes()).isEmpty();
        then(result.response().rounds().getFirst().courts().getFirst().slots())
                .containsExactly(1L, 2L, 3L, 4L);
        verify(chatModel, times(2)).call(any(Prompt.class));
    }

    @Test
    @DisplayName("REASSIGN_ALL에서는 under-filled여도 품질 개선 시도를 수행하지 않는다")
    void generateExecution_whenReassignAllIsUnderFilled_doesNotAttemptQualityRepair() {
        CreateFreeGameAssignmentPreviewCommand command = getCommand(
                List.of(
                        getParticipant("p1", "p1", 0),
                        getParticipant("p2", "p2", 0),
                        getParticipant("p3", "p3", 0),
                        getParticipant("p4", "p4", 0)
                ),
                List.of(getRound(1, Arrays.asList("p1", null, null, null))),
                List.of(),
                getPreferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.IGNORE_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.REASSIGN_ALL
                )
        );

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
                          "warnings": [
                            {"code": "PARTIAL_ASSIGNMENT", "message": "partial"}
                          ]
                        }
                        """));

        AssignmentPreviewAiGenerationResult result = getGateway().generateExecution(command);

        then(result.qualityRepairAttemptCount()).isZero();
        then(result.qualityRepairElapsedMsTotal()).isNull();
        then(result.qualityRepairReasons()).isEmpty();
        then(result.theoreticalMaxFilledSlots()).isEqualTo(4);
        then(result.actualFilledSlotsAfterInitial()).isEqualTo(2);
        then(result.bestValidFilledSlots()).isEqualTo(2);
        verify(chatModel, times(1)).call(any(Prompt.class));
    }

    @Test
    @DisplayName("품질 개선 응답이 구조 invalid이면 최초 valid 응답을 유지한다")
    void generateExecution_whenQualityRepairIsInvalid_keepsInitialValidResponse() {
        CreateFreeGameAssignmentPreviewCommand command = getCommand(
                List.of(
                        getParticipant("p1", "p1", 0),
                        getParticipant("p2", "p2", 0),
                        getParticipant("p3", "p3", 0),
                        getParticipant("p4", "p4", 0)
                ),
                List.of(getRound(1, Arrays.asList("p1", null, null, null))),
                List.of(),
                getPreferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.IGNORE_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );

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
                                          "slots": ["p1", "p2", null, null]
                                        }
                                      ]
                                    }
                                  ],
                                  "warnings": [
                                    {"code": "PARTIAL_ASSIGNMENT", "message": "partial"}
                                  ]
                                }
                                """),
                        getChatResponse("""
                                {
                                  "rounds": [
                                    {
                                      "roundNumber": 1,
                                      "courts": [
                                        {
                                          "courtNumber": 99,
                                          "slots": ["p1", "p2", "p3", "p4"]
                                        }
                                      ]
                                    }
                                  ],
                                  "warnings": []
                                }
                                """)
                );

        AssignmentPreviewAiGenerationResult result = getGateway().generateExecution(command);

        then(result.qualityRepairAttemptCount()).isEqualTo(2);
        then(result.bestValidFilledSlots()).isEqualTo(2);
        then(result.bestValidWarningCodes()).containsExactly("PARTIAL_ASSIGNMENT");
        then(result.response().rounds().getFirst().courts().getFirst().slots())
                .containsExactly(1L, 2L, null, null);
    }

    @Test
    @DisplayName("첫 품질 개선 후에도 under-filled면 두 번째 품질 개선을 수행한다")
    void generateExecution_whenFirstQualityRepairIsStillUnderFilled_attemptsSecondQualityRepair() {
        CreateFreeGameAssignmentPreviewCommand command = getCommand(
                List.of(
                        getParticipant("p1", "p1", 0),
                        getParticipant("p2", "p2", 0),
                        getParticipant("p3", "p3", 0),
                        getParticipant("p4", "p4", 0)
                ),
                List.of(getRound(1, Arrays.asList("p1", null, null, null))),
                List.of(),
                getPreferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.IGNORE_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );

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
                                          "slots": ["p1", "p2", null, null]
                                        }
                                      ]
                                    }
                                  ],
                                  "warnings": [
                                    {"code": "PARTIAL_ASSIGNMENT", "message": "partial"}
                                  ]
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
                                          "slots": ["p1", "p2", "p3", null]
                                        }
                                      ]
                                    }
                                  ],
                                  "warnings": [
                                    {"code": "PARTIAL_ASSIGNMENT", "message": "partial"}
                                  ]
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
                                          "slots": ["p1", "p2", "p3", "p4"]
                                        }
                                      ]
                                    }
                                  ],
                                  "warnings": []
                                }
                                """)
                );

        AssignmentPreviewAiGenerationResult result = getGateway().generateExecution(command);

        then(result.qualityRepairAttemptCount()).isEqualTo(2);
        then(result.qualityRepairReasons()).containsExactly("UNDER_FILLED");
        then(result.bestValidFilledSlots()).isEqualTo(4);
        then(result.response().rounds().getFirst().courts().getFirst().slots())
                .containsExactly(1L, 2L, 3L, 4L);
        verify(chatModel, times(3)).call(any(Prompt.class));
    }

    @Test
    @DisplayName("warning inconsistency만 있으면 채움률은 유지하고 warning만 보정한 결과를 채택한다")
    void generateExecution_whenOnlyWarningsAreInconsistent_repairsWarningsWithoutChangingFillCoverage() {
        CreateFreeGameAssignmentPreviewCommand command = getCommand(
                List.of(
                        getParticipant("p1", "p1", 0),
                        getParticipant("p2", "p2", 0)
                ),
                List.of(getRound(1, Arrays.asList("p1", null, null, null))),
                List.of(),
                getPreferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.IGNORE_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );

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
                                          "slots": ["p1", "p2", null, null]
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
                                          "slots": ["p1", "p2", null, null]
                                        }
                                      ]
                                    }
                                  ],
                                  "warnings": [
                                    {"code": "PARTIAL_ASSIGNMENT", "message": "partial"}
                                  ]
                                }
                                """)
                );

        AssignmentPreviewAiGenerationResult result = getGateway().generateExecution(command);

        then(result.qualityRepairAttemptCount()).isEqualTo(1);
        then(result.qualityRepairReasons()).containsExactly("INCONSISTENT_WARNINGS");
        then(result.actualFilledSlotsAfterInitial()).isEqualTo(2);
        then(result.bestValidFilledSlots()).isEqualTo(2);
        then(result.bestValidWarningCodes()).containsExactly("PARTIAL_ASSIGNMENT");
        then(result.response().warnings()).extracting(AssignmentPreviewAiResponse.Warning::code)
                .containsExactly("PARTIAL_ASSIGNMENT");
    }

    @Test
    @DisplayName("빈 슬롯이 없으면 PARTIAL_ASSIGNMENT와 NO_FURTHER_IMPROVEMENT warning을 제거한다")
    void generate_whenAllSlotsAreFilled_removesInconsistentWarnings() {
        CreateFreeGameAssignmentPreviewCommand command = getCommand(
                List.of(
                        getParticipant("p1", "p1", 0),
                        getParticipant("p2", "p2", 0),
                        getParticipant("p3", "p3", 0),
                        getParticipant("p4", "p4", 0)
                ),
                List.of(getRound(1, Arrays.asList(null, null, null, null))),
                List.of(),
                getPreferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.IGNORE_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );

        given(chatModel.call(any(Prompt.class)))
                .willReturn(getChatResponse("""
                        {
                          "rounds": [
                            {
                              "roundNumber": 1,
                              "courts": [
                                {
                                  "courtNumber": 1,
                                  "slots": ["p1", "p2", "p3", "p4"]
                                }
                              ]
                            }
                          ],
                          "warnings": [
                            {"code": "PARTIAL_ASSIGNMENT", "message": "partial"},
                            {"code": "NO_FURTHER_IMPROVEMENT", "message": "done"}
                          ]
                        }
                        """));

        AssignmentPreviewAiResponse result = getGateway().generate(command);

        then(result.warnings()).isEmpty();
    }
}
