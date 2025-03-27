package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ch.uzh.ifi.hase.soprafs24.service.IdeaService;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaRegister;
import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;

@RestController
@RequestMapping("/ideas")
public class IdeaController {
    
    private final IdeaService ideaService;

    IdeaController(IdeaService ideaService) {
        this.ideaService = ideaService;
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Idea> createIdea(@RequestBody IdeaRegister newIdea, @RequestHeader("Authorization") String authHeader) {
        Idea savedIdea = ideaService.createIdea(newIdea, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedIdea);
    }
}
