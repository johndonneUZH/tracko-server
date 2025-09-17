// package service;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.Import;
// import org.springframework.http.HttpStatus;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.util.ReflectionTestUtils;
// import org.springframework.web.server.ResponseStatusException;

// import tracko.auth.JwtUtil;
// import config.MongoTestConfig;
// import tracko.repository.IdeaRepository;
// import tracko.repository.MessageRepository;
// import tracko.repository.ProjectRepository;
// import tracko.repository.UserRepository;
// import tracko.constant.ChangeType;
// import tracko.models.comment.Comment;
// import tracko.models.idea.Idea;
// import tracko.models.messages.Message;
// import tracko.models.messages.MessageRegister;
// import tracko.models.project.Project;
// import tracko.models.project.ProjectRegister;
// import tracko.models.project.ProjectUpdate;
// import tracko.models.report.ReportRegister;
// import tracko.models.user.User;
// import tracko.service.AIService;
// import tracko.service.ChangeService;
// import tracko.service.CommentService;
// import tracko.service.ProjectAuthorizationService;
// import tracko.service.ProjectService;
// import tracko.service.ReportService;
// import tracko.service.UserService;

// @SpringBootTest(classes = tracko.Application.class)
// @Import(MongoTestConfig.class)
// @ActiveProfiles("test")
// public class ProjectServiceTest {

//     private ProjectService projectService;

//     @MockBean
//     private JwtUtil jwtUtil;

//     @MockBean
//     private ProjectRepository projectRepository;

//     @MockBean
//     private UserRepository userRepository;

//     @MockBean
//     private UserService userService;

//     @MockBean
//     private ChangeService changeService;

//     @MockBean
//     private IdeaRepository ideaRepository;

//     @MockBean
//     private AIService aiService;

//     @MockBean
//     private CommentService commentService;

//     @MockBean
//     private MessageRepository messageRepository;

//     @MockBean
//     private ProjectAuthorizationService projectAuthorizationService;

//     @MockBean
//     private ReportService reportService;

//     private final String VALID_AUTH_HEADER = "Bearer valid-token";
//     private final String PROJECT_ID = "project-123";
//     private final String USER_ID = "user-123";
//     private final String OTHER_USER_ID = "user-456";

//     private Project testProject;
//     private ProjectRegister testProjectRegister;
//     private ProjectUpdate testProjectUpdate;
//     private User testUser;
    
//     @BeforeEach
//     public void setup() {
//         projectService = new ProjectService(
//             projectRepository, 
//             jwtUtil,                         
//             userService, 
//             changeService,
//             projectAuthorizationService, 
//             ideaRepository,
//             messageRepository,
//             aiService,
//             commentService,
//             reportService);

//         ReflectionTestUtils.setField(projectService, "userRepository", userRepository);

//         when(userService.getUserIdByToken(VALID_AUTH_HEADER)).thenReturn(USER_ID);

//         testUser = new User();
//         testUser.setId(USER_ID);
//         testUser.setUsername("testuser");
//         when(userService.getUserByToken(VALID_AUTH_HEADER)).thenReturn(testUser);
//         when(userService.getUserById(USER_ID)).thenReturn(testUser);

//         testProject = new Project();
//         testProject.setProjectId(PROJECT_ID);
//         testProject.setProjectName("Test Project");
//         testProject.setProjectDescription("Test Description");
//         testProject.setOwnerId(USER_ID);
//         testProject.setProjectMembers(new ArrayList<>());
//         testProject.setCreatedAt(LocalDateTime.now());
//         testProject.setUpdatedAt(LocalDateTime.now());

//         testProjectRegister = new ProjectRegister();
//         testProjectRegister.setProjectName("Test Project");
//         testProjectRegister.setProjectDescription("Test Description");

//         testProjectUpdate = new ProjectUpdate();
//         testProjectUpdate.setProjectName("Updated Project");
//         testProjectUpdate.setProjectDescription("Updated Description");
//         testProjectUpdate.setMembersToAdd(Arrays.asList(OTHER_USER_ID));
//         testProjectUpdate.setMembersToRemove(new ArrayList<>());
//     }

