package ch.uzh.ifi.hase.soprafs24.controller;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaRegister;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaUpdate;
import ch.uzh.ifi.hase.soprafs24.service.IdeaService;

@Controller
public class IdeaWebSocketController {

    private final IdeaService ideaService;

    @Autowired
    public IdeaWebSocketController(IdeaService ideaService) {
        this.ideaService = ideaService;
    }

    @MessageMapping("/ideas/{projectId}/create")
    public void handleIdeaCreation(@DestinationVariable String projectId, 
                                    @DestinationVariable ArrayList<String> subIdeas,
                                 IdeaRegister ideaRegister,
                                 @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        String authHeader = (String) sessionAttributes.get("token");
        ideaService.createIdea(projectId, ideaRegister, authHeader, subIdeas);
    }

    @MessageMapping("/ideas/{projectId}/update")
    public void handleIdeaUpdate(@DestinationVariable String projectId, 
                                @DestinationVariable String ideaId,
                               IdeaUpdate ideaUpdate,
                               @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        String authHeader = (String) sessionAttributes.get("token");
        ideaService.updateIdea(projectId, ideaId, ideaUpdate, authHeader);
    }

    @MessageMapping("/ideas/{projectId}/delete")
    public void handleIdeaDeletion(@DestinationVariable String projectId, 
                                 @Payload String ideaId,
                                 @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        String authHeader = (String) sessionAttributes.get("token");
        ideaService.deleteIdea(projectId, ideaId, authHeader);
    }
}