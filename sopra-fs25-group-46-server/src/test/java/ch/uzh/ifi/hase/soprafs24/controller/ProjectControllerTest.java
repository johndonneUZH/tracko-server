package ch.uzh.ifi.hase.soprafs24.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.models.project.ProjectRegister;
import ch.uzh.ifi.hase.soprafs24.models.project.ProjectUpdate;
import ch.uzh.ifi.hase.soprafs24.service.ProjectAuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.ProjectService;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable Spring Security filters
@ActiveProfiles("test")
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

    @BeforeEach
    public void setup() {
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
        testProjectUpdate.setMembersToAdd(Arrays.asList("user-456"));
        testProjectUpdate.setMembersToRemove(new ArrayList<>());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void createProject_success() throws Exception {
        // given
        when(projectService.createProject(any(ProjectRegister.class), eq(AUTH_HEADER))).thenReturn(testProject);

        // when/then
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
        // given
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, AUTH_HEADER)).thenReturn(testProject);

        // when/then
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
        // given
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, AUTH_HEADER))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        // when/then
        mockMvc.perform(get("/projects/{projectId}", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void getProject_forbidden() throws Exception {
        // given
        when(projectAuthorizationService.authenticateProject(PROJECT_ID, AUTH_HEADER))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project"));

        // when/then
        mockMvc.perform(get("/projects/{projectId}", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void updateProject_success() throws Exception {
        // given
        Project updatedProject = new Project();
        updatedProject.setProjectId(PROJECT_ID);
        updatedProject.setProjectName("Updated Project");
        updatedProject.setProjectDescription("Updated Description");
        updatedProject.setOwnerId(USER_ID);
        updatedProject.setProjectMembers(Arrays.asList("user-456"));
        updatedProject.setCreatedAt(testProject.getCreatedAt());
        updatedProject.setUpdatedAt(LocalDateTime.now());

        when(projectService.updateProject(eq(PROJECT_ID), any(ProjectUpdate.class), eq(AUTH_HEADER))).thenReturn(updatedProject);

        // when/then
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
        // given
        when(projectService.updateProject(eq(PROJECT_ID), any(ProjectUpdate.class), eq(AUTH_HEADER)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this project"));

        // when/then
        mockMvc.perform(put("/projects/{projectId}", PROJECT_ID)
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(testProjectUpdate)))
                .andExpect(status().isForbidden());
    }
}