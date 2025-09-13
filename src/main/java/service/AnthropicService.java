package service;

import config.AnthropicConfig;
import exceptions.AnthropicApiException;
import exceptions.AnthropicRateLimitException;
import exceptions.AnthropicTimeoutException;
import models.ai.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@ConditionalOnProperty(name = "anthropic.enabled", havingValue = "true", matchIfMissing = false)
public class AnthropicService {
    
    private final Logger log = LoggerFactory.getLogger(AnthropicService.class);

    private final RestTemplate restTemplate;
    private final AnthropicConfig anthropicConfig;
    private final AnthropicRateLimiterService rateLimiterService;
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    
    public AnthropicService(RestTemplate restTemplate, 
                           AnthropicConfig anthropicConfig,
                           AnthropicRateLimiterService rateLimiterService) {
        this.restTemplate = restTemplate;
        this.anthropicConfig = anthropicConfig;
        this.rateLimiterService = rateLimiterService;
    }
    
    public AnthropicResponseDTO generateContent(String prompt) {
        // Check rate limit before proceeding
        if (!rateLimiterService.acquirePermit()) {
            throw new RuntimeException("API rate limit exceeded. Please try again later.");
        }
        
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicConfig.getApiKey());
        headers.set("anthropic-version", "2023-06-01");
        headers.set("content-type", "application/json");
        
        // Prepare message content
        ContentDTO contentItem = new ContentDTO();
        contentItem.setType("text");
        contentItem.setText(prompt);
        
        MessageDTO message = new MessageDTO();
        message.setRole("user");
        message.setContent(List.of(contentItem));
        
        // Prepare request body
        AnthropicRequestDTO request = new AnthropicRequestDTO();
        request.setModel(anthropicConfig.getModel());
        request.setMaxTokens(anthropicConfig.getMaxTokens());
        request.setTemperature(anthropicConfig.getTemperature());
        request.setMessages(List.of(message));
        
        // Make API call
        HttpEntity<AnthropicRequestDTO> entity = new HttpEntity<>(request, headers);

        log.info("Sending request to Anthropic API: {}", entity);
        
        try {
            return restTemplate.postForObject(API_URL, entity, AnthropicResponseDTO.class);
        } catch (HttpClientErrorException.TooManyRequests ex) {
            log.error("Anthropic API rate limit exceeded: {}", ex.getMessage());
            throw new AnthropicRateLimitException("The AI service is currently experiencing high demand. Please try again later.");
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden ex) {
            log.error("Anthropic API authentication error: {}", ex.getMessage());
            throw new AnthropicApiException("Authentication error with AI service", HttpStatus.UNAUTHORIZED, "AUTH_ERROR");
        } catch (ResourceAccessException ex) {
            log.error("Anthropic API timeout: {}", ex.getMessage());
            throw new AnthropicTimeoutException("The AI service is taking too long to respond. Please try again later.");
        } catch (HttpServerErrorException ex) {
            log.error("Anthropic API server error: {}", ex.getMessage());
            throw new AnthropicApiException("The AI service encountered an error", HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_ERROR");
        } catch (Exception ex) {
            log.error("Unexpected error calling Anthropic API: {}", ex.getMessage());
            throw new AnthropicApiException("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_ERROR");
        }
    }
    
    // Define specific methods for your use cases
    public AnthropicResponseDTO refineIdea(String ideaContent) {
        String prompt = "Refine this idea into a more innovative and detailed concept:\n" +
                       "Original idea: " + ideaContent + "\n" +
                        "Refined idea: make sure to return only the description of the idea, i.e. a cohesive text without bullet points or any other formatting.";
        return generateContent(prompt);
    }
    
    public AnthropicResponseDTO combineIdeas(String ideaOne, String ideaTwo) {
        String prompt = "Combine these two ideas into a new innovative concept:\nIdea 1: " + 
                       ideaOne + "\nIdea 2: " + ideaTwo
                       + "\nCombined idea: make sure to return only the description of the idea, i.e. a cohesive text without bullet points or any other formatting.";
        return generateContent(prompt);
    }
    
    public AnthropicResponseDTO generateFromTemplate(String template) {
        String prompt = "Transform my freehand brainstorm into an Idea Card with a title and short description.\n" 
                        + "The topic is: " + template + "\n\n"
                        + "Respond in exactly this format:\n"
                        + "Title: [Your generated title here]\n"
                        + "Description: [Your generated description here]";
        return generateContent(prompt);
    }
    public AnthropicResponseDTO suggestRelatedIdea(String originalIdea, String twist) {
        String prompt = "Suggest a new idea based on this original concept but with the following twist: " +
                       "Original idea: " + originalIdea + "\nTwist: " + twist
                       + "\nNew idea: make sure to return only the description of the idea, i.e. a cohesive text without bullet points or any other formatting.";
        return generateContent(prompt);
    }
}