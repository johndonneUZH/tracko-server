package tracko.controller;

import tracko.service.AIService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@ConditionalOnProperty(name = "ai.enabled", havingValue = "true", matchIfMissing = false)
public class AIController {
    
    private final Logger log = LoggerFactory.getLogger(AIController.class);
    private final AIService aiService;
    
    public AIController(AIService aiService) {
        this.aiService = aiService;
    }
    
    @PostMapping("/refine")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> refineIdea(@RequestBody Map<String, String> request) {
        log.info("POST /api/ai/refine - refining idea");
        String ideaContent = request.get("ideaContent");
        String response = aiService.refineIdea(ideaContent);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/combine")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> combineIdeas(@RequestBody Map<String, String> request) {
        log.info("POST /api/ai/combine - combining ideas");
        String ideaOne = request.get("ideaOne");
        String ideaTwo = request.get("ideaTwo");
        String response = aiService.combineIdeas(ideaOne, ideaTwo);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/template")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> generateFromTemplate(@RequestBody Map<String, String> request) {
        log.info("POST /api/ai/template - generating from template");
        String template = request.get("ideaContent");
        String response = aiService.generateFromTemplate(template);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/twist")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> suggestWithTwist(@RequestBody Map<String, String> request) {
        log.info("POST /api/ai/twist - suggesting idea with twist");
        String originalIdea = request.get("ideaContent");
        String twist = request.get("ideaTwist");
        String response = aiService.suggestRelatedIdea(originalIdea, twist);
        return ResponseEntity.ok(response);
    }
}
