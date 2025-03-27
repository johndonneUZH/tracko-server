package ch.uzh.ifi.hase.soprafs24.controller;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ch.uzh.ifi.hase.soprafs24.service.IdeaService;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaRegister;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaUpdate;
import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;



@RestController
@RequestMapping("/projects/{projectId}/ideas")
public class IdeaController {
    
    private final IdeaService ideaService;

    IdeaController(IdeaService ideaService) {
        this.ideaService = ideaService;
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Idea> createIdea(
            @PathVariable String projectId, 
            @RequestBody IdeaRegister newIdea, 
            @RequestHeader("Authorization") String authHeader) {

        Idea savedIdea = ideaService.createIdea(projectId, newIdea, authHeader, new ArrayList<String>());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedIdea);
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> getIdeasByProject(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String authHeader) {

        var ideas = ideaService.getIdeasByProject(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.OK).body(ideas);
    }

    @GetMapping("/{ideaId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Idea> getIdeaById(
            @PathVariable String projectId,
            @PathVariable String ideaId,
            @RequestHeader("Authorization") String authHeader) {

        Idea idea = ideaService.getIdeaById(projectId, ideaId, authHeader);
        return ResponseEntity.status(HttpStatus.OK).body(idea);
    }

    @PutMapping("/{ideaId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Idea> updateIdea(
            @PathVariable String projectId,
            @PathVariable String ideaId,
            @RequestBody IdeaUpdate updatedIdea,
            @RequestHeader("Authorization") String authHeader) {

        Idea updatedIdeaObj = ideaService.updateIdea(projectId, ideaId, updatedIdea, authHeader);
        return ResponseEntity.status(HttpStatus.OK).body(updatedIdeaObj);
    }

    // Create sub-idea
    @PostMapping("/{ideaId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Idea> createSubIdea(
            @PathVariable String projectId,
            @PathVariable String ideaId,
            @RequestBody IdeaRegister newIdea,
            @RequestHeader("Authorization") String authHeader) {

        Idea savedIdea = ideaService.createSubIdea(projectId, ideaId, newIdea, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedIdea);
    }
    
}
