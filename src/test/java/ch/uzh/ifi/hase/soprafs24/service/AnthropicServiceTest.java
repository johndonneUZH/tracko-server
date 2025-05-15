package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import ch.uzh.ifi.hase.soprafs24.config.AnthropicConfig;
import ch.uzh.ifi.hase.soprafs24.exceptions.AnthropicApiException;
import ch.uzh.ifi.hase.soprafs24.exceptions.AnthropicRateLimitException;
import ch.uzh.ifi.hase.soprafs24.exceptions.AnthropicTimeoutException;
import ch.uzh.ifi.hase.soprafs24.models.ai.AnthropicRequestDTO;
import ch.uzh.ifi.hase.soprafs24.models.ai.AnthropicResponseDTO;
import ch.uzh.ifi.hase.soprafs24.models.ai.ContentDTO;

@SpringBootTest(classes = {AnthropicConfig.class, AnthropicRateLimiterService.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "anthropic.enabled=true",
    "anthropic.api-key=test-api-key",
    "anthropic.model=test-model",
    "anthropic.max-tokens=1000",
    "anthropic.temperature=0.7",
    "anthropic.rate-limit=10"
})
public class AnthropicServiceTest {

    private AnthropicService anthropicService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private AnthropicConfig anthropicConfig;

    @MockBean
    private AnthropicRateLimiterService rateLimiterService;

    private final String API_URL = "https://api.anthropic.com/v1/messages";
    private final String TEST_PROMPT = "Test prompt";
    private final String API_KEY = "test-api-key";
    private final String MODEL = "claude-3-7-sonnet-20250219";

    @BeforeEach
    public void setup() {
        when(anthropicConfig.getApiKey()).thenReturn(API_KEY);
        when(anthropicConfig.getModel()).thenReturn(MODEL);
        when(anthropicConfig.getMaxTokens()).thenReturn(20000);
        when(anthropicConfig.getTemperature()).thenReturn(1.0);
        when(rateLimiterService.acquirePermit()).thenReturn(true);

        anthropicService = new AnthropicService(restTemplate, anthropicConfig, rateLimiterService);
    }

    @Test
    public void generateContent_success() {
        AnthropicResponseDTO mockResponse = new AnthropicResponseDTO();
        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setType("text");
        contentDTO.setText("AI generated response");
        mockResponse.setContent(List.of(contentDTO));

        when(restTemplate.postForObject(eq(API_URL), any(HttpEntity.class), eq(AnthropicResponseDTO.class)))
                .thenReturn(mockResponse);

        AnthropicResponseDTO result = anthropicService.generateContent(TEST_PROMPT);

        assertNotNull(result);
        assertEquals("AI generated response", result.getContent().get(0).getText());
        
        ArgumentCaptor<HttpEntity<AnthropicRequestDTO>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(API_URL), requestCaptor.capture(), eq(AnthropicResponseDTO.class));
        
        AnthropicRequestDTO requestDTO = requestCaptor.getValue().getBody();
        assertEquals(MODEL, requestDTO.getModel());
        assertEquals(20000, requestDTO.getMaxTokens());
        assertEquals(1.0, requestDTO.getTemperature());
        assertEquals("user", requestDTO.getMessages().get(0).getRole());
        assertEquals(TEST_PROMPT, requestDTO.getMessages().get(0).getContent().get(0).getText());
    }

    @Test
    public void generateContent_rateLimitExceeded() {
        when(rateLimiterService.acquirePermit()).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            anthropicService.generateContent(TEST_PROMPT);
        });
        
        assertEquals("API rate limit exceeded. Please try again later.", exception.getMessage());
    }

    @Test
    public void generateContent_apiRateLimited() {
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS, 
                "Too Many Requests",           
                null,                          
                null,                          
                null                           
        );
        
        when(restTemplate.postForObject(eq(API_URL), any(HttpEntity.class), eq(AnthropicResponseDTO.class)))
                .thenThrow(exception);

        assertThrows(AnthropicRateLimitException.class, () -> {
            anthropicService.generateContent(TEST_PROMPT);
        });
    }

    @Test
    public void generateContent_timeout() {
        ResourceAccessException exception = new ResourceAccessException("Connection timed out");
        
        when(restTemplate.postForObject(eq(API_URL), any(HttpEntity.class), eq(AnthropicResponseDTO.class)))
                .thenThrow(exception);

        assertThrows(AnthropicTimeoutException.class, () -> {
            anthropicService.generateContent(TEST_PROMPT);
        });
    }

    @Test
    public void generateContent_authError() {
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED,       
                "Unauthorized",                 
                null,                          
                null,                          
                null                           
        );
        
        when(restTemplate.postForObject(eq(API_URL), any(HttpEntity.class), eq(AnthropicResponseDTO.class)))
                .thenThrow(exception);
    
        assertThrows(AnthropicApiException.class, () -> {
            anthropicService.generateContent(TEST_PROMPT);
        });
    }

    @Test
    public void refineIdea_success() {
        AnthropicResponseDTO mockResponse = new AnthropicResponseDTO();
        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setType("text");
        contentDTO.setText("Refined idea");
        mockResponse.setContent(List.of(contentDTO));

        when(restTemplate.postForObject(eq(API_URL), any(HttpEntity.class), eq(AnthropicResponseDTO.class)))
                .thenReturn(mockResponse);

        AnthropicResponseDTO result = anthropicService.refineIdea("Raw idea");

        assertNotNull(result);
        assertEquals("Refined idea", result.getContent().get(0).getText());
        
        ArgumentCaptor<HttpEntity<AnthropicRequestDTO>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(API_URL), requestCaptor.capture(), eq(AnthropicResponseDTO.class));
        
        String capturedPrompt = requestCaptor.getValue().getBody().getMessages().get(0).getContent().get(0).getText();
        assertTrue(capturedPrompt.startsWith("Refine this idea into a more innovative and detailed concept:"));
        assertTrue(capturedPrompt.contains("Raw idea"));
    }

    @Test
    public void combineIdeas_success() {
        AnthropicResponseDTO mockResponse = new AnthropicResponseDTO();
        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setType("text");
        contentDTO.setText("Combined idea");
        mockResponse.setContent(List.of(contentDTO));

        when(restTemplate.postForObject(eq(API_URL), any(HttpEntity.class), eq(AnthropicResponseDTO.class)))
                .thenReturn(mockResponse);

        AnthropicResponseDTO result = anthropicService.combineIdeas("Idea 1", "Idea 2");

        assertNotNull(result);
        assertEquals("Combined idea", result.getContent().get(0).getText());
        
        ArgumentCaptor<HttpEntity<AnthropicRequestDTO>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(API_URL), requestCaptor.capture(), eq(AnthropicResponseDTO.class));
        
        String capturedPrompt = requestCaptor.getValue().getBody().getMessages().get(0).getContent().get(0).getText();
        assertTrue(capturedPrompt.contains("Idea 1"));
        assertTrue(capturedPrompt.contains("Idea 2"));
    }

    private static boolean assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
        return condition;
    }
}