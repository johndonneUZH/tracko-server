package ch.uzh.ifi.hase.soprafs24.service;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.repository.ProjectRepository;

@Service
public class ProjectAuthorizationService {
    private final UserService userService;
    private final ProjectRepository projectRepository;

    public ProjectAuthorizationService(UserService userService, ProjectRepository projectRepository) {
        this.userService = userService;
        this.projectRepository = projectRepository;
    }

    public Project authenticateProject(String projectId, String authHeader) {
        String userId = userService.getUserIdByToken(authHeader);
        Optional<Project> project = projectRepository.findById(projectId);

        if (project.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }

        if (!project.get().getOwnerId().equals(userId) && !project.get().getProjectMembers().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project");
        }

        return project.get();
    }
}