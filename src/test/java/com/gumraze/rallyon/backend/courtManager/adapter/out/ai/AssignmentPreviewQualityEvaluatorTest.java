package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.support.AssignmentPreviewQualityEvaluator;
import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.support.AssignmentPreviewQualityReport;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class AssignmentPreviewQualityEvaluatorTest {

    private final AssignmentPreviewQualityEvaluator evaluator = new AssignmentPreviewQualityEvaluator();

    @Test
    @DisplayName("빈 슬롯을 더 채우면 filled slot delta를 양수로 계산한다.")
    void evaluate_whenResponseAddsAssignments_reportsPositiveFilledSlotDelta() {
        // given: 빈 슬롯 하나가 추가로 채워진 응답을 준비한다.
        CreateFreeGameAssignmentPreviewCommand command = command(
                List.of(participant("p1"), participant("p2")),
                List.of(round(1, court(1, "p1", null, null, null))),
                List.of(),
                preferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.IGNORE_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );
        AssignmentPreviewAiResponse response = response(
                List.of(roundResponse(1, responseCourt(1, "p1", "p2", null, null))),
                List.of(warning("PARTIAL_ASSIGNMENT"))
        );

        // when: 품질 평가를 수행한다.
        AssignmentPreviewQualityReport report = evaluator.evaluate(command, response);

        // then: filled slot 증가와 pass 여부가 계산된다.
        then(report.filledSlotsBefore()).isEqualTo(1);
        then(report.filledSlotsAfter()).isEqualTo(2);
        then(report.filledSlotDelta()).isEqualTo(1);
        then(report.pass()).isTrue();
    }

    @Test
    @DisplayName("같은 라운드에 중복 참가자가 있으면 실패로 판단한다.")
    void evaluate_whenResponseHasDuplicateParticipantInRound_reportsFailure() {
        // given: 같은 라운드에 p1이 두 번 들어간 응답을 준비한다.
        CreateFreeGameAssignmentPreviewCommand command = command(
                List.of(participant("p1"), participant("p2")),
                List.of(round(1, court(1, "p1", null, null, null))),
                List.of(),
                defaultPreferences()
        );
        AssignmentPreviewAiResponse response = response(
                List.of(roundResponse(1, responseCourt(1, "p1", "p1", null, null))),
                List.of(warning("PARTIAL_ASSIGNMENT"))
        );

        // when: 품질 평가를 수행한다.
        AssignmentPreviewQualityReport report = evaluator.evaluate(command, response);

        // then: 중복 참가자 실패 사유가 기록된다.
        then(report.duplicateParticipantsInAnyRound()).isTrue();
        then(report.pass()).isFalse();
        then(report.failureReasons())
                .containsExactly(AssignmentPreviewQualityReport.FailureReason.DUPLICATE_PARTICIPANT_IN_ROUND);
    }

    @Test
    @DisplayName("요청에 없는 참가자가 있으면 unknown participant로 실패한다.")
    void evaluate_whenResponseContainsUnknownParticipant_reportsFailure() {
        // given: 요청에 없는 p999가 포함된 응답을 준비한다.
        CreateFreeGameAssignmentPreviewCommand command = command(
                List.of(participant("p1"), participant("p2")),
                List.of(round(1, court(1, "p1", null, null, null))),
                List.of(),
                defaultPreferences()
        );
        AssignmentPreviewAiResponse response = response(
                List.of(roundResponse(1, responseCourt(1, "p1", "p999", null, null))),
                List.of(warning("PARTIAL_ASSIGNMENT"))
        );

        // when: 품질 평가를 수행한다.
        AssignmentPreviewQualityReport report = evaluator.evaluate(command, response);

        // then: unknown participant count와 실패 사유가 기록된다.
        then(report.unknownParticipantCount()).isEqualTo(1);
        then(report.pass()).isFalse();
        then(report.failureReasons())
                .containsExactly(AssignmentPreviewQualityReport.FailureReason.UNKNOWN_PARTICIPANT);
    }

    @Test
    @DisplayName("빈 슬롯만 채우기 정책에서 기존 슬롯이 바뀌면 실패한다.")
    void evaluate_whenFillEmptySlotsChangesExistingAssignment_reportsFailure() {
        // given: 기존 p1 슬롯이 p2로 바뀐 응답을 준비한다.
        CreateFreeGameAssignmentPreviewCommand command = command(
                List.of(participant("p1"), participant("p2")),
                List.of(round(1, court(1, "p1", null, null, null))),
                List.of(),
                defaultPreferences()
        );
        AssignmentPreviewAiResponse response = response(
                List.of(roundResponse(1, responseCourt(1, "p2", null, null, null))),
                List.of(warning("PARTIAL_ASSIGNMENT"))
        );

        // when: 품질 평가를 수행한다.
        AssignmentPreviewQualityReport report = evaluator.evaluate(command, response);

        // then: 기존 슬롯 변경 count와 실패 사유가 기록된다.
        then(report.changedExistingAssignmentsCount()).isEqualTo(1);
        then(report.pass()).isFalse();
        then(report.failureReasons())
                .containsExactly(AssignmentPreviewQualityReport.FailureReason.EXISTING_ASSIGNMENT_CHANGED);
    }

    @Test
    @DisplayName("기존보다 채워진 슬롯 수가 줄면 negative delta 실패를 기록한다.")
    void evaluate_whenResponseLosesAssignments_reportsNegativeDeltaFailure() {
        // given: 기존 배정보다 하나 적게 채운 응답을 준비한다.
        CreateFreeGameAssignmentPreviewCommand command = command(
                List.of(participant("p1"), participant("p2")),
                List.of(round(1, court(1, "p1", "p2", null, null))),
                List.of(),
                preferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.IGNORE_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.REASSIGN_ALL
                )
        );
        AssignmentPreviewAiResponse response = response(
                List.of(roundResponse(1, responseCourt(1, null, "p2", null, null))),
                List.of(warning("PARTIAL_ASSIGNMENT"))
        );

        // when: 품질 평가를 수행한다.
        AssignmentPreviewQualityReport report = evaluator.evaluate(command, response);

        // then: filled slot delta 감소가 실패 사유로 기록된다.
        then(report.filledSlotDelta()).isEqualTo(-1);
        then(report.pass()).isFalse();
        then(report.failureReasons())
                .containsExactly(AssignmentPreviewQualityReport.FailureReason.NEGATIVE_FILLED_SLOT_DELTA);
    }

    @Test
    @DisplayName("여러 라운드가 동일한 round layout을 반복하면 실패한다.")
    void evaluate_whenResponseRepeatsSameRoundLayout_reportsFailure() {
        // given: 두 라운드가 동일한 코트 배치를 그대로 반복하는 응답을 준비한다.
        CreateFreeGameAssignmentPreviewCommand command = command(
                List.of(participant("p1"), participant("p2")),
                List.of(
                        round(1, court(1, "p1", null, null, null)),
                        round(2, court(1, null, null, null, null))
                ),
                List.of(),
                defaultPreferences()
        );
        AssignmentPreviewAiResponse response = response(
                List.of(
                        roundResponse(1, responseCourt(1, "p1", "p2", null, null)),
                        roundResponse(2, responseCourt(1, "p1", "p2", null, null))
                ),
                List.of(warning("PARTIAL_ASSIGNMENT"))
        );

        // when: 품질 평가를 수행한다.
        AssignmentPreviewQualityReport report = evaluator.evaluate(command, response);

        // then: repeated round layout 실패 사유가 기록된다.
        then(report.repeatedRoundLayoutAcrossRounds()).isTrue();
        then(report.pass()).isFalse();
        then(report.failureReasons())
                .containsExactly(AssignmentPreviewQualityReport.FailureReason.REPEATED_ROUND_LAYOUT);
    }

    @Test
    @DisplayName("파트너 pair를 만족하지 못했고 warning도 없으면 실패한다.")
    void evaluate_whenPartnerPairNotSatisfiedWithoutWarning_reportsFailure() {
        // given: 파트너 pair가 서로 다른 코트에 배정된 응답을 준비한다.
        CreateFreeGameAssignmentPreviewCommand command = command(
                List.of(participant("p1"), participant("p2"), participant("p3"), participant("p4")),
                List.of(
                        round(
                                1,
                                court(1, null, null, null, null),
                                court(2, null, null, null, null)
                        )
                ),
                List.of(partnerPair("p1", "p2")),
                preferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );
        AssignmentPreviewAiResponse response = response(
                List.of(
                        roundResponse(
                                1,
                                responseCourt(1, "p1", "p3", null, null),
                                responseCourt(2, "p2", "p4", null, null)
                        )
                ),
                List.of()
        );

        // when: 품질 평가를 수행한다.
        AssignmentPreviewQualityReport report = evaluator.evaluate(command, response);

        // then: 파트너 warning 누락 실패와 만족 count가 기록된다.
        then(report.satisfiedPartnerPairCount()).isZero();
        then(report.pass()).isFalse();
        then(report.failureReasons())
                .containsExactly(
                        AssignmentPreviewQualityReport.FailureReason.MISSING_PARTNER_CONSTRAINT_WARNING
                );
    }

    private CreateFreeGameAssignmentPreviewCommand command(
            List<CreateFreeGameAssignmentPreviewCommand.Participant> participants,
            List<CreateFreeGameAssignmentPreviewCommand.Round> rounds,
            List<CreateFreeGameAssignmentPreviewCommand.PartnerPairs> partnerPairs,
            CreateFreeGameAssignmentPreviewCommand.Preferences preferences
    ) {
        return new CreateFreeGameAssignmentPreviewCommand(participants, rounds, partnerPairs, preferences);
    }

    private CreateFreeGameAssignmentPreviewCommand.Participant participant(String clientId) {
        return new CreateFreeGameAssignmentPreviewCommand.Participant(
                clientId,
                clientId,
                Gender.MALE,
                20,
                Grade.S,
                0
        );
    }

    private CreateFreeGameAssignmentPreviewCommand.Round round(
            int roundNumber,
            CreateFreeGameAssignmentPreviewCommand.Court... courts
    ) {
        return new CreateFreeGameAssignmentPreviewCommand.Round(roundNumber, List.of(courts));
    }

    private CreateFreeGameAssignmentPreviewCommand.Court court(
            int courtNumber,
            String slot1,
            String slot2,
            String slot3,
            String slot4
    ) {
        return new CreateFreeGameAssignmentPreviewCommand.Court(
                courtNumber,
                Arrays.asList(slot1, slot2, slot3, slot4)
        );
    }

    private CreateFreeGameAssignmentPreviewCommand.PartnerPairs partnerPair(
            String participantId1,
            String participantId2
    ) {
        return new CreateFreeGameAssignmentPreviewCommand.PartnerPairs(participantId1, participantId2);
    }

    private CreateFreeGameAssignmentPreviewCommand.Preferences defaultPreferences() {
        return preferences(
                CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.IGNORE_PARTNERS,
                CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
        );
    }

    private CreateFreeGameAssignmentPreviewCommand.Preferences preferences(
            CreateFreeGameAssignmentPreviewCommand.PartnerPolicy partnerPolicy,
            CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy existingAssignmentPolicy
    ) {
        return new CreateFreeGameAssignmentPreviewCommand.Preferences(partnerPolicy, existingAssignmentPolicy);
    }

    private AssignmentPreviewAiResponse response(
            List<AssignmentPreviewAiResponse.Round> rounds,
            List<AssignmentPreviewAiResponse.Warning> warnings
    ) {
        return new AssignmentPreviewAiResponse(rounds, warnings);
    }

    private AssignmentPreviewAiResponse.Round roundResponse(
            int roundNumber,
            AssignmentPreviewAiResponse.Court... courts
    ) {
        return new AssignmentPreviewAiResponse.Round(roundNumber, List.of(courts));
    }

    private AssignmentPreviewAiResponse.Court responseCourt(
            int courtNumber,
            String slot1,
            String slot2,
            String slot3,
            String slot4
    ) {
        return new AssignmentPreviewAiResponse.Court(
                courtNumber,
                Arrays.asList(slot1, slot2, slot3, slot4)
        );
    }

    private AssignmentPreviewAiResponse.Warning warning(String code) {
        return new AssignmentPreviewAiResponse.Warning(code, code);
    }
}