//     @Test
//     public void createProject_success() {
//         when(projectRepository.findByOwnerIdAndProjectName(USER_ID, testProjectRegister.getProjectName()))
//             .thenReturn(null);
//         when(projectRepository.save(any(Project.class))).thenReturn(testProject);

//         Project createdProject = projectService.createProject(testProjectRegister, VALID_AUTH_HEADER);

//         assertNotNull(createdProject);
//         assertEquals(PROJECT_ID, createdProject.getProjectId());
//         assertEquals("Test Project", createdProject.getProjectName());
//         assertEquals("Test Description", createdProject.getProjectDescription());
//         assertEquals(USER_ID, createdProject.getOwnerId());
        
//         verify(projectRepository, times(1)).save(any(Project.class));
//         verify(userService, times(1)).addProjectIdToUser(USER_ID, PROJECT_ID);
//     }

//     @Test
//     public void createProject_duplicateName_conflict() {
//         when(projectRepository.findByOwnerIdAndProjectName(USER_ID, testProjectRegister.getProjectName()))
//             .thenReturn(testProject);

//         ResponseStatusException exception = assertThrows(
//             ResponseStatusException.class,
//             () -> projectService.createProject(testProjectRegister, VALID_AUTH_HEADER)
//         );

//         assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
//         assertEquals("Project with this name already exists", exception.getReason());
//     }

//     @Test
//     public void authenticateProject_success() {
//         when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
//             .thenReturn(testProject);

//         Project authenticatedProject = projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER);

//         assertNotNull(authenticatedProject);
//         assertEquals(PROJECT_ID, authenticatedProject.getProjectId());
//     }

//     @Test
//     public void getProjectsByUserId_success() {
//         List<Project> ownedProjects = new ArrayList<>();
//         ownedProjects.add(testProject);
        
//         Project memberProject = new Project();
//         memberProject.setProjectId("project-456");
//         memberProject.setOwnerId(OTHER_USER_ID);
//         memberProject.setProjectMembers(Arrays.asList(USER_ID));
        
//         List<Project> memberProjects = new ArrayList<>();
//         memberProjects.add(memberProject);
        
//         when(projectRepository.findByOwnerId(USER_ID)).thenReturn(ownedProjects);
//         when(projectRepository.findByProjectMembers(USER_ID)).thenReturn(memberProjects);
        
//         List<Project> projects = projectService.getProjectsByUserId(USER_ID);
        
//         assertEquals(2, projects.size());
//         assertTrue(projects.contains(testProject));
//         assertTrue(projects.contains(memberProject));
//     }

//     @Test
//     public void updateProject_success() {
//         when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
//             .thenReturn(testProject);
//         when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         Project updatedProject = projectService.updateProject(PROJECT_ID, testProjectUpdate, VALID_AUTH_HEADER);

//         assertNotNull(updatedProject);
//         assertEquals("Updated Project", updatedProject.getProjectName());
//         assertEquals("Updated Description", updatedProject.getProjectDescription());
//         assertTrue(updatedProject.getProjectMembers().contains(OTHER_USER_ID));
        
//         verify(projectRepository, times(1)).save(any(Project.class));
//         verify(changeService, times(1)).markChange(eq(PROJECT_ID), eq(ChangeType.ADDED_MEMBER), eq(VALID_AUTH_HEADER), eq(false), eq(null));
//         verify(userService, times(1)).addProjectIdToUser(OTHER_USER_ID, PROJECT_ID);
//     }

//     @Test
//     public void updateProject_notOwner_forbidden() {
//         testProject.setOwnerId(OTHER_USER_ID);
//         when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
//             .thenReturn(testProject);

//         ResponseStatusException exception = assertThrows(
//             ResponseStatusException.class,
//             () -> projectService.updateProject(PROJECT_ID, testProjectUpdate, VALID_AUTH_HEADER)
//         );

//         assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
//         assertEquals("You are not the owner of this project", exception.getReason());
        
//         verify(projectRepository, never()).save(any(Project.class));
//     }

//     @Test
//     public void updateProject_memberManagement() {
//         testProject.setProjectMembers(new ArrayList<>(Arrays.asList("user-111", "user-222")));
        
