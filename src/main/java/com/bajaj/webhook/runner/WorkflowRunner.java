package com.bajaj.webhook.runner;

import com.bajaj.webhook.dto.WebhookGenerationRequest;
import com.bajaj.webhook.dto.WebhookGenerationResponse;
import com.bajaj.webhook.service.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class WorkflowRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(WorkflowRunner.class);

    private final WebhookService webhookService;

    @Value("${user.name}")
    private String userName;

    @Value("${user.regNo}")
    private String regNo;

    @Value("${user.email}")
    private String email;

    @Autowired
    public WorkflowRunner(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("========================================");
        log.info("Starting Webhook Workflow");
        log.info("========================================");

        try {
            // Step 1: Generate webhook
            log.info("Step 1: Generating webhook for user {} ({})", userName, regNo);
            WebhookGenerationRequest request = new WebhookGenerationRequest();
            request.setName(userName);
            request.setRegNo(regNo);
            request.setEmail(email);
            WebhookGenerationResponse webhookResponse = webhookService.generateWebhook(request);

            if (webhookResponse == null) {
                log.error("Failed to generate webhook - received null response");
                return;
            }

            // Step 2: Determine question URL based on registration number
            log.info("Step 2: Determining question URL based on registration number");
            String questionUrl = webhookService.getQuestionUrl(regNo);

            // Step 3: Process question and generate SQL query
            log.info("Step 3: Processing question and generating SQL query");
            String sqlQuery = webhookService.processQuestionAndGenerateQuery(questionUrl);

            // Step 4: Send test webhook with SQL query
            log.info("Step 4: Sending test webhook with generated query");
            webhookService.sendTestWebhook(webhookResponse.getAccessToken(), sqlQuery);

            log.info("========================================");
            log.info("Webhook Workflow Completed Successfully!");
            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("Webhook Workflow Failed with error: {}", e.getMessage(), e);
            log.error("========================================");
        }
    }
}
