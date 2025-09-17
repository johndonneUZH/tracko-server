package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentResponse;

import tracko.config.AIConfig;
import tracko.service.AIService;

public class AIServiceTest {

    private AIService aiService;
    private AIConfig aiConfig;
    private Client genAiClient;
    private Models mockModels;

    private final String TEST_PROMPT = "Test prompt";
    private final String MODEL = "gemini-2.0-flash-exp";

    @BeforeEach
    public void setup() throws Exception {
        // Create mocks
        aiConfig = mock(AIConfig.class);
        genAiClient = mock(Client.class);
        mockModels = mock(Models.class);
        
        when(aiConfig.getModel()).thenReturn(MODEL);
        
        // Use reflection to set the final models field
        java.lang.reflect.Field modelsField = Client.class.getDeclaredField("models");
        modelsField.setAccessible(true);
        modelsField.set(genAiClient, mockModels);

        aiService = new AIService(genAiClient, aiConfig);
    }

    @Test
    public void generateContent_success() {
        GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
        when(mockResponse.text()).thenReturn("AI generated response");

        when(mockModels.generateContent(eq(MODEL), eq(TEST_PROMPT), eq(null)))
                .thenReturn(mockResponse);

        String result = aiService.generateContent(TEST_PROMPT);

        assertNotNull(result);
        assertEquals("AI generated response", result);
        
        verify(mockModels).generateContent(eq(MODEL), eq(TEST_PROMPT), eq(null));
    }

    @Test
    public void generateContent_exception() {
        when(mockModels.generateContent(eq(MODEL), eq(TEST_PROMPT), eq(null)))
                .thenThrow(new RuntimeException("API Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            aiService.generateContent(TEST_PROMPT);
        });
        
        assertTrue(exception.getMessage().contains("An error occurred while generating content"));
    }

    @Test
    public void refineIdea_success() {
        GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
        when(mockResponse.text()).thenReturn("Refined idea");

        when(mockModels.generateContent(eq(MODEL), any(String.class), eq(null)))
                .thenReturn(mockResponse);

        String result = aiService.refineIdea("Raw idea");

        assertNotNull(result);
        assertEquals("Refined idea", result);
        
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockModels).generateContent(eq(MODEL), promptCaptor.capture(), eq(null));
        
        String capturedPrompt = promptCaptor.getValue();
        assertTrue(capturedPrompt.contains("You are an expert idea refinement assistant"));
        assertTrue(capturedPrompt.contains("Raw idea"));
    }

    @Test
    public void combineIdeas_success() {
        GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
        when(mockResponse.text()).thenReturn("Combined idea");

        when(mockModels.generateContent(eq(MODEL), any(String.class), eq(null)))
                .thenReturn(mockResponse);

        String result = aiService.combineIdeas("Idea 1", "Idea 2");

        assertNotNull(result);
        assertEquals("Combined idea", result);
        
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockModels).generateContent(eq(MODEL), promptCaptor.capture(), eq(null));
        
        String capturedPrompt = promptCaptor.getValue();
        assertTrue(capturedPrompt.contains("Idea 1"));
        assertTrue(capturedPrompt.contains("Idea 2"));
    }

    @Test
    public void generateFromTemplate_success() {
        GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
        when(mockResponse.text()).thenReturn("Title: Generated Title\nDescription: Generated Description");

        when(mockModels.generateContent(eq(MODEL), any(String.class), eq(null)))
                .thenReturn(mockResponse);

        String result = aiService.generateFromTemplate("Test template");

        assertNotNull(result);
        assertEquals("Title: Generated Title\nDescription: Generated Description", result);
        
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockModels).generateContent(eq(MODEL), promptCaptor.capture(), eq(null));
        
        String capturedPrompt = promptCaptor.getValue();
        assertTrue(capturedPrompt.contains("Test template"));
        assertTrue(capturedPrompt.contains("You are a creative idea generator"));
    }

    @Test
    public void suggestRelatedIdea_success() {
        GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
        when(mockResponse.text()).thenReturn("Related idea with twist");

        when(mockModels.generateContent(eq(MODEL), any(String.class), eq(null)))
                .thenReturn(mockResponse);

        String result = aiService.suggestRelatedIdea("Original idea", "Make it eco-friendly");

        assertNotNull(result);
        assertEquals("Related idea with twist", result);
        
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockModels).generateContent(eq(MODEL), promptCaptor.capture(), eq(null));
        
        String capturedPrompt = promptCaptor.getValue();
        assertTrue(capturedPrompt.contains("Original idea"));
        assertTrue(capturedPrompt.contains("Make it eco-friendly"));
    }
}
