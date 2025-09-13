package controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import models.idea.Idea;
import models.idea.IdeaRegister;
import models.idea.IdeaUpdate;
import service.IdeaService;



@RestController
@RequestMapping("/projects/{projectId}/ideas")
public class IdeaController {
    
    private final IdeaService ideaService;

    IdeaController(IdeaService ideaService) {
        this.ideaService = ideaService;
    }

    @PostMapping("")
    public ResponseEntity<Idea> createIdea(
            @PathVariable String projectId, 
            @RequestBody IdeaRegister newIdea, 
            @RequestHeader("Authorization") String authHeader) {

        Idea savedIdea = ideaService.createIdea(projectId, newIdea, authHeader, new ArrayList<String>());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedIdea);
    }

    @GetMapping("")
    public ResponseEntity<List<Idea>> getIdeasByProject(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String authHeader) {

        List<Idea> ideas = ideaService.getIdeasByProject(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.OK).body(ideas);
    }

    @GetMapping("/{ideaId}")
    public ResponseEntity<Idea> getIdeaById(
            @PathVariable String projectId,
            @PathVariable String ideaId,
            @RequestHeader("Authorization") String authHeader) {

        Idea idea = ideaService.getIdeaById(projectId, ideaId, authHeader);
        return ResponseEntity.status(HttpStatus.OK).body(idea);
    }

    @PutMapping("/{ideaId}")
    public ResponseEntity<Idea> updateIdea(
            @PathVariable String projectId,
            @PathVariable String ideaId,
            @RequestBody IdeaUpdate updatedIdea,
            @RequestHeader("Authorization") String authHeader) {

        Idea updatedIdeaObj = ideaService.updateIdea(projectId, ideaId, updatedIdea, authHeader);
        return ResponseEntity.status(HttpStatus.OK).body(updatedIdeaObj);
    }

    @DeleteMapping("/{ideaId}")
    public ResponseEntity<Void> deleteIdea(
            @PathVariable String projectId,
            @PathVariable String ideaId,
            @RequestHeader("Authorization") String authHeader) {

        ideaService.deleteIdea(projectId, ideaId, authHeader);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
    
}
