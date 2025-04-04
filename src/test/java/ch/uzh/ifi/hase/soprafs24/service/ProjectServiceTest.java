package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

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

    private final String VALID_AUTH_HEADER = "Bearer valid-token";
    private final String PROJECT_ID = "project-123";
    private final String USER_ID = "user-123";
    private final String OTHER_USER_ID = "user-456";

    private Project testProject;
    private ProjectRegister testProjectRegister;
    private ProjectUpdate testProjectUpdate;

    @BeforeEach
    public void setup() {
        projectService = new ProjectService(projectRepository, jwtUtil, userService);

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
        when(projectRepository.findByOwnerId(USER_ID)).thenReturn(new ArrayList<>());
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // when
        Project createdProject = projectService.createProject(testProjectRegister, VALID_AUTH_HEADER);

        // then
        assertNotNull(createdProject);
        assertEquals(PROJECT_ID, createdProject.getProjectId());
        assertEquals("Test Project", createdProject.getProjectName());
        assertEquals("Test Description", createdProject.getProjectDescription());
        assertEquals(USER_ID, createdProject.getOwnerId());
    }

    @Test
    public void createProject_duplicateName_conflict() {
        // given
        when(projectRepository.findByOwnerId(USER_ID)).thenReturn(Arrays.asList(testProject));

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
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));

        // when
        Project authenticatedProject = projectService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER);

        // then
        assertNotNull(authenticatedProject);
        assertEquals(PROJECT_ID, authenticatedProject.getProjectId());
        assertEquals("Test Project", authenticatedProject.getProjectName());
    }

    @Test
    public void authenticateProject_notFound() {
        // given
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

        // when/then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Project not found", exception.getReason());
    }

    @Test
    public void authenticateProject_notMember_forbidden() {
        // given
        Project project = new Project();
        project.setProjectId(PROJECT_ID);
        project.setOwnerId(OTHER_USER_ID);
        project.setProjectMembers(new ArrayList<>());

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

        // when/then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("You are not a member of this project", exception.getReason());
    }

    @Test
    public void authenticateProject_member_success() {
        // given
        Project project = new Project();
        project.setProjectId(PROJECT_ID);
        project.setOwnerId(OTHER_USER_ID);
        project.setProjectMembers(Arrays.asList(USER_ID));

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

        // when
        Project authenticatedProject = projectService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER);

        // then
        assertNotNull(authenticatedProject);
        assertEquals(PROJECT_ID, authenticatedProject.getProjectId());
    }

    @Test
    public void updateProject_success() {
        // given
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Project updatedProject = projectService.updateProject(PROJECT_ID, testProjectUpdate, VALID_AUTH_HEADER);

        // then
        assertNotNull(updatedProject);
        assertEquals("Updated Project", updatedProject.getProjectName());
        assertEquals("Updated Description", updatedProject.getProjectDescription());
        assertTrue(updatedProject.getProjectMembers().contains(OTHER_USER_ID));
    }

    @Test
    public void updateProject_notOwner_forbidden() {
        // given
        Project project = new Project();
        project.setProjectId(PROJECT_ID);
        project.setOwnerId(OTHER_USER_ID);
        project.setProjectMembers(Arrays.asList(USER_ID));

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

        // when/then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> projectService.updateProject(PROJECT_ID, testProjectUpdate, VALID_AUTH_HEADER)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("You are not the owner of this project", exception.getReason());
    }

    @Test
    public void updateProject_memberManagement() {
        // given
        testProject.setProjectMembers(Arrays.asList("user-111", "user-222"));
        
        ProjectUpdate updateWithMembers = new ProjectUpdate();
        updateWithMembers.setProjectName("Updated Project");
        updateWithMembers.setProjectDescription("Updated Description");
        updateWithMembers.setMembersToAdd(Arrays.asList("user-333", "user-444"));
        updateWithMembers.setMembersToRemove(Arrays.asList("user-111"));
        
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Project updatedProject = projectService.updateProject(PROJECT_ID, updateWithMembers, VALID_AUTH_HEADER);

        // then
        assertNotNull(updatedProject);
        assertEquals(3, updatedProject.getProjectMembers().size());
        assertTrue(updatedProject.getProjectMembers().contains("user-222"));
        assertTrue(updatedProject.getProjectMembers().contains("user-333"));
        assertTrue(updatedProject.getProjectMembers().contains("user-444"));
        assertTrue(!updatedProject.getProjectMembers().contains("user-111"));
    }
}