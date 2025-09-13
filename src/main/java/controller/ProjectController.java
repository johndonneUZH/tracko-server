package controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import models.ai.ContentDTO;
import models.messages.Message;
import models.messages.MessageRegister;
import models.project.Project;
import models.project.ProjectRegister;
import models.project.ProjectUpdate;
import models.user.User;
import service.ProjectAuthorizationService;
import service.ProjectService;



@RestController
@RequestMapping("/projects")
public class ProjectController {
    
    private final ProjectAuthorizationService projectAuthorizationService;
    private final ProjectService projectService;

    ProjectController(ProjectAuthorizationService projectAuthorizationService, ProjectService projectService) {
        this.projectService = projectService;
        this.projectAuthorizationService = projectAuthorizationService;
    }

    @PostMapping("")
    public ResponseEntity<Project> createProject(@RequestBody ProjectRegister newProject, @RequestHeader("Authorization") String authHeader) {
        Project savedProject = projectService.createProject(newProject, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProject(@PathVariable String projectId, @RequestHeader("Authorization") String authHeader) {
        Project project = projectAuthorizationService.authenticateProject(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(project);
    }
    
    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<User>> getProjectMembers(@PathVariable String projectId, @RequestHeader("Authorization") String authHeader) {
        List<User> members = projectService.getProjectMembers(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(members);
    }

    @PutMapping("/{projectId}/members")    
    public ResponseEntity<List<User>> makeUserLeaveFromProject(@PathVariable String projectId, @RequestHeader("Authorization") String authHeader) {
        projectService.makeUserLeaveFromProject(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    @PutMapping("/{projectId}")    
    public ResponseEntity<Project> updateProject(@PathVariable String projectId, 
                                                 @RequestBody ProjectUpdate updatedProject, 
                                                 @RequestHeader("Authorization") String authHeader) {
        Project project = projectService.updateProject(projectId, updatedProject, authHeader);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(project);
    }
    
    @DeleteMapping("/{projectId}")    
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId, @RequestHeader("Authorization") String authHeader) {
        projectService.deleteProject(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{projectId}/changes")    
    public ResponseEntity<Void> deleteProjectChanges(@PathVariable String projectId, @RequestHeader("Authorization") String authHeader) {
        projectService.deleteProjectChanges(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{projectId}/report")
    public ResponseEntity<ContentDTO> generateReport(@PathVariable String projectId, @RequestHeader("Authorization") String authHeader) {
        ContentDTO project = projectService.generateReport(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(project);
    }   
    

    @PostMapping("/{projectId}/messages")    
    public ResponseEntity<Message> createMessage(  @PathVariable String projectId, 
                                                @RequestHeader("Authorization") String authHeader, 
                                                @RequestBody MessageRegister message) {
        Message msg = projectService.sendChatMessage(projectId, authHeader, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(msg);
    }

    @GetMapping("/{projectId}/messages")    
    public ResponseEntity<List<Message>> getMessages(@PathVariable String projectId, @RequestHeader("Authorization") String authHeader) {
        List<Message> messages = projectService.getMessages(projectId, authHeader);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(messages);
    }
}
