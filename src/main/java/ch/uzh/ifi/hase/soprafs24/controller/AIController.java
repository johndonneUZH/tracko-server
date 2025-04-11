// package ch.uzh.ifi.hase.soprafs24.controller;

// import ch.uzh.ifi.hase.soprafs24.models.ai.*;
// import ch.uzh.ifi.hase.soprafs24.service.AnthropicService;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/ai")
// public class AIController {
    
//     private final Logger log = LoggerFactory.getLogger(AIController.class);
//     private final AnthropicService anthropicService;
    
//     @Autowired
//     public AIController(AnthropicService anthropicService) {
//         this.anthropicService = anthropicService;
//     }
    
//     @PostMapping("/refine")
//     @ResponseStatus(HttpStatus.OK)
//     public ResponseEntity<AnthropicResponseDTO> refineIdea(@RequestBody IdeaRefinementRequestDTO request) {
//         log.info("POST /api/ai/refine - refining idea");
//         AnthropicResponseDTO response = anthropicService.refineIdea(request.getIdeaContent());
//         return ResponseEntity.ok(response);
//     }
    
//     @PostMapping("/combine")
//     @ResponseStatus(HttpStatus.OK)
//     public ResponseEntity<AnthropicResponseDTO> combineIdeas(@RequestBody IdeaCombinationRequestDTO request) {
//         log.info("POST /api/ai/combine - combining ideas");
//         AnthropicResponseDTO response = anthropicService.combineIdeas(request.getIdeaOne(), request.getIdeaTwo());
//         return ResponseEntity.ok(response);
//     }
    
//     @PostMapping("/template")
//     @ResponseStatus(HttpStatus.OK)
//     public ResponseEntity<AnthropicResponseDTO> generateFromTemplate(@RequestBody String template) {
//         log.info("POST /api/ai/template - generating from template");
//         AnthropicResponseDTO response = anthropicService.generateFromTemplate(template);
//         return ResponseEntity.ok(response);
//     }
    
//     @PostMapping("/twist")
//     @ResponseStatus(HttpStatus.OK)
//     public ResponseEntity<AnthropicResponseDTO> suggestWithTwist(@RequestBody IdeaTwistRequestDTO request) {
//         log.info("POST /api/ai/twist - suggesting idea with twist");
//         AnthropicResponseDTO response = anthropicService.suggestRelatedIdea(
//                 request.getOriginalIdea(), 
//                 request.getTwist());
//         return ResponseEntity.ok(response);
//     }
    
//     @PostMapping("/generate")
//     @ResponseStatus(HttpStatus.OK)
//     public ResponseEntity<AnthropicResponseDTO> generateContent(@RequestBody String prompt) {
//         log.info("POST /api/ai/generate - generating custom content");
//         AnthropicResponseDTO response = anthropicService.generateContent(prompt);
//         return ResponseEntity.ok(response);
//     }
// }