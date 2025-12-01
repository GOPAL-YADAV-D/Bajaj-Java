package com.bajaj.webhook.service;

import com.bajaj.webhook.dto.TestWebhookRequest;
import com.bajaj.webhook.dto.WebhookGenerationRequest;
import com.bajaj.webhook.dto.WebhookGenerationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private WebhookService webhookService;

    private static final String GENERATE_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
    private static final String TEST_URL = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
    private static final String QUESTION1_URL = "https://drive.google.com/file/d/1LAPx2to9zmN5DY0tkMrJRNvJrNVx1gnR/view";
    private static final String QUESTION2_URL = "https://drive.google.com/file/d/1b0p5C-6fUrUQglJVaWWAAB3P12lfoBCH/view";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(webhookService, "generateWebhookUrl", GENERATE_URL);
        ReflectionTestUtils.setField(webhookService, "testWebhookUrl", TEST_URL);
        ReflectionTestUtils.setField(webhookService, "question1Url", QUESTION1_URL);
        ReflectionTestUtils.setField(webhookService, "question2Url", QUESTION2_URL);
    }

    @Test
    void testGenerateWebhook_Success() {
        // Arrange
        WebhookGenerationRequest request = new WebhookGenerationRequest();
        request.setName("Gopal Yadav");
        request.setRegNo("22BCT0094");
        request.setEmail("gopalyadav6560@gmail.com");

        WebhookGenerationResponse expectedResponse = new WebhookGenerationResponse();
        expectedResponse.setWebhookUrl("https://example.com/webhook");
        expectedResponse.setAccessToken("test-token-12345");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(GENERATE_URL)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(WebhookGenerationResponse.class))
                .thenReturn(Mono.just(expectedResponse));

        // Act
        WebhookGenerationResponse actualResponse = webhookService.generateWebhook(request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getWebhookUrl(), actualResponse.getWebhookUrl());
        assertEquals(expectedResponse.getAccessToken(), actualResponse.getAccessToken());
        verify(webClient).post();
    }

    @Test
    void testGenerateWebhook_Failure() {
        // Arrange
        WebhookGenerationRequest request = new WebhookGenerationRequest();
        request.setName("Gopal Yadav");
        request.setRegNo("22BCT0094");
        request.setEmail("gopalyadav6560@gmail.com");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(GENERATE_URL)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(WebhookGenerationResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> webhookService.generateWebhook(request));
    }

    @Test
    void testGetQuestionUrl_OddRegNo() {
        // Arrange - regNo ends with 93 (odd)
        String regNo = "22BCT0093";

        // Act
        String questionUrl = webhookService.getQuestionUrl(regNo);

        // Assert
        assertEquals(QUESTION1_URL, questionUrl);
    }

    @Test
    void testGetQuestionUrl_EvenRegNo() {
        // Arrange - regNo ends with 94 (even)
        String regNo = "22BCT0094";

        // Act
        String questionUrl = webhookService.getQuestionUrl(regNo);

        // Assert
        assertEquals(QUESTION2_URL, questionUrl);
    }

    @Test
    void testProcessQuestionAndGenerateQuery() {
        // Arrange
        String questionUrl = QUESTION1_URL;

        // Act
        String sqlQuery = webhookService.processQuestionAndGenerateQuery(questionUrl);

        // Assert
        assertNotNull(sqlQuery);
        assertTrue(sqlQuery.contains("SELECT"));
    }

    @Test
    void testSendTestWebhook_Success() {
        // Arrange
        String accessToken = "test-token-12345";
        String finalQuery = "SELECT * FROM users";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TEST_URL)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("Authorization"), eq(accessToken))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just("Success"));

        // Act
        webhookService.sendTestWebhook(accessToken, finalQuery);

        // Assert
        verify(webClient).post();
        verify(requestBodySpec).header("Authorization", accessToken);
    }

    @Test
    void testSendTestWebhook_Failure() {
        // Arrange
        String accessToken = "test-token-12345";
        String finalQuery = "SELECT * FROM users";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TEST_URL)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            webhookService.sendTestWebhook(accessToken, finalQuery));
    }
}