//         ProjectUpdate updateWithMembers = new ProjectUpdate();
//         updateWithMembers.setProjectName("Updated Project");
//         updateWithMembers.setProjectDescription("Updated Description");
//         updateWithMembers.setMembersToAdd(Arrays.asList("user-333", "user-444"));
//         updateWithMembers.setMembersToRemove(Arrays.asList("user-111"));
        
//         when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
//             .thenReturn(testProject);
//         when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         Project updatedProject = projectService.updateProject(PROJECT_ID, updateWithMembers, VALID_AUTH_HEADER);

//         assertNotNull(updatedProject);
//         assertEquals(3, updatedProject.getProjectMembers().size());
//         assertTrue(updatedProject.getProjectMembers().contains("user-222"));
//         assertTrue(updatedProject.getProjectMembers().contains("user-333"));
//         assertTrue(updatedProject.getProjectMembers().contains("user-444"));
//         assertFalse(updatedProject.getProjectMembers().contains("user-111"));
        
//         verify(projectRepository, times(1)).save(any(Project.class));
//         verify(changeService, times(1)).markChange(eq(PROJECT_ID), eq(ChangeType.ADDED_MEMBER), eq(VALID_AUTH_HEADER), eq(false), eq(null));
//         verify(userService, times(1)).addProjectIdToUser("user-333", PROJECT_ID);
//         verify(userService, times(1)).addProjectIdToUser("user-444", PROJECT_ID);
//         verify(userService, times(1)).deleteProjectFromUser("user-111", PROJECT_ID);
//     }
    
//     @Test
//     public void deleteProject_success() {
//         testProject.setProjectMembers(Arrays.asList(OTHER_USER_ID));
//         User otherUser = new User();
//         otherUser.setId(OTHER_USER_ID);
        
//         when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
//             .thenReturn(testProject);
//         when(userService.getUserById(OTHER_USER_ID)).thenReturn(otherUser);
        
//         projectService.deleteProject(PROJECT_ID, VALID_AUTH_HEADER);
        
//         verify(userService, times(1)).deleteProjectFromUser(OTHER_USER_ID, PROJECT_ID);
//         verify(userService, times(1)).deleteProjectFromUser(USER_ID, PROJECT_ID);
//         verify(changeService, times(1)).deleteChangesByProjectId(PROJECT_ID);
//         verify(ideaRepository, times(1)).deleteByProjectId(PROJECT_ID);
//         verify(projectRepository, times(1)).deleteById(PROJECT_ID);
//     }
    
//     @Test
//     public void deleteProject_notOwner_forbidden() {
//         testProject.setOwnerId(OTHER_USER_ID);
//         when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
//             .thenReturn(testProject);
        
//         ResponseStatusException exception = assertThrows(
//             ResponseStatusException.class,
//             () -> projectService.deleteProject(PROJECT_ID, VALID_AUTH_HEADER)
//         );
        
//         assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
//         assertEquals("You are not the owner of this project", exception.getReason());
        
//         verify(projectRepository, never()).deleteById(any());
//     }
    
//     @Test
//     public void getProjectMembers_success() {
//         testProject.setProjectMembers(Arrays.asList(OTHER_USER_ID));
//         User otherUser = new User();
//         otherUser.setId(OTHER_USER_ID);
        
//         when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
//             .thenReturn(testProject);
//         when(userService.getUserById(OTHER_USER_ID)).thenReturn(otherUser);
        
//         List<User> members = projectService.getProjectMembers(PROJECT_ID, VALID_AUTH_HEADER);
        
//         assertEquals(1, members.size());
//         assertTrue(members.contains(testUser));
//     }
    
//     @Test
//     public void getOwnerIdByProjectId_success() {
//         when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        
//         String ownerId = projectService.getOwnerIdByProjectId(PROJECT_ID);
        
//         assertEquals(USER_ID, ownerId);
//     }
    
//     @Test
//     public void getOwnerIdByProjectId_notFound() {
//         when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());
        
//         ResponseStatusException exception = assertThrows(
//             ResponseStatusException.class,
//             () -> projectService.getOwnerIdByProjectId(PROJECT_ID)
//         );
        
//         assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
//         assertEquals("Project not found", exception.getReason());
//     }
    
