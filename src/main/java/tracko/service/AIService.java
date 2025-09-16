package tracko.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import tracko.config.AIConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "ai.enabled", havingValue = "true", matchIfMissing = false)
public class AIService {
    
    private final Logger log = LoggerFactory.getLogger(AIService.class);

    private final Client genAiClient;
    private final AIConfig aiConfig;
    
    public AIService(Client genAiClient, AIConfig aiConfig) {
        this.genAiClient = genAiClient;
        this.aiConfig = aiConfig;
    }
    
    public String generateContent(String prompt) {
        try {
            log.info("Sending request to Gemini API with model: {}", aiConfig.getModel());
            
            GenerateContentResponse response = genAiClient.models.generateContent(
                aiConfig.getModel(), 
                prompt, 
                null
            );
            
            return response.text();
        } catch (Exception ex) {
            log.error("Error calling Gemini API: {}", ex.getMessage());
            throw new RuntimeException("An error occurred while generating content: " + ex.getMessage());
        }
    }
    
    // Define specific methods for your use cases
    public String refineIdea(String ideaContent) {
        String prompt = "Refine this idea into a more innovative and detailed concept:\n" +
                       "Original idea: " + ideaContent + "\n" +
                        "Refined idea: make sure to return only the description of the idea, i.e. a cohesive text without bullet points or any other formatting.";
        return generateContent(prompt);
    }
    
    public String combineIdeas(String ideaOne, String ideaTwo) {
        String prompt = "Combine these two ideas into a new innovative concept:\nIdea 1: " + 
                       ideaOne + "\nIdea 2: " + ideaTwo
                       + "\nCombined idea: make sure to return only the description of the idea, i.e. a cohesive text without bullet points or any other formatting.";
        return generateContent(prompt);
    }
    
    public String generateFromTemplate(String template) {
        String prompt = "Transform my freehand brainstorm into an Idea Card with a title and short description.\n" 
                        + "The topic is: " + template + "\n\n"
                        + "Respond in exactly this format:\n"
                        + "Title: [Your generated title here]\n"
                        + "Description: [Your generated description here]";
        return generateContent(prompt);
    }
    
    public String suggestRelatedIdea(String originalIdea, String twist) {
        String prompt = "Suggest a new idea based on this original concept but with the following twist: " +
                       "Original idea: " + originalIdea + "\nTwist: " + twist
                       + "\nNew idea: make sure to return only the description of the idea, i.e. a cohesive text without bullet points or any other formatting.";
        return generateContent(prompt);
    }
}
