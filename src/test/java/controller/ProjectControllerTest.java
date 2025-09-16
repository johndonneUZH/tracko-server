package controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import tracko.auth.JwtUtil;
import tracko.models.messages.Message;
import tracko.models.messages.MessageRegister;
import tracko.models.project.Project;
import tracko.models.project.ProjectRegister;
import tracko.models.project.ProjectUpdate;
import tracko.models.user.User;
import tracko.service.ProjectAuthorizationService;
import tracko.service.ProjectService;
import org.springframework.test.context.ContextConfiguration;

@WebMvcTest(controllers = tracko.controller.ProjectController.class)
@ContextConfiguration(classes = tracko.Application.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ProjectAuthorizationService projectAuthorizationService;

    private final String AUTH_HEADER = "Bearer valid-token";
    private final String PROJECT_ID = "project-123";
    private final String USER_ID = "user-123";

    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    private Project testProject;
    private ProjectRegister testProjectRegister;
    private ProjectUpdate testProjectUpdate;
    private List<User> testMembers;
    private MessageRegister testMessageRegister;
    private List<Message> testMessages;

    @BeforeEach
    public void setup() {
        testProject = new Project();
        testProject.setProjectId(PROJECT_ID);
        testProject.setProjectName("Test Project");
        testProject.setProjectDescription("Test Description");
        testProject.setOwnerId(USER_ID);
        testProject.setProjectMembers(new ArrayList<>());
        testProject.setCreatedAt(LocalDateTime.now());
        testProject.setUpdatedAt(LocalDateTime.now());

        testProjectRegister = new ProjectRegister();
        testProjectRegister.setProjectName("Test Project");
        testProjectRegister.setProjectDescription("Test Description");

        testProjectUpdate = new ProjectUpdate();
        testProjectUpdate.setProjectName("Updated Project");
        testProjectUpdate.setProjectDescription("Updated Description");
        testProjectUpdate.setMembersToAdd(Arrays.asList("user-456"));
        testProjectUpdate.setMembersToRemove(new ArrayList<>());
        
        testMembers = new ArrayList<>();
        User member1 = new User();
        member1.setId(USER_ID);
        member1.setUsername("testuser1");
        member1.setEmail("test1@example.com");
        
        User member2 = new User();
        member2.setId("user-456");
        member2.setUsername("testuser2");
        member2.setEmail("test2@example.com");
        
        testMembers.add(member1);
        testMembers.add(member2);
        
        testMessageRegister = new MessageRegister();
        
        testMessages = new ArrayList<>();
        Message message1 = new Message();
        message1.setId("message-123");
        message1.setContent("Test message 1");
        message1.setSenderId(USER_ID);
        message1.setCreatedAt(LocalDateTime.now());
        
        Message message2 = new Message();
        message2.setId("message-456");
        message2.setContent("Test message 2");
        message2.setSenderId("user-456");
        message2.setCreatedAt(LocalDateTime.now());
        
        testMessages.add(message1);
        testMessages.add(message2);
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void createProject_success() throws Exception {
        when(projectService.createProject(any(ProjectRegister.class), eq(AUTH_HEADER))).thenReturn(testProject);

        mockMvc.perform(post("/projects")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(testProjectRegister)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(PROJECT_ID))
                .andExpect(jsonPath("$.projectName").value("Test Project"))
                .andExpect(jsonPath("$.projectDescription").value("Test Description"))
                .andExpect(jsonPath("$.ownerId").value(USER_ID));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void getProject_success() throws Exception {
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, AUTH_HEADER)).thenReturn(testProject);

        mockMvc.perform(get("/projects/{projectId}", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.projectId").value(PROJECT_ID))
                .andExpect(jsonPath("$.projectName").value("Test Project"))
                .andExpect(jsonPath("$.projectDescription").value("Test Description"))
                .andExpect(jsonPath("$.ownerId").value(USER_ID));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void getProject_notFound() throws Exception {
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, AUTH_HEADER))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        mockMvc.perform(get("/projects/{projectId}", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void getProject_forbidden() throws Exception {
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, AUTH_HEADER))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project"));

        mockMvc.perform(get("/projects/{projectId}", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void updateProject_success() throws Exception {
        Project updatedProject = new Project();
        updatedProject.setProjectId(PROJECT_ID);
        updatedProject.setProjectName("Updated Project");
        updatedProject.setProjectDescription("Updated Description");
        updatedProject.setOwnerId(USER_ID);
        updatedProject.setProjectMembers(Arrays.asList("user-456"));
        updatedProject.setCreatedAt(testProject.getCreatedAt());
        updatedProject.setUpdatedAt(LocalDateTime.now());

        when(projectService.updateProject(eq(PROJECT_ID), any(ProjectUpdate.class), eq(AUTH_HEADER))).thenReturn(updatedProject);

        mockMvc.perform(put("/projects/{projectId}", PROJECT_ID)
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(testProjectUpdate)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.projectId").value(PROJECT_ID))
                .andExpect(jsonPath("$.projectName").value("Updated Project"))
                .andExpect(jsonPath("$.projectDescription").value("Updated Description"))
                .andExpect(jsonPath("$.ownerId").value(USER_ID))
                .andExpect(jsonPath("$.projectMembers[0]").value("user-456"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void updateProject_forbidden() throws Exception {
        when(projectService.updateProject(eq(PROJECT_ID), any(ProjectUpdate.class), eq(AUTH_HEADER)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this project"));

        mockMvc.perform(put("/projects/{projectId}", PROJECT_ID)
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(testProjectUpdate)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(authorities = "USER")
    public void getProjectMembers_success() throws Exception {
        when(projectService.getProjectMembers(PROJECT_ID, AUTH_HEADER)).thenReturn(testMembers);

        mockMvc.perform(get("/projects/{projectId}/members", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(USER_ID))
                .andExpect(jsonPath("$[0].username").value("testuser1"))
                .andExpect(jsonPath("$[1].id").value("user-456"))
                .andExpect(jsonPath("$[1].username").value("testuser2"));
    }
    
    @Test
    @WithMockUser(authorities = "USER")
    public void makeUserLeaveFromProject_success() throws Exception {
        doNothing().when(projectService).makeUserLeaveFromProject(PROJECT_ID, AUTH_HEADER);

        mockMvc.perform(put("/projects/{projectId}/members", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isAccepted());
        
        verify(projectService, times(1)).makeUserLeaveFromProject(PROJECT_ID, AUTH_HEADER);
    }
    
    @Test
    @WithMockUser(authorities = "USER")
    public void deleteProject_success() throws Exception {
        doNothing().when(projectService).deleteProject(PROJECT_ID, AUTH_HEADER);

        mockMvc.perform(delete("/projects/{projectId}", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isNoContent());
        
        verify(projectService, times(1)).deleteProject(PROJECT_ID, AUTH_HEADER);
    }
    
    @Test
    @WithMockUser(authorities = "USER")
    public void deleteProjectChanges_success() throws Exception {
        doNothing().when(projectService).deleteProjectChanges(PROJECT_ID, AUTH_HEADER);

        mockMvc.perform(delete("/projects/{projectId}/changes", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isNoContent());
        
        verify(projectService, times(1)).deleteProjectChanges(PROJECT_ID, AUTH_HEADER);
    }
    
    @Test
    @WithMockUser(authorities = "USER")
    public void createMessage_success() throws Exception {
        Message savedMessage = testMessages.get(0);
        when(projectService.sendChatMessage(eq(PROJECT_ID), eq(AUTH_HEADER), any(MessageRegister.class)))
            .thenReturn(savedMessage);

        mockMvc.perform(post("/projects/{projectId}/messages", PROJECT_ID)
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(testMessageRegister)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("message-123"))
                .andExpect(jsonPath("$.content").value("Test message 1"))
                .andExpect(jsonPath("$.senderId").value(USER_ID));
    }
    
    @Test
    @WithMockUser(authorities = "USER")
    public void getMessages_success() throws Exception {
        when(projectService.getMessages(PROJECT_ID, AUTH_HEADER)).thenReturn(testMessages);

        mockMvc.perform(get("/projects/{projectId}/messages", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("message-123"))
                .andExpect(jsonPath("$[0].content").value("Test message 1"))
                .andExpect(jsonPath("$[0].senderId").value(USER_ID))
                .andExpect(jsonPath("$[1].id").value("message-456"))
                .andExpect(jsonPath("$[1].content").value("Test message 2"))
                .andExpect(jsonPath("$[1].senderId").value("user-456"));
    }
}
