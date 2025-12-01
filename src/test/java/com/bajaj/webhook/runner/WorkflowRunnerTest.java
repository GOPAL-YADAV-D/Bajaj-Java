package com.bajaj.webhook.runner;

import com.bajaj.webhook.dto.WebhookGenerationRequest;
import com.bajaj.webhook.dto.WebhookGenerationResponse;
import com.bajaj.webhook.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowRunnerTest {

    @Mock
    private WebhookService webhookService;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private WorkflowRunner workflowRunner;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workflowRunner, "userName", "Gopal Yadav");
        ReflectionTestUtils.setField(workflowRunner, "regNo", "22BCT0094");
        ReflectionTestUtils.setField(workflowRunner, "email", "gopalyadav6560@gmail.com");
    }

    @Test
    void testRun_Success() {
        // Arrange
        WebhookGenerationResponse webhookResponse = new WebhookGenerationResponse();
        webhookResponse.setWebhookUrl("https://example.com/webhook");
        webhookResponse.setAccessToken("test-token-12345");

        when(webhookService.generateWebhook(any(WebhookGenerationRequest.class)))
                .thenReturn(webhookResponse);
        when(webhookService.getQuestionUrl(anyString()))
                .thenReturn("https://drive.google.com/file/d/1b0p5C-6fUrUQglJVaWWAAB3P12lfoBCH/view");
        when(webhookService.processQuestionAndGenerateQuery(anyString()))
                .thenReturn("SELECT * FROM users");
        doNothing().when(webhookService).sendTestWebhook(anyString(), anyString());

        // Act
        workflowRunner.run(applicationArguments);

        // Assert
        verify(webhookService).generateWebhook(any(WebhookGenerationRequest.class));
        verify(webhookService).getQuestionUrl(anyString());
        verify(webhookService).processQuestionAndGenerateQuery(anyString());
        verify(webhookService).sendTestWebhook(anyString(), anyString());
    }

    @Test
    void testRun_NullResponse() {
        // Arrange
        when(webhookService.generateWebhook(any(WebhookGenerationRequest.class)))
                .thenReturn(null);

        // Act
        workflowRunner.run(applicationArguments);

        // Assert
        verify(webhookService).generateWebhook(any(WebhookGenerationRequest.class));
        verify(webhookService, never()).getQuestionUrl(anyString());
        verify(webhookService, never()).processQuestionAndGenerateQuery(anyString());
        verify(webhookService, never()).sendTestWebhook(anyString(), anyString());
    }

    @Test
    void testRun_Exception() {
        // Arrange
        when(webhookService.generateWebhook(any(WebhookGenerationRequest.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        workflowRunner.run(applicationArguments);

        // Assert
        verify(webhookService).generateWebhook(any(WebhookGenerationRequest.class));
        verify(webhookService, never()).getQuestionUrl(anyString());
    }
}
