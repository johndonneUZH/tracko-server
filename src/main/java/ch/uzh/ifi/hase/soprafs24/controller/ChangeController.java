package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs24.models.change.Change;
import ch.uzh.ifi.hase.soprafs24.models.change.ChangeRegister;
import ch.uzh.ifi.hase.soprafs24.service.ChangeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;




@RestController
@RequestMapping("/projects/{projectId}/changes")
public class ChangeController {
    
    private final ChangeService changeService;

    ChangeController(ChangeService changeService) {
        this.changeService = changeService;
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<Change>> getChangesById(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String authHeader) {

        var changes = changeService.getChangesByProject(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.OK).body(changes);
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Change> createChange(
            @PathVariable String projectId,
            @RequestBody ChangeRegister newChange,
            @RequestHeader("Authorization") String authHeader) {

        Change savedChange = changeService.createChange(projectId, newChange, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedChange);
    }
    
    
}
