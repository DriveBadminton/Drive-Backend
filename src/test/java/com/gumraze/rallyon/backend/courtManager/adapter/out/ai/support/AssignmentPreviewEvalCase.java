package com.gumraze.rallyon.backend.courtManager.adapter.out.ai.support;

import tools.jackson.databind.ObjectMapper;
import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.AssignmentPreviewAiResponse;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public record AssignmentPreviewEvalCase(
        String scenario,
        FixtureCommand command,
        FixtureResponse response,
        Expected expected
) {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String FIXTURE_ROOT = "assignment-preview-evals/";

    public AssignmentPreviewEvalCase {
        Objects.requireNonNull(scenario, "scenario must not be null");
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(response, "response must not be null");
        Objects.requireNonNull(expected, "expected must not be null");
    }

    public static AssignmentPreviewEvalCase load(String fileName) {
        ClassPathResource resource = new ClassPathResource(FIXTURE_ROOT + fileName);

        try (InputStream inputStream = resource.getInputStream()) {
            return OBJECT_MAPPER.readValue(inputStream, AssignmentPreviewEvalCase.class);
        } catch (IOException ex) {
            throw new IllegalStateException("평가 fixture를 읽을 수 없습니다: " + fileName, ex);
        }
    }

    public CreateFreeGameAssignmentPreviewCommand toCommand() {
        return command.toCommand();
    }

    public AssignmentPreviewAiResponse toResponse() {
        return response.toResponse();
    }

    public record FixtureCommand(
            List<FixtureParticipant> participants,
            List<FixtureRound> rounds,
            List<FixturePartnerPair> partnerPairs,
            FixturePreferences preferences
    ) {

        public FixtureCommand {
            Objects.requireNonNull(preferences, "preferences must not be null");
        }

        private CreateFreeGameAssignmentPreviewCommand toCommand() {
            return new CreateFreeGameAssignmentPreviewCommand(
                    safeList(participants).stream()
                            .map(FixtureParticipant::toCommandParticipant)
                            .toList(),
                    safeList(rounds).stream()
                            .map(FixtureRound::toCommandRound)
                            .toList(),
                    safeList(partnerPairs).stream()
                            .map(FixturePartnerPair::toCommandPartnerPair)
                            .toList(),
                    preferences.toCommandPreferences()
            );
        }
    }

    public record FixtureParticipant(
            String clientId,
            String name,
            String gender,
            Integer ageGroup,
            String grade,
            Integer gamesAssigned
    ) {

        private CreateFreeGameAssignmentPreviewCommand.Participant toCommandParticipant() {
            return new CreateFreeGameAssignmentPreviewCommand.Participant(
                    clientId,
                    name,
                    Gender.valueOf(gender),
                    ageGroup,
                    Grade.from(grade),
                    gamesAssigned
            );
        }
    }

    public record FixtureRound(
            Integer roundNumber,
            List<FixtureCourt> courts
    ) {

        private CreateFreeGameAssignmentPreviewCommand.Round toCommandRound() {
            return new CreateFreeGameAssignmentPreviewCommand.Round(
                    roundNumber,
                    safeList(courts).stream()
                            .map(FixtureCourt::toCommandCourt)
                            .toList()
            );
        }

        private AssignmentPreviewAiResponse.Round toResponseRound() {
            return new AssignmentPreviewAiResponse.Round(
                    roundNumber,
                    safeList(courts).stream()
                            .map(FixtureCourt::toResponseCourt)
                            .toList()
            );
        }
    }

    public record FixtureCourt(
            Integer courtNumber,
            List<String> slots
    ) {

        private CreateFreeGameAssignmentPreviewCommand.Court toCommandCourt() {
            return new CreateFreeGameAssignmentPreviewCommand.Court(courtNumber, safeList(slots));
        }

        private AssignmentPreviewAiResponse.Court toResponseCourt() {
            return new AssignmentPreviewAiResponse.Court(courtNumber, safeList(slots));
        }
    }

    public record FixturePartnerPair(
            String participantId1,
            String participantId2
    ) {

        private CreateFreeGameAssignmentPreviewCommand.PartnerPairs toCommandPartnerPair() {
            return new CreateFreeGameAssignmentPreviewCommand.PartnerPairs(participantId1, participantId2);
        }
    }

    public record FixturePreferences(
            String partnerPolicy,
            String existingAssignmentPolicy
    ) {

        private CreateFreeGameAssignmentPreviewCommand.Preferences toCommandPreferences() {
            return new CreateFreeGameAssignmentPreviewCommand.Preferences(
                    CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.valueOf(partnerPolicy),
                    CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.valueOf(existingAssignmentPolicy)
            );
        }
    }

    public record FixtureResponse(
            List<FixtureRound> rounds,
            List<FixtureWarning> warnings
    ) {

        private AssignmentPreviewAiResponse toResponse() {
            return new AssignmentPreviewAiResponse(
                    safeList(rounds).stream()
                            .map(FixtureRound::toResponseRound)
                            .toList(),
                    safeList(warnings).stream()
                            .map(FixtureWarning::toResponseWarning)
                            .toList()
            );
        }
    }

    public record FixtureWarning(
            String code,
            String message
    ) {

        private AssignmentPreviewAiResponse.Warning toResponseWarning() {
            return new AssignmentPreviewAiResponse.Warning(code, message);
        }
    }

    public record Expected(
            boolean pass,
            int minFilledSlotDelta,
            List<String> expectedWarningCodes,
            List<String> forbiddenWarningCodes,
            int expectedSatisfiedPartnerPairCount,
            List<AssignmentPreviewQualityReport.FailureReason> expectedFailureReasons
    ) {

        public Expected {
            expectedWarningCodes = safeList(expectedWarningCodes);
            forbiddenWarningCodes = safeList(forbiddenWarningCodes);
            expectedFailureReasons = safeList(expectedFailureReasons);
        }
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}
