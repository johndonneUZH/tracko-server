package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;

import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;
import ch.uzh.ifi.hase.soprafs24.constant.ChangeType;
import ch.uzh.ifi.hase.soprafs24.models.change.Change;
import ch.uzh.ifi.hase.soprafs24.models.change.ChangeRegister;
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.repository.ChangeRepository;

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

    @BeforeEach
    public void setup() {
        changeService = new ChangeService(changeRepository, projectService, userService, projectAuthorizationService, messagingTemplate);

        // Mock authentication
        when(userService.getUserIdByToken(VALID_AUTH_HEADER)).thenReturn(USER_ID);
        
        // Mock project authentication
        Project project = new Project();
        project.setProjectId(PROJECT_ID);
        project.setOwnerId(USER_ID);
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER)).thenReturn(project);
    }

    @Test
    public void getChangesByProject_success() {
        // given
        Change change1 = createTestChange("change-1", ChangeType.ADDED_IDEA);
        Change change2 = createTestChange("change-2", ChangeType.CHANGED_PROJECT_SETTINGS);
        List<Change> changes = Arrays.asList(change1, change2);
        
        when(changeRepository.findByProjectId(PROJECT_ID)).thenReturn(changes);

        // when
        List<Change> result = changeService.getChangesByProject(PROJECT_ID, VALID_AUTH_HEADER);

        // then
        assertEquals(2, result.size());
        assertEquals("change-1", result.get(0).getChangeId());
        assertEquals("change-2", result.get(1).getChangeId());
        verify(projectAuthorizationService, times(1)).authenticateProject(PROJECT_ID, VALID_AUTH_HEADER);
    }

    @Test
    public void createChange_success() {
        // given
        ChangeRegister changeRegister = new ChangeRegister();
        changeRegister.setChangeType(ChangeType.ADDED_IDEA);
    
        when(changeRepository.save(any(Change.class))).thenAnswer(invocation -> {
            Change input = invocation.getArgument(0);
            input.setChangeId("generated-change-id");  // mimic what DB might do
            return input;
        });
    
        // when
        Change result = changeService.createChange(PROJECT_ID, changeRegister, VALID_AUTH_HEADER);
    
        // then
        assertNotNull(result);
        assertEquals("generated-change-id", result.getChangeId());
        assertEquals(ChangeType.ADDED_IDEA, result.getChangeType());
        assertEquals("Added an idea", result.getChangeDescription());
        assertEquals(PROJECT_ID, result.getProjectId());
        assertEquals(USER_ID, result.getOwnerId());
        assertNotNull(result.getCreatedAt());
    
        verify(projectAuthorizationService, times(1)).authenticateProject(PROJECT_ID, VALID_AUTH_HEADER);
        verify(changeRepository, times(1)).save(any(Change.class));
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