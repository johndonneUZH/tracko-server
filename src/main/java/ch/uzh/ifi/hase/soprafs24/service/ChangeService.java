package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.lang.Nullable;

import ch.uzh.ifi.hase.soprafs24.constant.ChangeType;
import ch.uzh.ifi.hase.soprafs24.models.change.Change;
import ch.uzh.ifi.hase.soprafs24.models.change.ChangeRegister;
import ch.uzh.ifi.hase.soprafs24.repository.ChangeRepository;

@Service
@Transactional
public class ChangeService {
    private final ChangeRepository changeRepository;
    private final UserService userService;
    private final ProjectAuthorizationService projectAuthorizationService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChangeService(ChangeRepository changeRepository, 
                        @Lazy ProjectService projectService, @Lazy UserService userService,
                        ProjectAuthorizationService projectAuthorizationService,
                        SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.projectAuthorizationService = projectAuthorizationService;
        this.userService = userService;
        this.changeRepository = changeRepository;
    }

    private Change buildChange(String projectId, String userId, ChangeRegister newChange) {
        Change change = new Change();
        change.setProjectId(projectId);
        change.setOwnerId(userId);
        change.setChangeType(newChange.getChangeType());
        change.setChangeDescription(newChange.getChangeType().getDescription());
        change.setCreatedAt(LocalDateTime.now());
        return change;
    }
    
    public Change createChange(String projectId, ChangeRegister newChange, String authHeader) {
        projectAuthorizationService.authenticateProject(projectId, authHeader);
        String userId = userService.getUserIdByToken(authHeader);
        Change change = buildChange(projectId, userId, newChange);
        sendNotificationProject(projectId, change);
        return changeRepository.save(change);
    }
    
    public Change createChangeUser(String userId, ChangeRegister newChange, String authHeader, @Nullable String friendId) {
        String requestingUserId = userService.getUserIdByToken(authHeader);
        Change change = buildChange(null, requestingUserId, newChange);
        
        // Conditionally notify the friend if provided
        if (friendId != null) {
            sendNotificationUser(friendId, change);
        }
        
        return changeRepository.save(change);
    }

    public void markChange(String id, ChangeType changeType, String authHeader, boolean isUserChange, @Nullable String friendId) {
        ChangeRegister changeRegister = new ChangeRegister();
        changeRegister.setChangeType(changeType);
        
        if (isUserChange) {
            createChangeUser(id, changeRegister, authHeader, friendId);
        } else {
            createChange(id, changeRegister, authHeader);
        }
    }

    private void sendNotificationProject(String projectId, Change change) {
            messagingTemplate.convertAndSend(
            "/topic/projects/" + projectId + "/changes",
            change
        );
    }

    private void sendNotificationUser(String userId, Change change) {
        System.out.println("Attempting to send notification to user: " + userId);
        messagingTemplate.convertAndSend(
            "/queue/user-" + userId + "-notifications",
            change
        );
    }

    public List<Change> getChangesByProject(String projectId, String authHeader) {
        projectAuthorizationService.authenticateProject(projectId, authHeader);
        return changeRepository.findByProjectId(projectId);
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

    public record Contributions(LocalDateTime date, 
                                Long addIdea, Long editIdea, Long closeIdea, 
                                Long addComment, Long deleteComment,
                                Long upvote, Long downvote,
                                Long settings) {}

    public List<Contributions> getAnalytics(String projectId, String authHeader, Integer days) {
        String userId = userService.getUserIdByToken(authHeader);
        LocalDate cutoffDate = LocalDate.now().minusDays(days); // Last 90 days
        
        // Fetch changes and filter by date
        List<Change> userChanges = changeRepository.findByProjectId(projectId, userId)
            .stream()
            .filter(change -> !change.getCreatedAt().toLocalDate().isBefore(cutoffDate))
            .collect(Collectors.toList());

        return buildAnalytics(userChanges);
    }

    public List<Contributions> getAnalyticsByUserId(String projectId, String userId, String authHeader, Integer days) {
        projectAuthorizationService.authenticateProject(projectId, authHeader);
        LocalDate cutoffDate = LocalDate.now().minusDays(days); // Last 90 days
        
        // Fetch changes and filter by date
        List<Change> userChanges = changeRepository.findByOwnerIdAndProjectId(userId, projectId)
            .stream()
            .filter(change -> !change.getCreatedAt().toLocalDate().isBefore(cutoffDate))
            .collect(Collectors.toList());

        return buildAnalytics(userChanges);
    }
    
    public List<Contributions> buildAnalytics(List<Change> userChanges) {
        // Group by day (truncate time portion)
        Map<LocalDate, List<Change>> groupedChanges = userChanges.stream()
            .collect(Collectors.groupingBy(change -> change.getCreatedAt().toLocalDate()));
    
        // Then map to Contributions
        return groupedChanges.entrySet().stream()
            .map(entry -> {
                LocalDate date = entry.getKey();
                List<Change> changes = entry.getValue();
    
                long add = changes.stream().filter(c -> c.getChangeType() == ChangeType.ADDED_IDEA).count();
                long edit = changes.stream().filter(c -> c.getChangeType() == ChangeType.MODIFIED_IDEA).count();
                long close = changes.stream().filter(c -> c.getChangeType() == ChangeType.CLOSED_IDEA).count();
                long upvote = changes.stream().filter(c -> c.getChangeType() == ChangeType.UPVOTE).count();
                long downvote = changes.stream().filter(c -> c.getChangeType() == ChangeType.DOWNVOTE).count();
                long addComment = changes.stream().filter(c -> c.getChangeType() == ChangeType.ADDED_COMMENT).count();
                long deleteComment = changes.stream().filter(c -> c.getChangeType() == ChangeType.DELETED_COMMENT).count();
    
                long settings = changes.stream().filter(c -> c.getChangeType() == ChangeType.CHANGED_PROJECT_SETTINGS).count();
                settings += changes.stream().filter(c -> c.getChangeType() == ChangeType.LEFT_PROJECT).count();
                settings += changes.stream().filter(c -> c.getChangeType() == ChangeType.ADDED_MEMBER).count();
    
                return new Contributions(date.atStartOfDay(), add, edit, close, addComment, deleteComment, upvote, downvote, settings);
            })
            .sorted((c1, c2) -> c1.date().compareTo(c2.date())) 
            .collect(Collectors.toList());
    }

    

}
