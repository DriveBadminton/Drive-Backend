package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import io.micrometer.observation.ObservationRegistry;
import org.apache.hc.client5.http.config.RequestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.core.retry.RetryTemplate;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.mock;

class AssignmentPreviewAiConfigTest {

    @Test
    @DisplayName("AI timeout 설정값을 OpenAI 요청 config에 반영한다")
    void createRequestConfig_appliesConfiguredTimeouts() {
        AssignmentPreviewAiTimeoutProperties properties = new AssignmentPreviewAiTimeoutProperties();
        properties.setConnectTimeout(Duration.ofSeconds(3));
        properties.setConnectionRequestTimeout(Duration.ofSeconds(4));
        properties.setReadTimeout(Duration.ofSeconds(25));

        RequestConfig requestConfig = AssignmentPreviewAiConfig.createRequestConfig(properties);

        then(requestConfig.getConnectTimeout().toMilliseconds()).isEqualTo(3_000L);
        then(requestConfig.getConnectionRequestTimeout().toMilliseconds()).isEqualTo(4_000L);
        then(requestConfig.getResponseTimeout().toMilliseconds()).isEqualTo(25_000L);
    }

    @Test
    @DisplayName("preview retry template은 실패해도 자동 재시도하지 않는다")
    void createPreviewRetryTemplate_doesNotRetry() {
        RetryTemplate retryTemplate = AssignmentPreviewAiConfig.createPreviewRetryTemplate();
        AtomicInteger attempts = new AtomicInteger();

        assertThatThrownBy(() -> RetryUtils.execute(retryTemplate, () -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        then(attempts.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("preview chat model은 no-retry template을 사용한다")
    void assignmentPreviewChatModel_usesNoRetryTemplate() throws Exception {
        AssignmentPreviewAiConfig config = new AssignmentPreviewAiConfig();
        OpenAiChatProperties chatProperties = new OpenAiChatProperties();

        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        OpenAiChatModel chatModel = config.assignmentPreviewChatModel(
                mock(OpenAiApi.class),
                chatProperties,
                beanFactory.getBeanProvider(ToolCallingManager.class),
                beanFactory.getBeanProvider(ObservationRegistry.class),
                beanFactory.getBeanProvider(ChatModelObservationConvention.class),
                beanFactory.getBeanProvider(ToolExecutionEligibilityPredicate.class)
        );

        Field retryTemplateField = OpenAiChatModel.class.getDeclaredField("retryTemplate");
        retryTemplateField.setAccessible(true);
        RetryTemplate retryTemplate = (RetryTemplate) retryTemplateField.get(chatModel);
        AtomicInteger attempts = new AtomicInteger();

        assertThatThrownBy(() -> RetryUtils.execute(retryTemplate, () -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        then(attempts.get()).isEqualTo(1);
    }
}
