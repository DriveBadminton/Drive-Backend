package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import tools.jackson.databind.ObjectMapper;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import static org.assertj.core.api.BDDAssertions.then;

final class AssignmentPreviewLiveTestSupport {

    private AssignmentPreviewLiveTestSupport() {
    }

    static String resolveModelName() {
        return System.getenv().getOrDefault(
                "OPENAI_CHAT_MODEL",
                AssignmentPreviewAiDefaults.DEFAULT_MODEL
        );
    }

    static SpringAiAssignmentPreviewGateway createGateway(String modelName) {
        String apiKey = System.getenv("OPENAI_API_KEY");

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
                        .maxCompletionTokens(AssignmentPreviewAiDefaults.DEFAULT_MAX_COMPLETION_TOKENS)
                        .build())
                .retryTemplate(AssignmentPreviewAiConfig.createPreviewRetryTemplate())
                .build();

        AssignmentPreviewAiProperties properties = AssignmentPreviewAiProperties.defaults();
        properties.setModel(modelName);

        return new SpringAiAssignmentPreviewGateway(
                new AssignmentPreviewPlanningInputMapper(),
                chatModel,
                new ObjectMapper(),
                properties
        );
    }
}
