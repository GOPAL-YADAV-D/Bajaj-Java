package com.bajaj.webhook.dto;

public class WebhookGenerationResponse {
    private String webhookUrl;
    private String accessToken;

    public WebhookGenerationResponse() {
    }

    public WebhookGenerationResponse(String webhookUrl, String accessToken) {
        this.webhookUrl = webhookUrl;
        this.accessToken = accessToken;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
