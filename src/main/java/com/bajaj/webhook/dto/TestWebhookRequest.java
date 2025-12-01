package com.bajaj.webhook.dto;

public class TestWebhookRequest {
    private String finalQuery;

    public TestWebhookRequest() {
    }

    public TestWebhookRequest(String finalQuery) {
        this.finalQuery = finalQuery;
    }

    public String getFinalQuery() {
        return finalQuery;
    }

    public void setFinalQuery(String finalQuery) {
        this.finalQuery = finalQuery;
    }
}
