package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaRegister;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaUpdate;
import ch.uzh.ifi.hase.soprafs24.service.IdeaService;

import java.util.ArrayList;
import java.util.Map;

@Controller
public class IdeaWebSocketController {

    private final IdeaService ideaService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public IdeaWebSocketController(IdeaService ideaService, SimpMessagingTemplate messagingTemplate) {
        this.ideaService = ideaService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/ideas/{projectId}/create")
    public void handleIdeaCreation(
            @DestinationVariable String projectId,
            @DestinationVariable ArrayList<String> subIdeas,
            @Payload IdeaRegister ideaRegister,
            @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        String authHeader = (String) sessionAttributes.get("token");
        Idea createdIdea = ideaService.createIdea(projectId, ideaRegister, authHeader, subIdeas);
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/ideas", createdIdea);
    }

    @MessageMapping("/ideas/{projectId}/update/{ideaId}")
    public void handleIdeaUpdate(
            @DestinationVariable String projectId,
            @DestinationVariable String ideaId,
            @Payload IdeaUpdate ideaUpdate,
            @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        String authHeader = (String) sessionAttributes.get("token");
        Idea updatedIdea = ideaService.updateIdea(projectId, ideaId, ideaUpdate, authHeader);
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/ideas", updatedIdea);
    }

    @MessageMapping("/ideas/{projectId}/delete")
    public void handleIdeaDeletion(
            @DestinationVariable String projectId,
            @Payload String ideaId,
            @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        String authHeader = (String) sessionAttributes.get("token");
        ideaService.deleteIdea(projectId, ideaId, authHeader);
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/ideas", 
            Map.of("deletedId", ideaId));
    }
}