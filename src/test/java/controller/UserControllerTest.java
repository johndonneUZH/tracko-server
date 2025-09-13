package controller;

import constant.UserStatus;
import controller.UserController;
import models.project.Project;
import models.report.Report;
import models.report.ReportRegister;
import models.user.User;
import models.user.UserUpdate;
import service.ProjectAuthorizationService;
import service.ReportService;
import service.UserService;
import auth.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable Spring Security filters
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ReportService reportService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private ProjectAuthorizationService projectAuthorizationService;

    private User testUser;
    private UserUpdate testUserUpdate;
    private Report testReport;
    private ReportRegister testReportRegister;
    private final String VALID_AUTH_HEADER = "Bearer validToken";

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId("1");
        testUser.setName("Test User");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setProjectIds(new ArrayList<>());

        testUserUpdate = new UserUpdate();
        testUserUpdate.setName("Updated User");
        testUserUpdate.setUsername("updateduser");

        testReport = new Report();
        testReport.setReportId("report-1");
        testReport.setUserId("1");
        testReport.setReportContent("<h2>Test Report</h2><p>Test content</p>");
        testReport.setCreatedAt(LocalDateTime.now());

        testReportRegister = new ReportRegister();
        testReportRegister.setReportContent("<h2>Updated Report</h2><p>Updated content</p>");

        Project mockProject = new Project();
        when(projectAuthorizationService.authenticateProject(anyString(), anyString())).thenReturn(mockProject);
    }

    @Test
    public void getUsers_validRequest_success() throws Exception {
        List<User> allUsers = Arrays.asList(testUser);

        given(userService.getUsers()).willReturn(allUsers);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testUser.getId())))
                .andExpect(jsonPath("$[0].name", is(testUser.getName())))
                .andExpect(jsonPath("$[0].username", is(testUser.getUsername())));
    }

    @Test
    public void getUser_validId_userFound() throws Exception {
        given(userService.getUserById(testUser.getId())).willReturn(testUser);

        MockHttpServletRequestBuilder getRequest = get("/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId())))
                .andExpect(jsonPath("$.name", is(testUser.getName())))
                .andExpect(jsonPath("$.username", is(testUser.getUsername())));
    }

    @Test
    public void getUser_invalidId_notFound() throws Exception {
        String nonExistentId = "999";
        given(userService.getUserById(nonExistentId)).willReturn(null);

        MockHttpServletRequestBuilder getRequest = get("/users/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUser_validRequest_success() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(testUser.getId());
        updatedUser.setName(testUserUpdate.getName());
        updatedUser.setUsername(testUserUpdate.getUsername());
        updatedUser.setEmail(testUser.getEmail());
        updatedUser.setStatus(testUser.getStatus());
        updatedUser.setProjectIds(testUser.getProjectIds());

        doNothing().when(userService).authenticateUser(anyString(), anyString());
        given(userService.updateUser(eq(testUser.getId()), any(UserUpdate.class)))
            .willReturn(updatedUser);

        MockHttpServletRequestBuilder putRequest = put("/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER)
                .content(asJsonString(testUserUpdate));

        mockMvc.perform(putRequest)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(updatedUser.getId())))
                .andExpect(jsonPath("$.name", is(updatedUser.getName())))
                .andExpect(jsonPath("$.username", is(updatedUser.getUsername())));
    }

    @Test
    public void updateUser_authenticationFailed_forbidden() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(userService).authenticateUser(anyString(), anyString());

        MockHttpServletRequestBuilder putRequest = put("/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer invalidToken")
                .content(asJsonString(testUserUpdate));

        mockMvc.perform(putRequest)
                .andExpect(status().isForbidden());

        verify(userService, never()).updateUser(anyString(), any(UserUpdate.class));
    }

    @Test
    public void getUserProjects_validId_projectsFound() throws Exception {
        Project project = new Project();
        project.setProjectId("project-1");
        project.setProjectName("Test Project");
        project.setOwnerId(testUser.getId());
        
        List<Project> projects = Collections.singletonList(project);
        given(userService.getUserProjects(testUser.getId())).willReturn(projects);

        mockMvc.perform(get("/users/" + testUser.getId() + "/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].projectId", is(project.getProjectId())))
                .andExpect(jsonPath("$[0].projectName", is(project.getProjectName())));
    }

    @Test
    public void getUserProjects_invalidId_notFound() throws Exception {
        String nonExistentId = "999";
        given(userService.getUserProjects(nonExistentId)).willReturn(null);

        mockMvc.perform(get("/users/" + nonExistentId + "/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getUserFriends_validRequest_success() throws Exception {
        User friend = new User();
        friend.setId("2");
        friend.setName("Friend User");
        friend.setUsername("frienduser");
        
        List<User> friends = Collections.singletonList(friend);
        given(userService.getUserFriends(testUser.getId())).willReturn(friends);

        mockMvc.perform(get("/users/" + testUser.getId() + "/friends")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(friend.getId())))
                .andExpect(jsonPath("$[0].name", is(friend.getName())));
    }

    @Test
    public void inviteFriend_validRequest_success() throws Exception {
        doNothing().when(userService).inviteFriend(testUser.getId(), "2", VALID_AUTH_HEADER);

        mockMvc.perform(post("/users/" + testUser.getId() + "/friends/invite/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isCreated());
        
        verify(userService, times(1)).inviteFriend(testUser.getId(), "2", VALID_AUTH_HEADER);
    }

    @Test
    public void acceptFriend_validRequest_success() throws Exception {
        doNothing().when(userService).acceptFriend(testUser.getId(), "2", VALID_AUTH_HEADER);

        mockMvc.perform(post("/users/" + testUser.getId() + "/friends/accept/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isCreated());
        
        verify(userService, times(1)).acceptFriend(testUser.getId(), "2", VALID_AUTH_HEADER);
    }

    @Test
    public void rejectFriend_validRequest_success() throws Exception {
        doNothing().when(userService).rejectFriend(testUser.getId(), "2", VALID_AUTH_HEADER);

        mockMvc.perform(post("/users/" + testUser.getId() + "/friends/reject/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isCreated());
        
        verify(userService, times(1)).rejectFriend(testUser.getId(), "2", VALID_AUTH_HEADER);
    }

    @Test
    public void removeFriend_validRequest_success() throws Exception {
        doNothing().when(userService).removeFriend(testUser.getId(), "2", VALID_AUTH_HEADER);

        mockMvc.perform(post("/users/" + testUser.getId() + "/friends/remove/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isCreated());
        
        verify(userService, times(1)).removeFriend(testUser.getId(), "2", VALID_AUTH_HEADER);
    }

    @Test
    public void cancelFriendRequest_validRequest_success() throws Exception {
        doNothing().when(userService).cancelFriendRequest(testUser.getId(), "2");

        mockMvc.perform(post("/users/" + testUser.getId() + "/friends/cancel/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isCreated());
        
        verify(userService, times(1)).cancelFriendRequest(testUser.getId(), "2");
    }

    @Test
    public void getUserReports_validRequest_success() throws Exception {
        List<Report> reports = Collections.singletonList(testReport);
        doNothing().when(userService).authenticateUser(testUser.getId(), VALID_AUTH_HEADER);
        given(reportService.getReportsByUserId(testUser.getId())).willReturn(reports);

        mockMvc.perform(get("/users/" + testUser.getId() + "/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].reportId", is(testReport.getReportId())))
                .andExpect(jsonPath("$[0].userId", is(testReport.getUserId())))
                .andExpect(jsonPath("$[0].reportContent", is(testReport.getReportContent())));
    }

    @Test
    public void getUserReports_notFound() throws Exception {
        doNothing().when(userService).authenticateUser(testUser.getId(), VALID_AUTH_HEADER);
        given(reportService.getReportsByUserId(testUser.getId())).willReturn(null);

        mockMvc.perform(get("/users/" + testUser.getId() + "/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void updateReport_validRequest_success() throws Exception {
        doNothing().when(userService).authenticateUser(testUser.getId(), VALID_AUTH_HEADER);
        doNothing().when(reportService).updateReport(any(ReportRegister.class), eq(testReport.getReportId()));

        mockMvc.perform(put("/users/" + testUser.getId() + "/reports/" + testReport.getReportId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER)
                .content(asJsonString(testReportRegister)))
                .andExpect(status().isAccepted());
        
        verify(userService, times(1)).authenticateUser(testUser.getId(), VALID_AUTH_HEADER);
        verify(reportService, times(1)).updateReport(any(ReportRegister.class), eq(testReport.getReportId()));
    }

    @Test
    public void getReport_validRequest_success() throws Exception {
        doNothing().when(userService).authenticateUser(testUser.getId(), VALID_AUTH_HEADER);
        given(reportService.getReportById(testReport.getReportId())).willReturn(testReport);

        mockMvc.perform(get("/users/" + testUser.getId() + "/reports/" + testReport.getReportId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId", is(testReport.getReportId())))
                .andExpect(jsonPath("$.userId", is(testReport.getUserId())))
                .andExpect(jsonPath("$.reportContent", is(testReport.getReportContent())));
    }

    @Test
    public void getReport_notFound() throws Exception {
        doNothing().when(userService).authenticateUser(testUser.getId(), VALID_AUTH_HEADER);
        given(reportService.getReportById(testReport.getReportId())).willReturn(null);

        mockMvc.perform(get("/users/" + testUser.getId() + "/reports/" + testReport.getReportId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isNotFound());
    }

    /**
     * Helper Method to convert objects into a JSON string
     * 
     * @param object
     * @return string
     */
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}