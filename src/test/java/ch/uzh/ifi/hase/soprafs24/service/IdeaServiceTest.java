package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaRegister;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaUpdate;
import ch.uzh.ifi.hase.soprafs24.models.websocket.IdeaUpdateMessage;
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.repository.CommentRepository;
import ch.uzh.ifi.hase.soprafs24.repository.IdeaRepository;

import org.springframework.context.annotation.Import;
import org.mockito.Mock;
import org.mockito.Mockito;
// import org.mockito.InjectMocks;
// import org.mockito.MockitoAnnotations;

import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;
import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.service.IdeaService;

import org.springframework.messaging.simp.SimpMessagingTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class IdeaServiceTest {

    private IdeaService ideaService;

    @Mock
    private JwtUtil jwtUtil;

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

    @Mock
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
            projectService
        );
        
        // Mock the user authentication
        when(userService.getUserIdByToken(VALID_AUTH_HEADER)).thenReturn(USER_ID);
        
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
        assertEquals("Test Idea", createdIdea.getIdeaName());
        assertEquals("Test Description", createdIdea.getIdeaDescription());
        assertEquals(USER_ID, createdIdea.getOwnerId());
        assertEquals(PROJECT_ID, createdIdea.getProjectId());

        // assertEquals(0L, createdIdea.getUpVotes());
        // assertEquals(0L, createdIdea.getDownVotes());
        verify(ideaRepository, times(1)).save(any(Idea.class));
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

        // when
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
    public void updateIdea_success() {
        // given
        Idea existingIdea = new Idea();
        existingIdea.setIdeaId(IDEA_ID);
        existingIdea.setIdeaName("Original Name");
        existingIdea.setIdeaDescription("Original Description");
        existingIdea.setOwnerId(USER_ID);
        existingIdea.setProjectId(PROJECT_ID);

        
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

    }

    // @Test
    // public void updateIdea_forbidden() {
    //     // given
    //     Idea existingIdea = new Idea();
    //     existingIdea.setIdeaId(IDEA_ID);
    //     existingIdea.setOwnerId("different-user-id");
    //     existingIdea.setProjectId(PROJECT_ID);
        
    //     IdeaUpdate ideaUpdate = new IdeaUpdate();
        
    //     when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.of(existingIdea));

    //     // when / then
    //     ResponseStatusException exception = assertThrows(
    //         ResponseStatusException.class,
    //         () -> ideaService.updateIdea(PROJECT_ID, IDEA_ID, ideaUpdate, VALID_AUTH_HEADER)
    //     );
        
    //     assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    //     assertEquals("You are not the owner of this idea", exception.getReason());
    // }

    // @Test
    // public void createSubIdea_success() {
    //     // given
    //     Idea parentIdea = new Idea();
    //     parentIdea.setIdeaId(IDEA_ID);

    //     IdeaRegister subIdeaRegister = new IdeaRegister();
    //     subIdeaRegister.setIdeaName("Sub Idea");
    //     subIdeaRegister.setIdeaDescription("Sub Description");
        
    //     Idea newSubIdea = new Idea();
    //     newSubIdea.setIdeaId("sub-idea-id");
    //     newSubIdea.setIdeaName("Sub Idea");
        
    //     when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.of(parentIdea));
    //     when(ideaRepository.save(any(Idea.class))).thenAnswer(i -> i.getArgument(0));

    //     // Mock the createIdea method to return our new sub-idea
    //     // This requires a bit of special handling since we're mocking a method in the class under test
    //     when(ideaRepository.save(any(Idea.class))).thenAnswer(invocation -> {
    //         Idea idea = invocation.getArgument(0);
    //         if (idea.getIdeaName() != null && idea.getIdeaName().equals("Sub Idea")) {
    //             return newSubIdea;
    //         }
    //         return idea;
    //     });

    //     // when



    //     // The original parent idea should have been updated to include the sub-idea
    //     verify(ideaRepository, times(3)).save(any(Idea.class)); // Once for the sub idea, once for parent update
    // }

    // @Test
    // public void testCreateIdeaSendsWebSocketMessage() {
    //     // Arrange
    //     IdeaRegister ideaRegister = new IdeaRegister();
    //     ideaRegister.setIdeaName("Test Idea");
    //     ideaRegister.setIdeaDescription("Test Description");
        
    //     // Mock repository save
    //     when(ideaRepository.save(any(Idea.class))).thenAnswer(invocation -> {
    //         Idea savedIdea = invocation.getArgument(0);
    //         savedIdea.setIdeaId(IDEA_ID);
    //         return savedIdea;
    //     });

    //     // Act
    //     ideaService.createIdea(PROJECT_ID, ideaRegister, VALID_AUTH_HEADER, new ArrayList<>());
        
    //     // Assert
    //     ArgumentCaptor<IdeaUpdateMessage> messageCaptor = ArgumentCaptor.forClass(IdeaUpdateMessage.class);
    //     verify(messagingTemplate).convertAndSend(
    //         eq("/topic/projects/" + PROJECT_ID + "/ideas"), 
    //         messageCaptor.capture()
    //     );

    //     // Then verify the content of the captured message
    //     IdeaUpdateMessage capturedMessage = messageCaptor.getValue();
    //     assertEquals("CREATE", capturedMessage.getAction());
    //     assertEquals(PROJECT_ID, capturedMessage.getProjectId());
    //     assertEquals(IDEA_ID, capturedMessage.getIdeaId());  // Note: Using your constant IDEA_ID here
    //     assertEquals("Test Idea", capturedMessage.getIdea().getIdeaName());
    // }

    // @Test
    // public void testUpdateIdeaSendsWebSocketMessage() {
    //     // Arrange
    //     IdeaUpdate ideaUpdate = new IdeaUpdate();
    //     ideaUpdate.setIdeaName("Updated Idea");
    //     ideaUpdate.setIdeaDescription("Updated Description");

    //     Idea existingIdea = new Idea();
    //     existingIdea.setIdeaId(IDEA_ID);
    //     existingIdea.setOwnerId(USER_ID);
    //     existingIdea.setProjectId(PROJECT_ID);
    //     existingIdea.setIdeaName("Original Idea");

    //     when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.of(existingIdea));
    //     when(ideaRepository.save(any(Idea.class))).thenReturn(existingIdea);
        
    //     // Act
    //     ideaService.updateIdea(PROJECT_ID, IDEA_ID, ideaUpdate, VALID_AUTH_HEADER);
        
    //     // Assert
    //     ArgumentCaptor<IdeaUpdateMessage> messageCaptor = ArgumentCaptor.forClass(IdeaUpdateMessage.class);
    //     verify(messagingTemplate).convertAndSend(
    //         eq("/topic/projects/" + PROJECT_ID + "/ideas"), 
    //         messageCaptor.capture()
    //     );
        
    //     IdeaUpdateMessage capturedMessage = messageCaptor.getValue();
    //     assertEquals("UPDATE", capturedMessage.getAction());
    //     assertEquals(PROJECT_ID, capturedMessage.getProjectId());
    //     assertEquals(IDEA_ID, capturedMessage.getIdeaId());
    // }

    // @SuppressWarnings("unchecked")
    // @Test
    // public void testCreateSubIdeaSendsWebSocketMessages() {
    //     // Arrange
    //     IdeaRegister subIdeaRegister = new IdeaRegister();
    //     subIdeaRegister.setIdeaName("Sub Idea");
    //     subIdeaRegister.setIdeaDescription("Sub Description");
        
    //     Idea parentIdea = new Idea();
    //     parentIdea.setIdeaId(IDEA_ID);
    //     parentIdea.setOwnerId(USER_ID);
    //     parentIdea.setProjectId(PROJECT_ID);

        
    //     Idea newSubIdea = new Idea();
    //     newSubIdea.setIdeaId("sub-idea-123");
    //     newSubIdea.setOwnerId(USER_ID);
    //     newSubIdea.setProjectId(PROJECT_ID);
        
    //     when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.of(parentIdea));
    //     when(ideaRepository.save(any(Idea.class))).thenReturn(parentIdea);
        
    //     // Mock the createIdea method to return our sub-idea
    //     IdeaService spyIdeaService = Mockito.spy(ideaService);
    //     doReturn(newSubIdea).when(spyIdeaService).createIdea(
    //         eq(PROJECT_ID), 
    //         any(IdeaRegister.class), 
    //         eq(VALID_AUTH_HEADER), 
    //         any(ArrayList.class)
    //     );

        
    //     // Assert - should capture the message for the parent idea update
    //     ArgumentCaptor<IdeaUpdateMessage> messageCaptor = ArgumentCaptor.forClass(IdeaUpdateMessage.class);
    //     verify(messagingTemplate).convertAndSend(
    //         eq("/topic/projects/" + PROJECT_ID + "/ideas"), 
    //         messageCaptor.capture()
    //     );
        
    //     IdeaUpdateMessage capturedMessage = messageCaptor.getValue();
    //     assertEquals("UPDATE", capturedMessage.getAction());
    //     assertEquals(PROJECT_ID, capturedMessage.getProjectId());
    //     assertEquals(IDEA_ID, capturedMessage.getIdeaId());
        
    //     // Verify the sub-idea was added to the parent's subIdeas list

    // }

    // @Test
    // public void testDeleteIdeaSendsWebSocketMessage() {
    //     // Arrange
    //     Idea existingIdea = new Idea();
    //     existingIdea.setIdeaId(IDEA_ID);
    //     existingIdea.setOwnerId(USER_ID);
    //     existingIdea.setProjectId(PROJECT_ID);
    //     existingIdea.setIdeaName("Idea to Delete");
        
    //     when(ideaRepository.findById(IDEA_ID)).thenReturn(Optional.of(existingIdea));
    //     doNothing().when(ideaRepository).deleteById(IDEA_ID);
        
    //     // Act
    //     ideaService.deleteIdea(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER);
        
    //     // Assert
    //     ArgumentCaptor<IdeaUpdateMessage> messageCaptor = ArgumentCaptor.forClass(IdeaUpdateMessage.class);
    //     verify(messagingTemplate).convertAndSend(
    //         eq("/topic/projects/" + PROJECT_ID + "/ideas"), 
    //         messageCaptor.capture()
    //     );
        
    //     IdeaUpdateMessage capturedMessage = messageCaptor.getValue();
    //     assertEquals("DELETE", capturedMessage.getAction());
    //     assertEquals(PROJECT_ID, capturedMessage.getProjectId());
    //     assertEquals(IDEA_ID, capturedMessage.getIdeaId());
    // }
}