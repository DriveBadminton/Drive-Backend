package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.support.AssignmentPreviewEvalCase;
import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.support.AssignmentPreviewQualityEvaluator;
import com.gumraze.rallyon.backend.courtManager.adapter.out.ai.support.AssignmentPreviewQualityReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

@Tag("live-ai")
@Tag("live-ai-benchmark")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "OPENAI_LIVE_BENCHMARK_TEST", matches = "true")
class AssignmentPreviewLiveBenchmarkTest {

    private static final Logger log = LoggerFactory.getLogger(AssignmentPreviewLiveBenchmarkTest.class);

    private final AssignmentPreviewQualityEvaluator evaluator = new AssignmentPreviewQualityEvaluator();

    private SpringAiAssignmentPreviewGateway gateway;
    private String modelName;

    @BeforeAll
    void setUp() {
        this.modelName = AssignmentPreviewLiveTestSupport.resolveModelName();
        this.gateway = AssignmentPreviewLiveTestSupport.createGateway(modelName);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("benchmarkCases")
    @Timeout(300)
    @DisplayName("실모델 60슬롯 benchmark는 대형 입력에서도 기본 품질 기준을 만족해야 한다.")
    void generate_largeLiveModelResponse_meetsQualityThreshold(AssignmentPreviewEvalCase evalCase) {
        Instant startedAt = Instant.now();
        AssignmentPreviewAiResponse response = gateway.generate(evalCase.toCommand());
        long elapsedMs = Duration.between(startedAt, Instant.now()).toMillis();
        AssignmentPreviewQualityReport report = evaluator.evaluate(evalCase.toCommand(), response);

        log.info(
                "live-ai-benchmark case={}, model={}, elapsedMs={}, filledSlotDelta={}, warningCodes={}, report={}",
                evalCase.scenario(),
                modelName,
                elapsedMs,
                report.filledSlotDelta(),
                report.warningCodes(),
                report
        );

        then(report.pass()).isTrue();
        then(report.filledSlotDelta()).isGreaterThanOrEqualTo(evalCase.expected().minFilledSlotDelta());
        then(report.satisfiedPartnerPairCount())
                .isGreaterThanOrEqualTo(evalCase.expected().expectedSatisfiedPartnerPairCount());
        then(report.warningCodes()).containsAll(evalCase.expected().expectedWarningCodes());
        if (!evalCase.expected().forbiddenWarningCodes().isEmpty()) {
            then(report.warningCodes()).doesNotContainAnyElementsOf(evalCase.expected().forbiddenWarningCodes());
        }
    }

    private static Stream<Named<AssignmentPreviewEvalCase>> benchmarkCases() {
        return List.of(
                        "benchmark-60-slots-fill-all.json",
                        "benchmark-60-slots-preserve-fixed.json",
                        "benchmark-60-slots-partial-shortage.json"
                ).stream()
                .map(AssignmentPreviewEvalCase::load)
                .map(evalCase -> Named.of(evalCase.scenario(), evalCase));
    }
}
