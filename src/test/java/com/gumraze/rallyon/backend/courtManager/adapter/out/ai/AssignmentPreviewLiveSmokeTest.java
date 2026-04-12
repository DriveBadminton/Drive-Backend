package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import tools.jackson.databind.ObjectMapper;
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
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

@Tag("live-ai")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "OPENAI_LIVE_TEST", matches = "true")
class AssignmentPreviewLiveSmokeTest {

    private static final Logger log = LoggerFactory.getLogger(AssignmentPreviewLiveSmokeTest.class);

    private final AssignmentPreviewQualityEvaluator evaluator = new AssignmentPreviewQualityEvaluator();

    private SpringAiAssignmentPreviewGateway gateway;
    private String modelName;

    @BeforeAll
    void setUp() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        this.modelName = System.getenv().getOrDefault("OPENAI_CHAT_MODEL", "gpt-5-mini");

        then(apiKey)
                .as("OPENAI_API_KEY must be set when OPENAI_LIVE_TEST=true")
                .isNotBlank();

        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(apiKey)
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(modelName)
                        .temperature(0.0)
                        .build())
                .build();

        this.gateway = new SpringAiAssignmentPreviewGateway(
                new AssignmentPreviewPlanningInputMapper(),
                chatModel,
                new ObjectMapper()
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("smokeCases")
    @Timeout(120)
    @DisplayName("실모델 smoke test는 최소 품질 기준을 만족해야 한다.")
    void generate_liveModelResponse_meetsQualityThreshold(AssignmentPreviewEvalCase evalCase) {
        // given: live smoke용 curated fixture 케이스를 준비한다.
        // when: 실모델로 preview를 생성하고 품질 평가를 수행한다.
        Instant startedAt = Instant.now();
        AssignmentPreviewAiResponse response = gateway.generate(evalCase.toCommand());
        long elapsedMs = Duration.between(startedAt, Instant.now()).toMillis();
        AssignmentPreviewQualityReport report = evaluator.evaluate(evalCase.toCommand(), response);

        log.info(
                "live-ai case={}, model={}, elapsedMs={}, warningCodes={}, report={}",
                evalCase.scenario(),
                modelName,
                elapsedMs,
                report.warningCodes(),
                report
        );

        // then: exact JSON이 아니라 최소 품질 기준을 만족해야 한다.
        then(report.pass()).isTrue();
        then(report.filledSlotDelta()).isGreaterThanOrEqualTo(evalCase.expected().minFilledSlotDelta());
        then(report.satisfiedPartnerPairCount())
                .isGreaterThanOrEqualTo(evalCase.expected().expectedSatisfiedPartnerPairCount());
        then(report.warningCodes()).containsAll(evalCase.expected().expectedWarningCodes());
        if (!evalCase.expected().forbiddenWarningCodes().isEmpty()) {
            then(report.warningCodes()).doesNotContainAnyElementsOf(evalCase.expected().forbiddenWarningCodes());
        }
    }

    private static Stream<Named<AssignmentPreviewEvalCase>> smokeCases() {
        return List.of(
                        "pass-fill-empty-basic.json",
                        "pass-partner-pair-satisfied.json"
                ).stream()
                .map(AssignmentPreviewEvalCase::load)
                .map(evalCase -> Named.of(evalCase.scenario(), evalCase));
    }
}
