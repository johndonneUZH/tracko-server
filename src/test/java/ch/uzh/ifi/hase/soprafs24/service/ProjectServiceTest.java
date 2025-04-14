package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.models.project.ProjectRegister;
import ch.uzh.ifi.hase.soprafs24.models.project.ProjectUpdate;
import ch.uzh.ifi.hase.soprafs24.repository.IdeaRepository;
import ch.uzh.ifi.hase.soprafs24.repository.ProjectRepository;

@SpringBootTest
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class ProjectServiceTest {

    private ProjectService projectService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private ChangeService changeService;

    @MockBean
    private IdeaRepository ideaRepository;

    @MockBean
    private ProjectAuthorizationService projectAuthorizationService;

    private final String VALID_AUTH_HEADER = "Bearer valid-token";
    private final String PROJECT_ID = "project-123";
    private final String USER_ID = "user-123";
    private final String OTHER_USER_ID = "user-456";

    private Project testProject;
    private ProjectRegister testProjectRegister;
    private ProjectUpdate testProjectUpdate;
    
    @BeforeEach
    public void setup() {
        projectService = new ProjectService(projectRepository, jwtUtil, 
                                          userService, changeService,
                                          projectAuthorizationService, ideaRepository);

        // Mock the authentication
        when(userService.getUserIdByToken(VALID_AUTH_HEADER)).thenReturn(USER_ID);

        // Create test project
        testProject = new Project();
        testProject.setProjectId(PROJECT_ID);
        testProject.setProjectName("Test Project");
        testProject.setProjectDescription("Test Description");
        testProject.setOwnerId(USER_ID);
        testProject.setProjectMembers(new ArrayList<>());
        testProject.setCreatedAt(LocalDateTime.now());
        testProject.setUpdatedAt(LocalDateTime.now());

        // Create test project registration
        testProjectRegister = new ProjectRegister();
        testProjectRegister.setProjectName("Test Project");
        testProjectRegister.setProjectDescription("Test Description");

        // Create test project update
        testProjectUpdate = new ProjectUpdate();
        testProjectUpdate.setProjectName("Updated Project");
        testProjectUpdate.setProjectDescription("Updated Description");
        testProjectUpdate.setMembersToAdd(Arrays.asList(OTHER_USER_ID));
        testProjectUpdate.setMembersToRemove(new ArrayList<>());
    }

    @Test
    public void createProject_success() {
        // given
        when(projectRepository.findByOwnerIdAndProjectName(USER_ID, testProjectRegister.getProjectName()))
            .thenReturn(null);
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // when
        Project createdProject = projectService.createProject(testProjectRegister, VALID_AUTH_HEADER);

        // then
        assertNotNull(createdProject);
        assertEquals(PROJECT_ID, createdProject.getProjectId());
        assertEquals("Test Project", createdProject.getProjectName());
        assertEquals("Test Description", createdProject.getProjectDescription());
        assertEquals(USER_ID, createdProject.getOwnerId());
        
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    public void createProject_duplicateName_conflict() {
        // given
        when(projectRepository.findByOwnerIdAndProjectName(USER_ID, testProjectRegister.getProjectName()))
            .thenReturn(testProject);

        // when/then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.createProject(testProjectRegister, VALID_AUTH_HEADER)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Project with this name already exists", exception.getReason());
    }

    @Test
    public void authenticateProject_success() {
        // given
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
            .thenReturn(testProject);

        // when
        Project authenticatedProject = projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER);

        // then
        assertNotNull(authenticatedProject);
        assertEquals(PROJECT_ID, authenticatedProject.getProjectId());
    }

    @Test
    public void authenticateProject_notFound() {
        // given
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        // when/then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Project not found", exception.getReason());
    }

    @Test
    public void authenticateProject_notMember_forbidden() {
        // given
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project"));

        // when/then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("You are not a member of this project", exception.getReason());
    }

    @Test
    public void updateProject_success() {
        // given
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
            .thenReturn(testProject);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Project updatedProject = projectService.updateProject(PROJECT_ID, testProjectUpdate, VALID_AUTH_HEADER);

        // then
        assertNotNull(updatedProject);
        assertEquals("Updated Project", updatedProject.getProjectName());
        assertEquals("Updated Description", updatedProject.getProjectDescription());
        assertTrue(updatedProject.getProjectMembers().contains(OTHER_USER_ID));
        
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    public void updateProject_notOwner_forbidden() {
        // given
        testProject.setOwnerId(OTHER_USER_ID);
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
            .thenReturn(testProject);

        // when/then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.updateProject(PROJECT_ID, testProjectUpdate, VALID_AUTH_HEADER)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("You are not the owner of this project", exception.getReason());
        
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    public void updateProject_memberManagement() {
        // given
        testProject.setProjectMembers(new ArrayList<>(Arrays.asList("user-111", "user-222")));
        
        ProjectUpdate updateWithMembers = new ProjectUpdate();
        updateWithMembers.setProjectName("Updated Project");
        updateWithMembers.setProjectDescription("Updated Description");
        updateWithMembers.setMembersToAdd(Arrays.asList("user-333", "user-444"));
        updateWithMembers.setMembersToRemove(Arrays.asList("user-111"));
        
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
            .thenReturn(testProject);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Project updatedProject = projectService.updateProject(PROJECT_ID, updateWithMembers, VALID_AUTH_HEADER);

        // then
        assertNotNull(updatedProject);
        assertEquals(3, updatedProject.getProjectMembers().size());
        assertTrue(updatedProject.getProjectMembers().contains("user-222"));
        assertTrue(updatedProject.getProjectMembers().contains("user-333"));
        assertTrue(updatedProject.getProjectMembers().contains("user-444"));
        assertFalse(updatedProject.getProjectMembers().contains("user-111"));
        
        verify(projectRepository, times(1)).save(any(Project.class));
    }
}