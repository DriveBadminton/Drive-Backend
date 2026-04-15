package com.gumraze.rallyon.backend.courtManager.adapter.out.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.court-manager.assignment-preview.ai")
public class AssignmentPreviewAiProperties {

    private String model = AssignmentPreviewAiDefaults.DEFAULT_MODEL;
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration connectionRequestTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(90);
    private Integer maxCompletionTokens = AssignmentPreviewAiDefaults.DEFAULT_MAX_COMPLETION_TOKENS;

    public static AssignmentPreviewAiProperties defaults() {
        return new AssignmentPreviewAiProperties();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(Duration connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Integer getMaxCompletionTokens() {
        return maxCompletionTokens;
    }

    public void setMaxCompletionTokens(Integer maxCompletionTokens) {
        this.maxCompletionTokens = maxCompletionTokens;
    }
}
