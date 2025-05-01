package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs24.models.change.Change;
import ch.uzh.ifi.hase.soprafs24.models.change.ChangeRegister;
import ch.uzh.ifi.hase.soprafs24.service.ChangeService;
import ch.uzh.ifi.hase.soprafs24.service.ChangeService.Contributions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<List<Change>> getChangesById(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String authHeader) {

        var changes = changeService.getChangesByProject(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.OK).body(changes);
    }

    @PostMapping("")
    public ResponseEntity<Change> createChange(
            @PathVariable String projectId,
            @RequestBody ChangeRegister newChange,
            @RequestHeader("Authorization") String authHeader) {

        Change savedChange = changeService.createChange(projectId, newChange, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedChange);
    }
    
    @GetMapping("/daily-contributions")
    public ResponseEntity<List<ChangeService.DailyContribution>> getDailyContributions(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer days) {
        
        List<ChangeService.DailyContribution> contributions = 
            changeService.getDailyContributions(projectId, authHeader, days);
        return ResponseEntity.ok(contributions);
    }

    @GetMapping("/contributions")
    public ResponseEntity<Map<String, Long>> getContributionsByDate(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String authHeader) {
        List<Change> changes = changeService.getChangesByProject(projectId, authHeader);
        
        Map<String, Long> contributionsByDate = changes.stream()
            .collect(Collectors.groupingBy(
                change -> change.getCreatedAt().toLocalDate().toString(),
                Collectors.counting()
            ));
            
        return ResponseEntity.ok(contributionsByDate);
    }

    @GetMapping("/analytics")
    public ResponseEntity<List<Contributions>> getAnalytics(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "90") Integer days) {
        List<Contributions> analytics = changeService.getAnalytics(projectId, authHeader, days);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/{userId}")
    public ResponseEntity<List<Contributions>> getAnalyticsByUserId(
            @PathVariable String projectId,
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "180") Integer days) {
        List<Contributions> analytics = changeService.getAnalyticsByUserId(projectId, userId, authHeader, days);
        return ResponseEntity.ok(analytics);
    }
}
