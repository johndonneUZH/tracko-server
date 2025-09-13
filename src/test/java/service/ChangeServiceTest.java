package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;

import config.MongoTestConfig;
import tracko.repository.ChangeRepository;
import tracko.constant.ChangeType;
import tracko.models.change.Change;
import tracko.models.change.ChangeRegister;
import tracko.models.project.Project;
import tracko.service.ChangeService;
import tracko.service.ProjectAuthorizationService;
import tracko.service.ProjectService;
import tracko.service.UserService;
import tracko.service.ChangeService.Contributions;
import tracko.service.ChangeService.DailyContribution;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class ChangeServiceTest {

    private ChangeService changeService;

    @MockBean
    private ChangeRepository changeRepository;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserService userService;

    @MockBean
    private ProjectAuthorizationService projectAuthorizationService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    private final String VALID_AUTH_HEADER = "Bearer valid-token";
    private final String PROJECT_ID = "project-123";
    private final String USER_ID = "user-123";
    private final String FRIEND_ID = "friend-456";

    @BeforeEach
    public void setup() {
        changeService = new ChangeService(changeRepository, projectService, userService, projectAuthorizationService, messagingTemplate);

        when(userService.getUserIdByToken(VALID_AUTH_HEADER)).thenReturn(USER_ID);
        
        Project project = new Project();
        project.setProjectId(PROJECT_ID);
        project.setOwnerId(USER_ID);
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER)).thenReturn(project);
    }

    @Test
    public void getChangesByProject_success() {
        Change change1 = createTestChange("change-1", ChangeType.ADDED_IDEA);
        Change change2 = createTestChange("change-2", ChangeType.CHANGED_PROJECT_SETTINGS);
        List<Change> changes = Arrays.asList(change1, change2);
        
        when(changeRepository.findByProjectId(PROJECT_ID)).thenReturn(changes);

        List<Change> result = changeService.getChangesByProject(PROJECT_ID, VALID_AUTH_HEADER);

        assertEquals(2, result.size());
        assertEquals("change-1", result.get(0).getChangeId());
        assertEquals("change-2", result.get(1).getChangeId());
        verify(projectAuthorizationService, times(1)).authenticateProject(PROJECT_ID, VALID_AUTH_HEADER);
    }

    @Test
    public void createChange_success() {
        ChangeRegister changeRegister = new ChangeRegister();
        changeRegister.setChangeType(ChangeType.ADDED_IDEA);
    
        when(changeRepository.save(any(Change.class))).thenAnswer(invocation -> {
            Change input = invocation.getArgument(0);
            input.setChangeId("generated-change-id");
            return input;
        });
    
        Change result = changeService.createChange(PROJECT_ID, changeRegister, VALID_AUTH_HEADER);
    
        assertNotNull(result);
        assertEquals("generated-change-id", result.getChangeId());
        assertEquals(ChangeType.ADDED_IDEA, result.getChangeType());
        assertEquals("Added an idea", result.getChangeDescription());
        assertEquals(PROJECT_ID, result.getProjectId());
        assertEquals(USER_ID, result.getOwnerId());
        assertNotNull(result.getCreatedAt());
    
        verify(projectAuthorizationService, times(1)).authenticateProject(PROJECT_ID, VALID_AUTH_HEADER);
        verify(changeRepository, times(1)).save(any(Change.class));
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/projects/" + PROJECT_ID + "/changes"),
            any(Change.class)
        );
    }
    
    @Test
    public void createChangeUser_success() {
        ChangeRegister changeRegister = new ChangeRegister();
        changeRegister.setChangeType(ChangeType.SENT_FRIEND_REQUEST);
        
        when(changeRepository.save(any(Change.class))).thenAnswer(invocation -> {
            Change input = invocation.getArgument(0);
            input.setChangeId("generated-change-id");
            return input;
        });
        
        Change result = changeService.createChangeUser(USER_ID, changeRegister, VALID_AUTH_HEADER, FRIEND_ID);
        
        assertNotNull(result);
        assertEquals("generated-change-id", result.getChangeId());
        assertEquals(ChangeType.SENT_FRIEND_REQUEST, result.getChangeType());
        assertEquals(USER_ID, result.getOwnerId());
        assertNotNull(result.getCreatedAt());
        
        verify(changeRepository, times(1)).save(any(Change.class));
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/queue/user-" + FRIEND_ID + "-notifications"),
            any(Change.class)
        );
    }
    
    @Test
    public void markChange_project() {
        ChangeType changeType = ChangeType.ADDED_IDEA;
        
        when(changeRepository.save(any(Change.class))).thenAnswer(invocation -> {
            Change input = invocation.getArgument(0);
            input.setChangeId("generated-change-id");
            return input;
        });
        
        changeService.markChange(PROJECT_ID, changeType, VALID_AUTH_HEADER, false, null);
        
        verify(projectAuthorizationService, times(1)).authenticateProject(PROJECT_ID, VALID_AUTH_HEADER);
        verify(changeRepository, times(1)).save(any(Change.class));
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/projects/" + PROJECT_ID + "/changes"),
            any(Change.class)
        );
    }
    
    @Test
    public void markChange_Comment() {
        ChangeType changeType = ChangeType.SENT_FRIEND_REQUEST;
        
        when(changeRepository.save(any(Change.class))).thenAnswer(invocation -> {
            Change input = invocation.getArgument(0);
            input.setChangeId("generated-change-id");
            return input;
        });
        
        changeService.markChange(USER_ID, changeType, VALID_AUTH_HEADER, true, FRIEND_ID);
        
        verify(changeRepository, times(1)).save(any(Change.class));
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/queue/user-" + FRIEND_ID + "-notifications"),
            any(Change.class)
        );
    }
    
    @Test
    public void deleteChangesByProjectId() {
        changeService.deleteChangesByProjectId(PROJECT_ID);
        
        verify(changeRepository, times(1)).deleteByProjectId(PROJECT_ID);
    }
    
    @Test
    public void getContributionsByDate_success() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1);
        
        Change change1 = createTestChange("change-1", ChangeType.ADDED_IDEA);
        change1.setCreatedAt(today);
        
        Change change2 = createTestChange("change-2", ChangeType.MODIFIED_IDEA);
        change2.setCreatedAt(today);
        
        Change change3 = createTestChange("change-3", ChangeType.UPVOTE);
        change3.setCreatedAt(yesterday);
        
        List<Change> changes = Arrays.asList(change1, change2, change3);
        
        when(changeRepository.findByProjectId(PROJECT_ID)).thenReturn(changes);
        
        Map<LocalDate, Long> result = changeService.getContributionsByDate(PROJECT_ID, VALID_AUTH_HEADER);
        
        assertEquals(2, result.size());
        assertEquals(2L, result.get(today.toLocalDate()));
        assertEquals(1L, result.get(yesterday.toLocalDate()));
    }
    
    @Test
    public void getDailyContributions_success() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1);
        LocalDateTime twoDaysAgo = today.minusDays(2);
        
        Change change1 = createTestChange("change-1", ChangeType.ADDED_IDEA);
        change1.setCreatedAt(today);
        
        Change change2 = createTestChange("change-2", ChangeType.MODIFIED_IDEA);
        change2.setCreatedAt(today);
        
        Change change3 = createTestChange("change-3", ChangeType.UPVOTE);
        change3.setCreatedAt(yesterday);
        
        Change change4 = createTestChange("change-4", ChangeType.DOWNVOTE);
        change4.setCreatedAt(twoDaysAgo);
        
        List<Change> changes = Arrays.asList(change1, change2, change3, change4);
        
        when(changeRepository.findByProjectId(PROJECT_ID)).thenReturn(changes);
        
        List<DailyContribution> result = changeService.getDailyContributions(PROJECT_ID, VALID_AUTH_HEADER, 2);
        
        assertEquals(2, result.size());
        assertEquals(today.toLocalDate(), result.get(1).date());
        assertEquals(2L, result.get(1).count());
        assertEquals(yesterday.toLocalDate(), result.get(0).date());
        assertEquals(1L, result.get(0).count());
    }
    
    @Test
    public void getAnalytics_success() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1);
        
        Change change1 = createTestChange("change-1", ChangeType.ADDED_IDEA);
        change1.setCreatedAt(today);
        
        Change change2 = createTestChange("change-2", ChangeType.UPVOTE);
        change2.setCreatedAt(today);
        
        Change change3 = createTestChange("change-3", ChangeType.ADDED_COMMENT);
        change3.setCreatedAt(yesterday);
        
        List<Change> changes = Arrays.asList(change1, change2, change3);
        
        when(changeRepository.findByProjectId(PROJECT_ID, USER_ID)).thenReturn(changes);
        
        List<Contributions> result = changeService.getAnalytics(PROJECT_ID, VALID_AUTH_HEADER, 7);
        
        assertEquals(2, result.size());
        
        Contributions todayContrib = result.stream()
            .filter(c -> c.date().toLocalDate().equals(today.toLocalDate()))
            .findFirst().orElse(null);
        assertNotNull(todayContrib);
        assertEquals(1L, todayContrib.addIdea());
        assertEquals(1L, todayContrib.upvote());
        assertEquals(0L, todayContrib.addComment());
        
        Contributions yesterdayContrib = result.stream()
            .filter(c -> c.date().toLocalDate().equals(yesterday.toLocalDate()))
            .findFirst().orElse(null);
        assertNotNull(yesterdayContrib);
        assertEquals(0L, yesterdayContrib.addIdea());
        assertEquals(0L, yesterdayContrib.upvote());
        assertEquals(1L, yesterdayContrib.addComment());
    }
    
    @Test
    public void getAnalyticsByUserId_success() {
        LocalDateTime today = LocalDateTime.now();
        
        Change change1 = createTestChange("change-1", ChangeType.MODIFIED_IDEA);
        change1.setCreatedAt(today);
        
        Change change2 = createTestChange("change-2", ChangeType.DOWNVOTE);
        change2.setCreatedAt(today);
        
        List<Change> changes = Arrays.asList(change1, change2);
        
        when(changeRepository.findByOwnerIdAndProjectId(USER_ID, PROJECT_ID)).thenReturn(changes);
        
        List<Contributions> result = changeService.getAnalyticsByUserId(PROJECT_ID, USER_ID, VALID_AUTH_HEADER, 7);
        
        assertEquals(1, result.size());
        
        Contributions todayContrib = result.get(0);
        assertEquals(today.toLocalDate().atStartOfDay(), todayContrib.date());
        assertEquals(0L, todayContrib.addIdea());
        assertEquals(1L, todayContrib.editIdea());
        assertEquals(0L, todayContrib.upvote());
        assertEquals(1L, todayContrib.downvote());
    }
    
    @Test
    public void buildAnalytics_success() {
        LocalDateTime today = LocalDateTime.now();
        
        Change change1 = createTestChange("change-1", ChangeType.ADDED_IDEA);
        change1.setCreatedAt(today);
        
        Change change2 = createTestChange("change-2", ChangeType.ADDED_MEMBER);
        change2.setCreatedAt(today);
        
        List<Change> changes = Arrays.asList(change1, change2);
        
        List<Contributions> result = changeService.buildAnalytics(changes);
        
        assertEquals(1, result.size());
        
        Contributions contribution = result.get(0);
        assertEquals(today.toLocalDate().atStartOfDay(), contribution.date());
        assertEquals(1L, contribution.addIdea());
        assertEquals(1L, contribution.settings());
    }

    private Change createTestChange(String changeId, ChangeType changeType) {
        Change change = new Change();
        change.setChangeId(changeId);
        change.setChangeType(changeType);
        change.setChangeDescription(changeType.getDescription());
        change.setProjectId(PROJECT_ID);
        change.setOwnerId(USER_ID);
        change.setCreatedAt(LocalDateTime.now());
        return change;
    }
}