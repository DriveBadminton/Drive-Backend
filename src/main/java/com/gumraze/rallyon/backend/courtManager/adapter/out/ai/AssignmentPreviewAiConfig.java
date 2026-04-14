package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.model.openai.autoconfigure.OpenAIAutoConfigurationUtil;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiConnectionProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration(proxyBeanMethods = false)
public class AssignmentPreviewAiConfig {

    @Bean("assignmentPreviewOpenAiApi")
    OpenAiApi assignmentPreviewOpenAiApi(
            OpenAiConnectionProperties commonProperties,
            OpenAiChatProperties chatProperties,
            AssignmentPreviewAiTimeoutProperties timeoutProperties,
            ObjectProvider<ResponseErrorHandler> responseErrorHandler
    ) {
        OpenAIAutoConfigurationUtil.ResolvedConnectionProperties resolved =
                OpenAIAutoConfigurationUtil.resolveConnectionProperties(
                        commonProperties,
                        chatProperties,
                        "chat"
                );

        return OpenAiApi.builder()
                .baseUrl(resolved.baseUrl())
                .apiKey(new SimpleApiKey(resolved.apiKey()))
                .headers(resolved.headers())
                .completionsPath(chatProperties.getCompletionsPath())
                .embeddingsPath(OpenAiEmbeddingProperties.DEFAULT_EMBEDDINGS_PATH)
                .restClientBuilder(
                        RestClient.builder().requestFactory(createRequestFactory(timeoutProperties))
                )
                .webClientBuilder(WebClient.builder())
                .responseErrorHandler(responseErrorHandler.getIfAvailable(
                        () -> RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER
                ))
                .build();
    }

    @Bean("assignmentPreviewChatModel")
    OpenAiChatModel assignmentPreviewChatModel(
            @Qualifier("assignmentPreviewOpenAiApi") OpenAiApi openAiApi,
            OpenAiChatProperties chatProperties,
            ObjectProvider<ToolCallingManager> toolCallingManager,
            ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<ChatModelObservationConvention> observationConvention,
            ObjectProvider<ToolExecutionEligibilityPredicate> toolExecutionEligibilityPredicate
    ) {
        OpenAiChatModel.Builder builder = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(resolveChatOptions(chatProperties))
                .retryTemplate(createPreviewRetryTemplate())
                .observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP));

        toolCallingManager.ifAvailable(builder::toolCallingManager);
        toolExecutionEligibilityPredicate.ifAvailable(builder::toolExecutionEligibilityPredicate);

        OpenAiChatModel chatModel = builder.build();
        observationConvention.ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }

    static RetryTemplate createPreviewRetryTemplate() {
        return new RetryTemplate(RetryPolicy.withMaxRetries(0));
    }

    public static boolean isPreviewRetryEnabled() {
        return false;
    }

    private static OpenAiChatOptions resolveChatOptions(OpenAiChatProperties chatProperties) {
        return chatProperties.getOptions() != null
                ? chatProperties.getOptions()
                : OpenAiChatOptions.builder().build();
    }

    static HttpComponentsClientHttpRequestFactory createRequestFactory(
            AssignmentPreviewAiTimeoutProperties timeoutProperties
    ) {
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(
                        HttpClients.custom()
                                .setDefaultRequestConfig(createRequestConfig(timeoutProperties))
                                .build()
                );
        requestFactory.setConnectionRequestTimeout(timeoutProperties.getConnectionRequestTimeout());
        requestFactory.setReadTimeout(timeoutProperties.getReadTimeout());
        return requestFactory;
    }

    static RequestConfig createRequestConfig(
            AssignmentPreviewAiTimeoutProperties timeoutProperties
    ) {
        return RequestConfig.custom()
                .setConnectTimeout(Timeout.of(timeoutProperties.getConnectTimeout()))
                .setConnectionRequestTimeout(Timeout.of(timeoutProperties.getConnectionRequestTimeout()))
                .setResponseTimeout(Timeout.of(timeoutProperties.getReadTimeout()))
                .build();
    }
}
