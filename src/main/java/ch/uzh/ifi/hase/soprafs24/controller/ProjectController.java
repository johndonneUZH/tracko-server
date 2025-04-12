package ch.uzh.ifi.hase.soprafs24.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.models.project.ProjectRegister;
import ch.uzh.ifi.hase.soprafs24.models.project.ProjectUpdate;
import ch.uzh.ifi.hase.soprafs24.models.user.User;
import ch.uzh.ifi.hase.soprafs24.service.ProjectService;



@RestController
@RequestMapping("/projects")
public class ProjectController {
    
    private final ProjectService projectService;

    ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Project> createProject(@RequestBody ProjectRegister newProject, @RequestHeader("Authorization") String authHeader) {
        Project savedProject = projectService.createProject(newProject, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Project> getProject(@PathVariable String projectId, @RequestHeader("Authorization") String authHeader) {
        Project project = projectService.authenticateProject(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(project);
    }
    
    @GetMapping("/{projectId}/members")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<User>> getProjectMembers(@PathVariable String projectId, @RequestHeader("Authorization") String authHeader) {
        List<User> members = projectService.getProjectMembers(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(members);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Project> updateProject(@PathVariable String projectId, 
                                                 @RequestBody ProjectUpdate updatedProject, 
                                                 @RequestHeader("Authorization") String authHeader) {
        Project project = projectService.updateProject(projectId, updatedProject, authHeader);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(project);
    }
    
    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId, @RequestHeader("Authorization") String authHeader) {
        projectService.deleteProject(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
