

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import auth.JwtUtil;
import config.MongoTestConfig;
import repository.IdeaRepository;
import repository.ProjectRepository;
import repository.UserRepository;
import models.idea.Idea;
import models.idea.IdeaRegister;
import models.idea.IdeaUpdate;
import models.project.Project;
import models.project.ProjectRegister;
import models.user.User;
import models.user.UserRegister;
import service.IdeaService;
import service.ProjectService;
import service.UserService;

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
        ideaRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        UserRegister userRegister = new UserRegister();
        userRegister.setName("Test User");
        userRegister.setUsername("testuser");
        userRegister.setEmail("test@example.com");
        userRegister.setPassword("password");

        User createdUser = userService.createUser(userRegister);
        userId = createdUser.getId();

        String token = jwtUtil.generateToken(userId);
        authHeader = "Bearer " + token;

        ProjectRegister projectRegister = new ProjectRegister();
        projectRegister.setProjectName("Test Project");
        projectRegister.setProjectDescription("This is a test project");

        Project createdProject = projectService.createProject(projectRegister, authHeader);
        projectId = createdProject.getProjectId();
    }

    @Test
    public void testCreateAndRetrieveIdea() {
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

        Idea retrievedIdea = ideaService.getIdeaById(projectId, createdIdea.getIdeaId(), authHeader);
        assertEquals(createdIdea.getIdeaId(), retrievedIdea.getIdeaId());
        assertEquals(createdIdea.getIdeaName(), retrievedIdea.getIdeaName());
        assertEquals(createdIdea.getIdeaDescription(), retrievedIdea.getIdeaDescription());
    }

    @Test
    public void testUpdateIdea() {
        IdeaRegister ideaRegister = new IdeaRegister();
        ideaRegister.setIdeaName("Original Idea");
        ideaRegister.setIdeaDescription("Original Description");

        Idea createdIdea = ideaService.createIdea(projectId, ideaRegister, authHeader, new ArrayList<>());
        
        IdeaUpdate ideaUpdate = new IdeaUpdate();
        ideaUpdate.setIdeaName("Updated Idea");
        ideaUpdate.setIdeaDescription("Updated Description");

        Idea updatedIdea = ideaService.updateIdea(projectId, createdIdea.getIdeaId(), ideaUpdate, authHeader);
        
        assertEquals("Updated Idea", updatedIdea.getIdeaName());
        assertEquals("Updated Description", updatedIdea.getIdeaDescription());
    }

    @Test
    public void testUnauthorizedAccess() {
        UserRegister secondUserRegister = new UserRegister();
        secondUserRegister.setName("Second User");
        secondUserRegister.setUsername("seconduser");
        secondUserRegister.setEmail("second@example.com");
        secondUserRegister.setPassword("password");

        User secondUser = userService.createUser(secondUserRegister);
        String secondUserId = secondUser.getId();
        String secondUserToken = "Bearer " + jwtUtil.generateToken(secondUserId);
        
        IdeaRegister ideaRegister = new IdeaRegister();
        ideaRegister.setIdeaName("First User's Idea");
        ideaRegister.setIdeaDescription("This idea belongs to the first user");

        Idea createdIdea = ideaService.createIdea(projectId, ideaRegister, authHeader, new ArrayList<>());
        
        IdeaUpdate ideaUpdate = new IdeaUpdate();
        ideaUpdate.setIdeaName("Attempted Update");
        
        try {
            ideaService.updateIdea(projectId, createdIdea.getIdeaId(), ideaUpdate, secondUserToken);
            assertTrue(false, "Second user should not be able to update first user's idea");
        } 
        catch (ResponseStatusException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        }
    }
}