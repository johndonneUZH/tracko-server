package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import ch.uzh.ifi.hase.soprafs24.models.change.Change;
import ch.uzh.ifi.hase.soprafs24.models.change.ChangeRegister;
import ch.uzh.ifi.hase.soprafs24.repository.ChangeRepository;

@Service
@Transactional
public class ChangeService {
    private final ChangeRepository changeRepository;
    private final ProjectService projectService;
    private final UserService userService;

    public ChangeService(ChangeRepository changeRepository, ProjectService projectService, UserService userService) {
        this.userService = userService;
        this.changeRepository = changeRepository;
        this.projectService = projectService;
    }

    public List<Change> getChangesByProject(String projectId, String authHeader) {
        projectService.authenticateProject(projectId, authHeader);
        return changeRepository.findByProjectId(projectId);
    }

    public Change createChange(String projectId, ChangeRegister newChange, String authHeader) {
        projectService.authenticateProject(projectId, authHeader);
        String userId = userService.getUserIdByToken(authHeader);

        // Create a new Change entity from ChangeRegister
        Change change = new Change();
        change.setProjectId(projectId);
        change.setOwnerId(userId);
        change.setChangeType(newChange.getChangeType()); // Enum type
        change.setChangeDescription(newChange.getChangeDescription());
        change.setCreatedAt(LocalDateTime.now()); // Set creation timestamp

        return changeRepository.save(change);
    }

}
