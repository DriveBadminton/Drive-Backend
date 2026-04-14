package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import tools.jackson.databind.ObjectMapper;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

abstract class SpringAiAssignmentPreviewGatewayTestSupport {

    @Mock
    protected OpenAiChatModel chatModel;

    protected SpringAiAssignmentPreviewGateway getGateway() {
        return getGateway(new ObjectMapper());
    }

    protected SpringAiAssignmentPreviewGateway getGateway(ObjectMapper objectMapper) {
        return new SpringAiAssignmentPreviewGateway(
                new AssignmentPreviewPlanningInputMapper(),
                chatModel,
                objectMapper
        );
    }

    protected ChatResponse getEmptyPreviewChatResponse() {
        return getChatResponse("""
                {
                  "rounds": [],
                  "warnings": []
                }
                """);
    }

    protected ChatResponse getChatResponse(String content) {
        return new ChatResponse(List.of(
                new Generation(new AssistantMessage(content))
        ));
    }

    protected Prompt getSingleCapturedPrompt() {
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(1)).call(promptCaptor.capture());
        return promptCaptor.getValue();
    }

    protected List<Prompt> getCapturedPrompts(int expectedInvocations) {
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(expectedInvocations)).call(promptCaptor.capture());
        return promptCaptor.getAllValues();
    }

    protected CreateFreeGameAssignmentPreviewCommand getSingleRoundCommand() {
        return getCommand(
                List.of(getParticipant()),
                List.of(getRound(1, Arrays.asList("p1", null, null, null))),
                List.of(),
                getPreferences()
        );
    }

    protected AssignmentPreviewAiResponse getSingleRoundAiResponse() {
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

    protected CreateFreeGameAssignmentPreviewCommand getTwoRoundCommand() {
        return getCommand(
                List.of(getParticipant()),
                List.of(
                        getRound(1, Arrays.asList("p1", null, null, null)),
                        getRound(2, Arrays.asList(null, null, null, null))
                ),
                List.of(),
                getPreferences()
        );
    }

    protected CreateFreeGameAssignmentPreviewCommand getTwoRoundVariedCommand() {
        return getCommand(
                List.of(
                        getParticipant("p1", "서승재", 1),
                        getParticipant("p2", "김원호", 0)
                ),
                List.of(
                        getRound(1, Arrays.asList("p1", null, null, null)),
                        getRound(2, Arrays.asList(null, null, null, null))
                ),
                List.of(),
                getPreferences()
        );
    }

    protected CreateFreeGameAssignmentPreviewCommand.Participant getParticipant() {
        return getParticipant("p1", "서승재", 1);
    }

    protected CreateFreeGameAssignmentPreviewCommand.Participant getParticipant(
            String clientId,
            String name,
            int gamesAssigned
    ) {
        return new CreateFreeGameAssignmentPreviewCommand.Participant(
                clientId,
                name,
                Gender.MALE,
                20,
                Grade.S,
                gamesAssigned
        );
    }

    protected CreateFreeGameAssignmentPreviewCommand.Round getRound(int roundNumber, List<String> slots) {
        return new CreateFreeGameAssignmentPreviewCommand.Round(
                roundNumber,
                List.of(new CreateFreeGameAssignmentPreviewCommand.Court(1, slots))
        );
    }

    protected CreateFreeGameAssignmentPreviewCommand.Round getRound(
            int roundNumber,
            CreateFreeGameAssignmentPreviewCommand.Court... courts
    ) {
        return new CreateFreeGameAssignmentPreviewCommand.Round(roundNumber, List.of(courts));
    }

    protected CreateFreeGameAssignmentPreviewCommand.Preferences getPreferences() {
        return getPreferences(
                CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
        );
    }

    protected CreateFreeGameAssignmentPreviewCommand.Preferences getReassignAllPreferences() {
        return getPreferences(
                CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.REASSIGN_ALL
        );
    }

    protected CreateFreeGameAssignmentPreviewCommand.Preferences getPreferences(
            CreateFreeGameAssignmentPreviewCommand.PartnerPolicy partnerPolicy,
            CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy existingAssignmentPolicy
    ) {
        return new CreateFreeGameAssignmentPreviewCommand.Preferences(
                partnerPolicy,
                existingAssignmentPolicy
        );
    }

    protected CreateFreeGameAssignmentPreviewCommand.Court getCourt(
            int courtNumber,
            List<String> slots
    ) {
        return new CreateFreeGameAssignmentPreviewCommand.Court(courtNumber, slots);
    }

    protected CreateFreeGameAssignmentPreviewCommand getCommand(
            List<CreateFreeGameAssignmentPreviewCommand.Participant> participants,
            List<CreateFreeGameAssignmentPreviewCommand.Round> rounds,
            List<CreateFreeGameAssignmentPreviewCommand.PartnerPairs> partnerPairs,
            CreateFreeGameAssignmentPreviewCommand.Preferences preferences
    ) {
        return new CreateFreeGameAssignmentPreviewCommand(
                participants,
                rounds,
                partnerPairs,
                preferences
        );
    }

    protected CreateFreeGameAssignmentPreviewCommand.PartnerPairs getPartnerPair(
            String participantId1,
            String participantId2
    ) {
        return new CreateFreeGameAssignmentPreviewCommand.PartnerPairs(
                participantId1,
                participantId2
        );
    }
}
