package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.support.AssignmentPreviewEvalCase;
import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.support.AssignmentPreviewQualityEvaluator;
import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.support.AssignmentPreviewQualityReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

class AssignmentPreviewFixtureRegressionTest {

    private static final List<String> FIXTURE_FILE_NAMES = List.of(
            "pass-fill-empty-basic.json",
            "pass-all-slots-already-filled-no-op.json",
            "pass-fixed-slots-preserved-with-partial-warning.json",
            "pass-fixed-anchors-multi-round-varied-layout.json",
            "pass-full-fill-warning-normalized.json",
            "pass-partner-pair-satisfied.json",
            "pass-partner-pair-partial-with-warning.json",
            "pass-partner-pair-unavoidable-partial-warning.json",
            "pass-reassign-all-basic.json",
            "pass-multi-round-varied-layout.json",
            "pass-imbalanced-participant-count.json",
            "pass-large-partial-shortage.json",
            "fail-duplicate-participant-in-round.json",
            "fail-repeated-round-layout.json",
            "fail-unknown-participant-id.json",
            "fail-existing-assignment-changed.json",
            "fail-silent-no-op-with-empty-slots.json"
    );

    private final AssignmentPreviewQualityEvaluator evaluator = new AssignmentPreviewQualityEvaluator();

    @ParameterizedTest(name = "{0}")
    @MethodSource("fixtures")
    @DisplayName("curated fixture 회귀 케이스를 규칙 기반으로 평가한다.")
    void evaluate_fixtureCases_matchExpectedQualitySignals(AssignmentPreviewEvalCase evalCase) {
        // given: curated fixture 케이스를 불러온다.
        // when: fixture 응답을 규칙 기반으로 평가한다.
        AssignmentPreviewQualityReport report = evaluator.evaluate(evalCase.toCommand(), evalCase.toResponse());

        // then: pass/failure reason/warning threshold가 기대값과 일치한다.
        then(report.pass()).isEqualTo(evalCase.expected().pass());
        then(report.filledSlotDelta()).isGreaterThanOrEqualTo(evalCase.expected().minFilledSlotDelta());
        then(report.satisfiedPartnerPairCount())
                .isEqualTo(evalCase.expected().expectedSatisfiedPartnerPairCount());
        then(report.warningCodes()).containsAll(evalCase.expected().expectedWarningCodes());
        if (!evalCase.expected().forbiddenWarningCodes().isEmpty()) {
            then(report.warningCodes()).doesNotContainAnyElementsOf(evalCase.expected().forbiddenWarningCodes());
        }
        then(report.failureReasons())
                .containsExactlyInAnyOrderElementsOf(evalCase.expected().expectedFailureReasons());
    }

    private static Stream<Named<AssignmentPreviewEvalCase>> fixtures() {
        return FIXTURE_FILE_NAMES.stream()
                .map(fileName -> AssignmentPreviewEvalCase.load(fileName))
                .map(evalCase -> Named.of(evalCase.scenario(), evalCase));
    }
}
