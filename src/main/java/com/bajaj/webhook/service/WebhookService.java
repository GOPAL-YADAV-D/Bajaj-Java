package com.bajaj.webhook.service;

import com.bajaj.webhook.dto.TestWebhookRequest;
import com.bajaj.webhook.dto.WebhookGenerationRequest;
import com.bajaj.webhook.dto.WebhookGenerationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final WebClient webClient;

    @Value("${webhook.generate.url}")
    private String generateWebhookUrl;

    @Value("${webhook.test.url}")
    private String testWebhookUrl;

    @Value("${webhook.question1.url}")
    private String question1Url;

    @Value("${webhook.question2.url}")
    private String question2Url;

    @Autowired
    public WebhookService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Generate webhook by sending user information
     * Includes retry logic with exponential backoff
     */
    @Retryable(
            retryFor = {WebClientResponseException.class, Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public WebhookGenerationResponse generateWebhook(WebhookGenerationRequest request) {
        log.info("Generating webhook for user: {}", request.getName());
        
        try {
            WebhookGenerationResponse response = webClient.post()
                    .uri(generateWebhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(WebhookGenerationResponse.class)
                    .block();

            log.info("Successfully generated webhook. Access Token: {}", 
                    response != null ? maskToken(response.getAccessToken()) : "null");
            log.info("Webhook URL: {}", response != null ? response.getWebhookUrl() : "null");
            
            return response;
        } catch (WebClientResponseException e) {
            log.error("Error generating webhook - Status: {}, Response: {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error generating webhook", e);
            throw e;
        }
    }

    /**
     * Determine which question URL to use based on registration number
     */
    public String getQuestionUrl(String regNo) {
        // Extract last two digits
        int lastTwoDigits = Integer.parseInt(regNo.substring(regNo.length() - 2));
        
        String questionUrl;
        if (lastTwoDigits % 2 == 0) {
            log.info("Registration number ends with even digits ({}). Using Question 2.", lastTwoDigits);
            questionUrl = question2Url;
        } else {
            log.info("Registration number ends with odd digits ({}). Using Question 1.", lastTwoDigits);
            questionUrl = question1Url;
        }
        
        return questionUrl;
    }

    /**
     * Process the question and generate SQL query
     * Currently returns a mock SQL query as per requirements
     */
    public String processQuestionAndGenerateQuery(String questionUrl) {
        log.info("Processing question from URL: {}", questionUrl);
        
        // Mock SQL query generation as per requirements
        // In production, this would fetch and parse the question document
        String sqlQuery = "SELECT " +
                "d.DEPARTMENT_NAME, " +
                "AVG(TIMESTAMPDIFF(YEAR, e.DOB, CURRENT_DATE)) AS AVERAGE_AGE, " +
                "(SELECT GROUP_CONCAT(fullname ORDER BY fullname SEPARATOR ', ') " +
                "FROM (SELECT CONCAT(e2.FIRST_NAME, ' ', e2.LAST_NAME) AS fullname " +
                "FROM EMPLOYEE e2 " +
                "JOIN PAYMENTS p2 ON e2.EMP_ID = p2.EMP_ID " +
                "WHERE p2.AMOUNT > 70000 AND e2.DEPARTMENT = d.DEPARTMENT_ID " +
                "GROUP BY e2.EMP_ID " +
                "ORDER BY fullname " +
                "LIMIT 10) AS t) AS EMPLOYEE_LIST " +
                "FROM DEPARTMENT d " +
                "JOIN EMPLOYEE e ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                "JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID " +
                "WHERE p.AMOUNT > 70000 " +
                "GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME " +
                "ORDER BY d.DEPARTMENT_ID DESC";
        
        log.info("Generated SQL query: {}", sqlQuery);
        return sqlQuery;
    }

    /**
     * Send test webhook with final SQL query
     * Includes retry logic with exponential backoff
     */
    @Retryable(
            retryFor = {WebClientResponseException.class, Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendTestWebhook(String accessToken, String finalQuery) {
        log.info("Sending test webhook with query: {}", finalQuery);
        
        TestWebhookRequest request = new TestWebhookRequest();
        request.setFinalQuery(finalQuery);
        
        try {
            String response = webClient.post()
                    .uri(testWebhookUrl)
                    .header("Authorization", accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Test webhook sent successfully. Response: {}", response);
        } catch (WebClientResponseException e) {
            log.error("Error sending test webhook - Status: {}, Response: {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error sending test webhook", e);
            throw e;
        }
    }

    /**
     * Mask access token for logging (show only first and last 4 characters)
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "****";
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}
