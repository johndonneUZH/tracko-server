package ch.uzh.ifi.hase.soprafs24.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;
import ch.uzh.ifi.hase.soprafs24.constant.IdeaStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaRegister;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaUpdate;
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.models.project.ProjectRegister;
import ch.uzh.ifi.hase.soprafs24.models.user.User;
import ch.uzh.ifi.hase.soprafs24.models.user.UserRegister;
import ch.uzh.ifi.hase.soprafs24.repository.IdeaRepository;
import ch.uzh.ifi.hase.soprafs24.repository.ProjectRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.service.IdeaService;
import ch.uzh.ifi.hase.soprafs24.service.ProjectService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class IdeaIntegrationTest {

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IdeaService ideaService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private String userId;
    private String projectId;
    private String authHeader;

    @BeforeEach
    public void setup() {
        // Clear databases
        ideaRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        // Register a test user
        UserRegister userRegister = new UserRegister();
        userRegister.setName("Test User");
        userRegister.setUsername("testuser");
        userRegister.setEmail("test@example.com");
        userRegister.setPassword("password");

        User createdUser = userService.createUser(userRegister);
        userId = createdUser.getId();

        // Create auth header
        String token = jwtUtil.generateToken(userId);
        authHeader = "Bearer " + token;

        // Create a project
        ProjectRegister projectRegister = new ProjectRegister();
        projectRegister.setProjectName("Test Project");
        projectRegister.setProjectDescription("This is a test project");

        Project createdProject = projectService.createProject(projectRegister, authHeader);
        projectId = createdProject.getProjectId();
    }

    @Test
    public void testCreateAndRetrieveIdea() {
        // Create an idea
        IdeaRegister ideaRegister = new IdeaRegister();
        ideaRegister.setIdeaName("Test Idea");
        ideaRegister.setIdeaDescription("This is a test idea");

        Idea createdIdea = ideaService.createIdea(projectId, ideaRegister, authHeader, new ArrayList<>());
        assertNotNull(createdIdea);
        assertNotNull(createdIdea.getIdeaId());
        assertEquals("Test Idea", createdIdea.getIdeaName());
        assertEquals("This is a test idea", createdIdea.getIdeaDescription());
        assertEquals(projectId, createdIdea.getProjectId());
        assertEquals(userId, createdIdea.getOwnerId());
        assertEquals(IdeaStatus.OPEN, createdIdea.getIdeaStatus());
        assertEquals(0L, createdIdea.getUpVotes());
        assertEquals(0L, createdIdea.getDownVotes());
        assertTrue(createdIdea.getSubIdeas().isEmpty());

        // Retrieve the idea and verify
        Idea retrievedIdea = ideaService.getIdeaById(projectId, createdIdea.getIdeaId(), authHeader);
        assertEquals(createdIdea.getIdeaId(), retrievedIdea.getIdeaId());
        assertEquals(createdIdea.getIdeaName(), retrievedIdea.getIdeaName());
        assertEquals(createdIdea.getIdeaDescription(), retrievedIdea.getIdeaDescription());
    }

    @Test
    public void testUpdateIdea() {
        // Create an idea
        IdeaRegister ideaRegister = new IdeaRegister();
        ideaRegister.setIdeaName("Original Idea");
        ideaRegister.setIdeaDescription("Original Description");

        Idea createdIdea = ideaService.createIdea(projectId, ideaRegister, authHeader, new ArrayList<>());
        
        // Update the idea
        IdeaUpdate ideaUpdate = new IdeaUpdate();
        ideaUpdate.setIdeaName("Updated Idea");
        ideaUpdate.setIdeaDescription("Updated Description");
        ideaUpdate.setIdeaStatus(IdeaStatus.CLOSED);
        ideaUpdate.setUpVotes(5L);
        ideaUpdate.setDownVotes(2L);
        ideaUpdate.setSubIdeas(new ArrayList<>());

        Idea updatedIdea = ideaService.updateIdea(projectId, createdIdea.getIdeaId(), ideaUpdate, authHeader);
        
        // Verify updates
        assertEquals("Updated Idea", updatedIdea.getIdeaName());
        assertEquals("Updated Description", updatedIdea.getIdeaDescription());
        assertEquals(IdeaStatus.CLOSED, updatedIdea.getIdeaStatus());
        assertEquals(5L, updatedIdea.getUpVotes());
        assertEquals(2L, updatedIdea.getDownVotes());
    }

    @Test
    public void testCreateSubIdea() {
        // Create a parent idea
        IdeaRegister parentIdeaRegister = new IdeaRegister();
        parentIdeaRegister.setIdeaName("Parent Idea");
        parentIdeaRegister.setIdeaDescription("Parent Description");

        Idea parentIdea = ideaService.createIdea(projectId, parentIdeaRegister, authHeader, new ArrayList<>());
        
        // Create a sub-idea
        IdeaRegister subIdeaRegister = new IdeaRegister();
        subIdeaRegister.setIdeaName("Sub Idea");
        subIdeaRegister.setIdeaDescription("Sub Description");

        Idea subIdea = ideaService.createSubIdea(projectId, parentIdea.getIdeaId(), subIdeaRegister, authHeader);
        
        // Verify sub-idea creation
        assertNotNull(subIdea);
        assertEquals("Sub Idea", subIdea.getIdeaName());
        
        // Verify parent idea has the sub-idea
        Idea updatedParentIdea = ideaService.getIdeaById(projectId, parentIdea.getIdeaId(), authHeader);
        assertEquals(1, updatedParentIdea.getSubIdeas().size());
        assertEquals(subIdea.getIdeaId(), updatedParentIdea.getSubIdeas().get(0));
    }
    
    @Test
    public void testUnauthorizedAccess() {
        // Create a second user and project
        UserRegister secondUserRegister = new UserRegister();
        secondUserRegister.setName("Second User");
        secondUserRegister.setUsername("seconduser");
        secondUserRegister.setEmail("second@example.com");
        secondUserRegister.setPassword("password");

        User secondUser = userService.createUser(secondUserRegister);
        String secondUserId = secondUser.getId();
        String secondUserToken = "Bearer " + jwtUtil.generateToken(secondUserId);
        
        // Create an idea owned by the first user
        IdeaRegister ideaRegister = new IdeaRegister();
        ideaRegister.setIdeaName("First User's Idea");
        ideaRegister.setIdeaDescription("This idea belongs to the first user");

        Idea createdIdea = ideaService.createIdea(projectId, ideaRegister, authHeader, new ArrayList<>());
        
        // Try to update the idea as the second user - should fail
        IdeaUpdate ideaUpdate = new IdeaUpdate();
        ideaUpdate.setIdeaName("Attempted Update");
        
        try {
            ideaService.updateIdea(projectId, createdIdea.getIdeaId(), ideaUpdate, secondUserToken);
            // If we reach here, the test has failed
            assertTrue(false, "Second user should not be able to update first user's idea");
        } 
        catch (ResponseStatusException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        }
    }
}