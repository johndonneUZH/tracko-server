package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ch.uzh.ifi.hase.soprafs24.models.change.Change;
import ch.uzh.ifi.hase.soprafs24.models.change.ChangeRegister;
import ch.uzh.ifi.hase.soprafs24.repository.ChangeRepository;

@Service
@Transactional
public class ChangeService {
    private final ChangeRepository changeRepository;
    private final UserService userService;
    private final ProjectAuthorizationService projectAuthorizationService;

    public ChangeService(ChangeRepository changeRepository, 
                        @Lazy ProjectService projectService, UserService userService,
                        ProjectAuthorizationService projectAuthorizationService) {
        this.projectAuthorizationService = projectAuthorizationService;
        this.userService = userService;
        this.changeRepository = changeRepository;
    }

    public List<Change> getChangesByProject(String projectId, String authHeader) {
        projectAuthorizationService.authenticateProject(projectId, authHeader);
        return changeRepository.findByProjectId(projectId);
    }

    public Change createChange(String projectId, ChangeRegister newChange, String authHeader) {
        projectAuthorizationService.authenticateProject(projectId, authHeader);
        String userId = userService.getUserIdByToken(authHeader);

        // Create a new Change entity from ChangeRegister
        Change change = new Change();
        change.setProjectId(projectId);
        change.setOwnerId(userId);
        change.setChangeType(newChange.getChangeType()); // Enum type
        change.setChangeDescription(newChange.getChangeType().getDescription());
        change.setCreatedAt(LocalDateTime.now()); // Set creation timestamp

        return changeRepository.save(change);
    }

    public void deleteChangesByProjectId(String projectId) {
        changeRepository.deleteByProjectId(projectId);
    }

    public Map<LocalDate, Long> getContributionsByDate(String projectId, String authHeader) {
        List<Change> changes = getChangesByProject(projectId, authHeader);
        return changes.stream()
            .collect(Collectors.groupingBy(
                change -> change.getCreatedAt().toLocalDate(),
                Collectors.counting()
            ));
    }

    public record DailyContribution(LocalDate date, Long count) {}
    
    public List<DailyContribution> getDailyContributions(String projectId, String authHeader, Integer days) {
        List<Change> changes = getChangesByProject(projectId, authHeader);
        
        return changes.stream()
            .filter(change -> days == null || 
                   change.getCreatedAt().isAfter(LocalDateTime.now().minusDays(days)))
            .collect(Collectors.groupingBy(
                change -> change.getCreatedAt().toLocalDate(),
                Collectors.counting()
            ))
            .entrySet().stream()
            .map(entry -> new DailyContribution(entry.getKey(), entry.getValue()))
            .sorted((a, b) -> a.date().compareTo(b.date()))
            .collect(Collectors.toList());
    }
}