//     @Test
//     public void makeUserLeaveFromProject_success() {
//         testProject.setProjectMembers(new ArrayList<>(Arrays.asList(USER_ID, OTHER_USER_ID)));
        
//         when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
//             .thenReturn(testProject);
//         when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        
//         projectService.makeUserLeaveFromProject(PROJECT_ID, VALID_AUTH_HEADER);
        
//         verify(userService, times(1)).deleteProjectFromUser(USER_ID, PROJECT_ID);
//         verify(projectRepository, times(1)).save(testProject);
//         assertFalse(testProject.getProjectMembers().contains(USER_ID));
//     }
    
//     @Test
//     public void sendChatMessage_success() {
//         MessageRegister messageRegister = new MessageRegister();
        
//         Message newMessage = new Message();
//         newMessage.setId("message-123");
//         newMessage.setProjectId(PROJECT_ID);
//         newMessage.setSenderId(USER_ID);
//         newMessage.setUsername("testuser");
//         newMessage.setContent("Test message");
//         newMessage.setCreatedAt(LocalDateTime.now());
        
//         when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
//             .thenReturn(testProject);
//         when(messageRepository.save(any(Message.class))).thenReturn(newMessage);
        
//         Message result = projectService.sendChatMessage(PROJECT_ID, VALID_AUTH_HEADER, messageRegister);
        
//         assertNotNull(result);
//         assertEquals(newMessage.getId(), result.getId());
//         assertEquals(newMessage.getContent(), result.getContent());
//         assertEquals(USER_ID, result.getSenderId());
//         assertEquals("testuser", result.getUsername());
//     }
    
//     @Test
//     public void getMessages_success() {
//         List<Message> messages = new ArrayList<>();
//         Message message1 = new Message();
//         message1.setId("message-123");
//         message1.setProjectId(PROJECT_ID);
//         messages.add(message1);
        
//         when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
//             .thenReturn(testProject);
//         when(messageRepository.findByProjectIdOrderByCreatedAtAsc(PROJECT_ID)).thenReturn(messages);
        
//         List<Message> result = projectService.getMessages(PROJECT_ID, VALID_AUTH_HEADER);
        
//         assertEquals(1, result.size());
//         assertEquals("message-123", result.get(0).getId());
//     }
    
//     @Test
//     public void generateReport_success() {
//         List<Message> messages = new ArrayList<>();
//         Message message = new Message();
//         message.setContent("Test message");
//         message.setCreatedAt(LocalDateTime.now());
//         messages.add(message);
        
//         List<Idea> ideas = new ArrayList<>();
//         Idea idea = new Idea();
//         idea.setIdeaId("idea-123");
//         idea.setIdeaName("Test Idea");
//         idea.setIdeaDescription("Test Description");
//         idea.setUpVotes(new ArrayList<>());
//         idea.setDownVotes(new ArrayList<>());
//         idea.setComments(new ArrayList<>());
//         ideas.add(idea);
        
//         List<Comment> comments = new ArrayList<>();
//         Comment comment = new Comment();
//         comment.setCommentText("Test comment");
//         comment.setIdeaId("idea-123");
//         comments.add(comment);
        
//         String geminiResponse = "<h2>Project Report</h2><p>This is a test report</p>";
        
//         when(projectAuthorizationService.authenticateProject(PROJECT_ID, VALID_AUTH_HEADER))
//             .thenReturn(testProject);
//         when(messageRepository.findByProjectIdOrderByCreatedAtAsc(PROJECT_ID)).thenReturn(messages);
//         when(ideaRepository.findByProjectId(PROJECT_ID)).thenReturn(ideas);
//         when(commentService.getCommentsByIdeaId("idea-123")).thenReturn(comments);
//         when(aiService.generateContent(any())).thenReturn(geminiResponse);
        
//         String result = projectService.generateReport(PROJECT_ID, VALID_AUTH_HEADER);
        
//         assertNotNull(result);
//         assertEquals("<h2>Project Report</h2><p>This is a test report</p>", result);
        
//         verify(reportService, times(1)).createReport(any(ReportRegister.class), eq(USER_ID), eq(PROJECT_ID));
//     }    
// }
