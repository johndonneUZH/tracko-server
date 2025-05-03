package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.models.report.Report;
import ch.uzh.ifi.hase.soprafs24.models.report.ReportRegister;
import ch.uzh.ifi.hase.soprafs24.models.user.User;
import ch.uzh.ifi.hase.soprafs24.models.user.UserUpdate;
import ch.uzh.ifi.hase.soprafs24.service.ProjectAuthorizationService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.service.ReportService;
import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
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
        // Create test user
        testUser = new User();
        testUser.setId("1");
        testUser.setName("Test User");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setProjectIds(new ArrayList<>());

        // Create test user update
        testUserUpdate = new UserUpdate();
        testUserUpdate.setName("Updated User");
        testUserUpdate.setUsername("updateduser");

        // Create test report
        testReport = new Report();
        testReport.setReportId("report-1");
        testReport.setUserId("1");
        testReport.setReportContent("<h2>Test Report</h2><p>Test content</p>");
        testReport.setCreatedAt(LocalDateTime.now());

        // Create test report register
        testReportRegister = new ReportRegister();
        testReportRegister.setReportContent("<h2>Updated Report</h2><p>Updated content</p>");

        Project mockProject = new Project();
        when(projectAuthorizationService.authenticateProject(anyString(), anyString())).thenReturn(mockProject);
    }

    @Test
    public void getUsers_validRequest_success() throws Exception {
        // given
        List<User> allUsers = Arrays.asList(testUser);

        // Mock userService's getUsers method
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
        // given
        given(userService.getUserById(testUser.getId())).willReturn(testUser);

        // when/then
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
        // given
        String nonExistentId = "999";
        given(userService.getUserById(nonExistentId)).willReturn(null);

        // when/then
        MockHttpServletRequestBuilder getRequest = get("/users/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUser_validRequest_success() throws Exception {
        // given
        User updatedUser = new User();
        updatedUser.setId(testUser.getId());
        updatedUser.setName(testUserUpdate.getName());
        updatedUser.setUsername(testUserUpdate.getUsername());
        updatedUser.setEmail(testUser.getEmail());
        updatedUser.setStatus(testUser.getStatus());
        updatedUser.setProjectIds(testUser.getProjectIds());

        // Mock authentication and update
        doNothing().when(userService).authenticateUser(anyString(), anyString());
        given(userService.updateUser(eq(testUser.getId()), any(UserUpdate.class)))
            .willReturn(updatedUser);

        // when/then
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
        // given - authentication fails
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(userService).authenticateUser(anyString(), anyString());

        // when/then
        MockHttpServletRequestBuilder putRequest = put("/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer invalidToken")
                .content(asJsonString(testUserUpdate));

        mockMvc.perform(putRequest)
                .andExpect(status().isForbidden());

        // Verify updateUser was never called
        verify(userService, never()).updateUser(anyString(), any(UserUpdate.class));
    }

    @Test
    public void getUserProjects_validId_projectsFound() throws Exception {
        // given
        Project project = new Project();
        project.setProjectId("project-1");
        project.setProjectName("Test Project");
        project.setOwnerId(testUser.getId());
        
        List<Project> projects = Collections.singletonList(project);
        given(userService.getUserProjects(testUser.getId())).willReturn(projects);

        // when/then
        mockMvc.perform(get("/users/" + testUser.getId() + "/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].projectId", is(project.getProjectId())))
                .andExpect(jsonPath("$[0].projectName", is(project.getProjectName())));
    }

    @Test
    public void getUserProjects_invalidId_notFound() throws Exception {
        // given
        String nonExistentId = "999";
        given(userService.getUserProjects(nonExistentId)).willReturn(null);

        // when/then
        mockMvc.perform(get("/users/" + nonExistentId + "/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getUserFriends_validRequest_success() throws Exception {
        // given
        User friend = new User();
        friend.setId("2");
        friend.setName("Friend User");
        friend.setUsername("frienduser");
        
        List<User> friends = Collections.singletonList(friend);
        given(userService.getUserFriends(testUser.getId())).willReturn(friends);

        // when/then
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
        // given
        doNothing().when(userService).inviteFriend(testUser.getId(), "2", VALID_AUTH_HEADER);

        // when/then
        mockMvc.perform(post("/users/" + testUser.getId() + "/friends/invite/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isCreated());
        
        verify(userService, times(1)).inviteFriend(testUser.getId(), "2", VALID_AUTH_HEADER);
    }

    @Test
    public void acceptFriend_validRequest_success() throws Exception {
        // given
        doNothing().when(userService).acceptFriend(testUser.getId(), "2", VALID_AUTH_HEADER);

        // when/then
        mockMvc.perform(post("/users/" + testUser.getId() + "/friends/accept/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isCreated());
        
        verify(userService, times(1)).acceptFriend(testUser.getId(), "2", VALID_AUTH_HEADER);
    }

    @Test
    public void rejectFriend_validRequest_success() throws Exception {
        // given
        doNothing().when(userService).rejectFriend(testUser.getId(), "2", VALID_AUTH_HEADER);

        // when/then
        mockMvc.perform(post("/users/" + testUser.getId() + "/friends/reject/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isCreated());
        
        verify(userService, times(1)).rejectFriend(testUser.getId(), "2", VALID_AUTH_HEADER);
    }

    @Test
    public void removeFriend_validRequest_success() throws Exception {
        // given
        doNothing().when(userService).removeFriend(testUser.getId(), "2", VALID_AUTH_HEADER);

        // when/then
        mockMvc.perform(post("/users/" + testUser.getId() + "/friends/remove/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isCreated());
        
        verify(userService, times(1)).removeFriend(testUser.getId(), "2", VALID_AUTH_HEADER);
    }

    @Test
    public void cancelFriendRequest_validRequest_success() throws Exception {
        // given
        doNothing().when(userService).cancelFriendRequest(testUser.getId(), "2");

        // when/then
        mockMvc.perform(post("/users/" + testUser.getId() + "/friends/cancel/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isCreated());
        
        verify(userService, times(1)).cancelFriendRequest(testUser.getId(), "2");
    }

    @Test
    public void getUserReports_validRequest_success() throws Exception {
        // given
        List<Report> reports = Collections.singletonList(testReport);
        doNothing().when(userService).authenticateUser(testUser.getId(), VALID_AUTH_HEADER);
        given(reportService.getReportsByUserId(testUser.getId())).willReturn(reports);

        // when/then
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
        // given
        doNothing().when(userService).authenticateUser(testUser.getId(), VALID_AUTH_HEADER);
        given(reportService.getReportsByUserId(testUser.getId())).willReturn(null);

        // when/then
        mockMvc.perform(get("/users/" + testUser.getId() + "/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", VALID_AUTH_HEADER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void updateReport_validRequest_success() throws Exception {
        // given
        doNothing().when(userService).authenticateUser(testUser.getId(), VALID_AUTH_HEADER);
        doNothing().when(reportService).updateReport(any(ReportRegister.class), eq(testReport.getReportId()));

        // when/then
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
        // given
        doNothing().when(userService).authenticateUser(testUser.getId(), VALID_AUTH_HEADER);
        given(reportService.getReportById(testReport.getReportId())).willReturn(testReport);

        // when/then
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
        // given
        doNothing().when(userService).authenticateUser(testUser.getId(), VALID_AUTH_HEADER);
        given(reportService.getReportById(testReport.getReportId())).willReturn(null);

        // when/then
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