package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;
import ch.uzh.ifi.hase.soprafs24.constant.ChangeType;
import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaRegister;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaUpdate;
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.models.user.User;
import ch.uzh.ifi.hase.soprafs24.repository.CommentRepository;
import ch.uzh.ifi.hase.soprafs24.repository.IdeaRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class IdeaServiceTest {

    private IdeaService ideaService;

    @MockBean
    private IdeaRepository ideaRepository;

    @MockBean
    private CommentRepository commentRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ProjectAuthorizationService projectAuthorizationService;

    @MockBean
    private ChangeService changeService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    private final String VALID_AUTH_HEADER = "Bearer valid-token";
    private final String PROJECT_ID = "project-123";
    private final String IDEA_ID = "idea-123";
    private final String USER_ID = "user-123";

    @BeforeEach
    public void setup() {
        ideaService = new IdeaService(
            ideaRepository, 
            userService, 
            messagingTemplate,
            projectAuthorizationService,
            commentRepository,
            projectService,
            changeService
        );
    
        // Create a mock user
        User mockUser = new User();
        mockUser.setId(USER_ID);
        mockUser.setUsername("testuser");

        // Mock user service calls
        when(userService.getUserIdByToken(VALID_AUTH_HEADER)).thenReturn(USER_ID);
        when(userService.getUserById(USER_ID)).thenReturn(mockUser);
        when(userService.getUserByToken(VALID_AUTH_HEADER)).thenReturn(mockUser); 
            
        // Mock project authentication
        Project project = new Project();
        project.setProjectId(PROJECT_ID);
        project.setOwnerId(USER_ID);
        
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER)).thenReturn(project);
    }

    @Test
    public void createIdea_success() {
        // given
        IdeaRegister ideaRegister = new IdeaRegister();
        ideaRegister.setIdeaName("Test Idea");
        ideaRegister.setIdeaDescription("Test Description");
        ideaRegister.setx(100.0f);
        ideaRegister.sety(200.0f);
        
        // Mock repository save
        when(ideaRepository.save(any(Idea.class))).thenAnswer(invocation -> {
            Idea savedIdea = invocation.getArgument(0);
            savedIdea.setIdeaId(IDEA_ID);
            return savedIdea;
        });

        // when
        Idea createdIdea = ideaService.createIdea(PROJECT_ID, ideaRegister, VALID_AUTH_HEADER, new ArrayList<>());

        // then
        assertNotNull(createdIdea);
        assertEquals(IDEA_ID, createdIdea.getIdeaId());
        assertEquals("Test Idea", createdIdea.getIdeaName());
        assertEquals("Test Description", createdIdea.getIdeaDescription());
        assertEquals(100.0f, createdIdea.getX());
        assertEquals(200.0f, createdIdea.gety());
        assertEquals(USER_ID, createdIdea.getOwnerId());
        assertEquals(PROJECT_ID, createdIdea.getProjectId());
        assertEquals(0, createdIdea.getUpVotes().size());
        assertEquals(0, createdIdea.getDownVotes().size());
        assertEquals(0, createdIdea.getComments().size());

        verify(ideaRepository, times(1)).save(any(Idea.class));
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/projects/" + PROJECT_ID + "/ideas"),
            any(Idea.class)
        );
        verify(changeService, times(1)).markChange(
            eq(PROJECT_ID),
            eq(ChangeType.ADDED_IDEA),
            eq(VALID_AUTH_HEADER),
            eq(false),
            eq(null)
        );
    }

    @Test
    public void getIdeasByProject_success() {
        // given
        Idea idea1 = new Idea();
        idea1.setIdeaId("idea-1");
        idea1.setIdeaName("Idea 1");
        idea1.setProjectId(PROJECT_ID);
        
        Idea idea2 = new Idea();
        idea2.setIdeaId("idea-2");
        idea2.setIdeaName("Idea 2");
        idea2.setProjectId(PROJECT_ID);
        
        List<Idea> ideas = Arrays.asList(idea1, idea2);
        
        when(ideaRepository.findByProjectId(PROJECT_ID)).thenReturn(ideas);

        // when
        List<Idea> foundIdeas = ideaService.getIdeasByProject(PROJECT_ID, VALID_AUTH_HEADER);

        // then
        assertEquals(2, foundIdeas.size());
        assertEquals("Idea 1", foundIdeas.get(0).getIdeaName());
        assertEquals("Idea 2", foundIdeas.get(1).getIdeaName());
        verify(projectAuthorizationService, times(1)).authenticateProject(PROJECT_ID, VALID_AUTH_HEADER);
    }

    @Test
    public void getIdeaById_success() {
        // given
        Idea idea = new Idea();
        idea.setIdeaId(IDEA_ID);
        idea.setIdeaName("Test Idea");
        idea.setProjectId(PROJECT_ID);
        
        when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.of(idea));

        // whena
        Idea foundIdea = ideaService.getIdeaById(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER);

        // then
        assertNotNull(foundIdea);
        assertEquals(IDEA_ID, foundIdea.getIdeaId());
        assertEquals("Test Idea", foundIdea.getIdeaName());
        verify(projectAuthorizationService, times(1)).authenticateProject(PROJECT_ID, VALID_AUTH_HEADER);
    }

    @Test
    public void getIdeaById_notFound() {
        // given
        when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.empty());

        // when / then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> ideaService.getIdeaById(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER)
        );
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Idea not found", exception.getReason());
    }

    @Test
    public void updateIdea_nameAndDescription_success() {
        // given
        Idea existingIdea = new Idea();
        existingIdea.setIdeaId(IDEA_ID);
        existingIdea.setIdeaName("Original Name");
        existingIdea.setIdeaDescription("Original Description");
        existingIdea.setOwnerId(USER_ID);
        existingIdea.setProjectId(PROJECT_ID);
        existingIdea.setUpVotes(new ArrayList<>());
        existingIdea.setDownVotes(new ArrayList<>());
        existingIdea.setComments(new ArrayList<>());
        
        IdeaUpdate ideaUpdate = new IdeaUpdate();
        ideaUpdate.setIdeaName("Updated Name");
        ideaUpdate.setIdeaDescription("Updated Description");

        when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.of(existingIdea));
        when(ideaRepository.save(any(Idea.class))).thenAnswer(i -> i.getArgument(0));

        // when
        Idea updatedIdea = ideaService.updateIdea(PROJECT_ID, IDEA_ID, ideaUpdate, VALID_AUTH_HEADER);

        // then
        assertNotNull(updatedIdea);
        assertEquals("Updated Name", updatedIdea.getIdeaName());
        assertEquals("Updated Description", updatedIdea.getIdeaDescription());
        
        verify(ideaRepository, times(1)).save(any(Idea.class));
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/projects/" + PROJECT_ID + "/ideas"),
            any(Idea.class)
        );
        verify(changeService, times(1)).markChange(
            eq(PROJECT_ID),
            eq(ChangeType.MODIFIED_IDEA),
            eq(VALID_AUTH_HEADER),
            eq(false),
            eq(null)
        );
    }
    
    @Test
    public void updateIdea_position_success() {
        // given
        Idea existingIdea = new Idea();
        existingIdea.setIdeaId(IDEA_ID);
        existingIdea.setIdeaName("Original Name");
        existingIdea.setIdeaDescription("Original Description");
        existingIdea.setOwnerId(USER_ID);
        existingIdea.setProjectId(PROJECT_ID);
        existingIdea.setx(100.0f);
        existingIdea.sety(100.0f);
        existingIdea.setUpVotes(new ArrayList<>());
        existingIdea.setDownVotes(new ArrayList<>());
        existingIdea.setComments(new ArrayList<>());
        
        IdeaUpdate ideaUpdate = new IdeaUpdate();
        ideaUpdate.setx(200.0f);
        ideaUpdate.sety(300.0f);

        when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.of(existingIdea));
        when(ideaRepository.save(any(Idea.class))).thenAnswer(i -> i.getArgument(0));

        // when
        Idea updatedIdea = ideaService.updateIdea(PROJECT_ID, IDEA_ID, ideaUpdate, VALID_AUTH_HEADER);

        // then
        assertNotNull(updatedIdea);
        assertEquals(200.0f, updatedIdea.getX());
        assertEquals(300.0f, updatedIdea.gety());
        
        verify(ideaRepository, times(1)).save(any(Idea.class));
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/projects/" + PROJECT_ID + "/ideas"),
            any(Idea.class)
        );
        // No change type should be marked for just position updates
        verify(changeService, never()).markChange(
            eq(PROJECT_ID),
            eq(ChangeType.MODIFIED_IDEA),
            eq(VALID_AUTH_HEADER),
            eq(false),
            eq(null)
        );
    }
    
    @Test
    public void updateIdea_upvote_success() {
        // given
        Idea existingIdea = new Idea();
        existingIdea.setIdeaId(IDEA_ID);
        existingIdea.setIdeaName("Original Name");
        existingIdea.setOwnerId(USER_ID);
        existingIdea.setProjectId(PROJECT_ID);
        existingIdea.setUpVotes(new ArrayList<>());
        existingIdea.setDownVotes(new ArrayList<>());
        existingIdea.setComments(new ArrayList<>());
        
        ArrayList<String> upvotes = new ArrayList<>();
        upvotes.add(USER_ID);
        
        IdeaUpdate ideaUpdate = new IdeaUpdate();
        ideaUpdate.setUpVotes(upvotes);

        when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.of(existingIdea));
        when(ideaRepository.save(any(Idea.class))).thenAnswer(i -> i.getArgument(0));

        // when
        Idea updatedIdea = ideaService.updateIdea(PROJECT_ID, IDEA_ID, ideaUpdate, VALID_AUTH_HEADER);

        // then
        assertNotNull(updatedIdea);
        assertEquals(1, updatedIdea.getUpVotes().size());
        assertEquals(USER_ID, updatedIdea.getUpVotes().get(0));
        
        verify(ideaRepository, times(1)).save(any(Idea.class));
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/projects/" + PROJECT_ID + "/ideas"),
            any(Idea.class)
        );
        verify(changeService, times(1)).markChange(
            eq(PROJECT_ID),
            eq(ChangeType.UPVOTE),
            eq(VALID_AUTH_HEADER),
            eq(false),
            eq(null)
        );
    }
    
    @Test
    public void updateIdea_downvote_success() {
        // given
        Idea existingIdea = new Idea();
        existingIdea.setIdeaId(IDEA_ID);
        existingIdea.setIdeaName("Original Name");
        existingIdea.setOwnerId(USER_ID);
        existingIdea.setProjectId(PROJECT_ID);
        existingIdea.setUpVotes(new ArrayList<>());
        existingIdea.setDownVotes(new ArrayList<>());
        existingIdea.setComments(new ArrayList<>());
        
        ArrayList<String> downvotes = new ArrayList<>();
        downvotes.add(USER_ID);
        
        IdeaUpdate ideaUpdate = new IdeaUpdate();
        ideaUpdate.setDownVotes(downvotes);

        when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.of(existingIdea));
        when(ideaRepository.save(any(Idea.class))).thenAnswer(i -> i.getArgument(0));

        // when
        Idea updatedIdea = ideaService.updateIdea(PROJECT_ID, IDEA_ID, ideaUpdate, VALID_AUTH_HEADER);

        // then
        assertNotNull(updatedIdea);
        assertEquals(1, updatedIdea.getDownVotes().size());
        assertEquals(USER_ID, updatedIdea.getDownVotes().get(0));
        
        verify(ideaRepository, times(1)).save(any(Idea.class));
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/projects/" + PROJECT_ID + "/ideas"),
            any(Idea.class)
        );
        verify(changeService, times(1)).markChange(
            eq(PROJECT_ID),
            eq(ChangeType.DOWNVOTE),
            eq(VALID_AUTH_HEADER),
            eq(false),
            eq(null)
        );
    }

    @Test
    public void deleteIdea_success() {
        // given
        Idea existingIdea = new Idea();
        existingIdea.setIdeaId(IDEA_ID);
        existingIdea.setIdeaName("Idea to Delete");
        existingIdea.setOwnerId(USER_ID);
        existingIdea.setProjectId(PROJECT_ID);
        
        when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.of(existingIdea));
        doNothing().when(ideaRepository).deleteById(IDEA_ID);
        doNothing().when(commentRepository).deleteByIdeaId(IDEA_ID);

        // when
        ideaService.deleteIdea(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER);
        
        // then
        verify(ideaRepository, times(1)).deleteById(IDEA_ID);
        verify(commentRepository, times(1)).deleteByIdeaId(IDEA_ID);
        
        ArgumentCaptor<Map<String, String>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/projects/" + PROJECT_ID + "/ideas"),
            messageCaptor.capture()
        );
        
        Map<String, String> capturedMessage = messageCaptor.getValue();
        assertEquals(IDEA_ID, capturedMessage.get("deletedId"));
        
        verify(changeService, times(1)).markChange(
            eq(PROJECT_ID),
            eq(ChangeType.CLOSED_IDEA),
            eq(VALID_AUTH_HEADER),
            eq(false),
            eq(null)
        );
    }
    
    @Test
    public void deleteIdea_notFound() {
        // given
        when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.empty());

        // when/then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> ideaService.deleteIdea(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER)
        );
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Idea not found", exception.getReason());
    }
    
    @Test
    public void deleteIdea_notOwner_forbidden() {
        // given
        Idea existingIdea = new Idea();
        existingIdea.setIdeaId(IDEA_ID);
        existingIdea.setIdeaName("Idea to Delete");
        existingIdea.setOwnerId("different-user-id");
        existingIdea.setProjectId(PROJECT_ID);
        
        Project project = new Project();
        project.setProjectId(PROJECT_ID);
        project.setOwnerId("another-user-id");
        
        when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.of(existingIdea));
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER)).thenReturn(project);
        when(projectService.getOwnerIdByProjectId(PROJECT_ID)).thenReturn("another-user-id");

        // when/then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> ideaService.deleteIdea(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER)
        );
        
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("You are not the owner of this idea", exception.getReason());
    }
    
    @Test
    public void deleteIdea_asProjectOwner_success() {
        // given
        Idea existingIdea = new Idea();
        existingIdea.setIdeaId(IDEA_ID);
        existingIdea.setIdeaName("Idea to Delete");
        existingIdea.setOwnerId("different-user-id");
        existingIdea.setProjectId(PROJECT_ID);
        
        // User is project owner
        Project project = new Project();
        project.setProjectId(PROJECT_ID);
        project.setOwnerId(USER_ID);
        
        when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.of(existingIdea));
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER)).thenReturn(project);
        when(projectService.getOwnerIdByProjectId(PROJECT_ID)).thenReturn(USER_ID);
        doNothing().when(ideaRepository).deleteById(IDEA_ID);
        doNothing().when(commentRepository).deleteByIdeaId(IDEA_ID);

        // when
        ideaService.deleteIdea(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER);
        
        // then
        verify(ideaRepository, times(1)).deleteById(IDEA_ID);
        verify(commentRepository, times(1)).deleteByIdeaId(IDEA_ID);
    }
}