package tracko.service;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import tracko.auth.JwtUtil;
import tracko.repository.ProjectRepository;
import tracko.models.project.Project;

@Service
public class ProjectAuthorizationService {
    private final JwtUtil jwtUtil;  // Add this
    private final ProjectRepository projectRepository;

    public ProjectAuthorizationService(JwtUtil jwtUtil, ProjectRepository projectRepository) {
        this.jwtUtil = jwtUtil;
        this.projectRepository = projectRepository;
    }

    public Project authenticateProject(String projectId, String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(token);  // Get userId directly from token
        
        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }

        if (!project.get().getOwnerId().equals(userId) && !project.get().getProjectMembers().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a project member: " + userId);
        }

        return project.get();
    }
}