package controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import auth.JwtUtil;
import constant.ChangeType;
import controller.ChangeController;
import models.change.Change;
import models.change.ChangeRegister;
import service.ChangeService;
import service.ChangeService.Contributions;
import service.ChangeService.DailyContribution;

@WebMvcTest(ChangeController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable Spring Security filters
@ActiveProfiles("test")
public class ChangeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private ChangeService changeService;

    private final String AUTH_HEADER = "Bearer valid-token";
    private final String PROJECT_ID = "project-123";
    private final String USER_ID = "user-123";

    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @WithMockUser(authorities = "USER")
    public void getChangesByProject_success() throws Exception {
        Change change1 = createTestChange("change-1", ChangeType.ADDED_IDEA);
        Change change2 = createTestChange("change-2", ChangeType.CHANGED_PROJECT_SETTINGS);
        List<Change> changes = Arrays.asList(change1, change2);
    
        when(changeService.getChangesByProject(PROJECT_ID, AUTH_HEADER)).thenReturn(changes);
    
        mockMvc.perform(get("/projects/{projectId}/changes", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].changeId").value("change-1"))
                .andExpect(jsonPath("$[0].changeType").value("ADDED_IDEA"))
                .andExpect(jsonPath("$[0].changeDescription").value("Added an idea"))
                .andExpect(jsonPath("$[1].changeId").value("change-2"))
                .andExpect(jsonPath("$[1].changeType").value("CHANGED_PROJECT_SETTINGS"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void createChange_success() throws Exception {
        ChangeRegister changeRegister = new ChangeRegister();
        changeRegister.setChangeType(ChangeType.ADDED_IDEA);
    
        Change createdChange = createTestChange("generated-change-id", ChangeType.ADDED_IDEA); // Simulate the generated ID
    
        when(changeService.createChange(eq(PROJECT_ID), any(ChangeRegister.class), eq(AUTH_HEADER)))
                .thenReturn(createdChange);
    
        mockMvc.perform(post("/projects/{projectId}/changes", PROJECT_ID)
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(changeRegister)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.changeId").value("generated-change-id"))
                .andExpect(jsonPath("$.changeType").value("ADDED_IDEA"))
                .andExpect(jsonPath("$.changeDescription").value("Added an idea"))
                .andExpect(jsonPath("$.projectId").value(PROJECT_ID))
                .andExpect(jsonPath("$.ownerId").value(USER_ID));
    }
    
    @Test
    @WithMockUser(authorities = "USER")
    public void getDailyContributions_success() throws Exception {
        DailyContribution contribution1 = new DailyContribution(LocalDate.now().minusDays(1), 3L);
        DailyContribution contribution2 = new DailyContribution(LocalDate.now(), 5L);
        List<DailyContribution> contributions = Arrays.asList(contribution1, contribution2);
        
        when(changeService.getDailyContributions(eq(PROJECT_ID), eq(AUTH_HEADER), any())).thenReturn(contributions);
        
        mockMvc.perform(get("/projects/{projectId}/changes/daily-contributions", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").isNotEmpty())
                .andExpect(jsonPath("$[0].count").value(3))
                .andExpect(jsonPath("$[1].date").isNotEmpty())
                .andExpect(jsonPath("$[1].count").value(5));
    }
    
    @Test
    @WithMockUser(authorities = "USER")
    public void getDailyContributions_withDaysParameter_success() throws Exception {
        DailyContribution contribution1 = new DailyContribution(LocalDate.now().minusDays(1), 3L);
        DailyContribution contribution2 = new DailyContribution(LocalDate.now(), 5L);
        List<DailyContribution> contributions = Arrays.asList(contribution1, contribution2);
        
        when(changeService.getDailyContributions(eq(PROJECT_ID), eq(AUTH_HEADER), eq(7))).thenReturn(contributions);
        
        mockMvc.perform(get("/projects/{projectId}/changes/daily-contributions", PROJECT_ID)
                .param("days", "7")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").isNotEmpty())
                .andExpect(jsonPath("$[0].count").value(3))
                .andExpect(jsonPath("$[1].date").isNotEmpty())
                .andExpect(jsonPath("$[1].count").value(5));
    }
    
    @Test
    @WithMockUser(authorities = "USER")
    public void getAnalytics_success() throws Exception {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Contributions contrib1 = new Contributions(today, 3L, 2L, 1L, 5L, 0L, 7L, 1L, 2L);
        Contributions contrib2 = new Contributions(today.minusDays(1), 1L, 0L, 0L, 3L, 1L, 2L, 0L, 0L);
        List<Contributions> contributions = Arrays.asList(contrib1, contrib2);
        
        when(changeService.getAnalytics(eq(PROJECT_ID), eq(AUTH_HEADER), eq(90))).thenReturn(contributions);
        
        mockMvc.perform(get("/projects/{projectId}/changes/analytics", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").exists())
                .andExpect(jsonPath("$[0].addIdea").value(3))
                .andExpect(jsonPath("$[0].editIdea").value(2))
                .andExpect(jsonPath("$[0].closeIdea").value(1))
                .andExpect(jsonPath("$[0].addComment").value(5))
                .andExpect(jsonPath("$[0].deleteComment").value(0))
                .andExpect(jsonPath("$[0].upvote").value(7))
                .andExpect(jsonPath("$[0].downvote").value(1))
                .andExpect(jsonPath("$[0].settings").value(2))
                .andExpect(jsonPath("$[1].date").exists())
                .andExpect(jsonPath("$[1].addIdea").value(1));
    }
    
    @Test
    @WithMockUser(authorities = "USER")
    public void getAnalytics_withCustomDays_success() throws Exception {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Contributions contrib1 = new Contributions(today, 5L, 3L, 0L, 2L, 1L, 4L, 2L, 1L);
        List<Contributions> contributions = Arrays.asList(contrib1);
        
        when(changeService.getAnalytics(eq(PROJECT_ID), eq(AUTH_HEADER), eq(30))).thenReturn(contributions);
        
        mockMvc.perform(get("/projects/{projectId}/changes/analytics", PROJECT_ID)
                .param("days", "30")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").exists())
                .andExpect(jsonPath("$[0].addIdea").value(5))
                .andExpect(jsonPath("$[0].editIdea").value(3))
                .andExpect(jsonPath("$[0].closeIdea").value(0))
                .andExpect(jsonPath("$[0].addComment").value(2))
                .andExpect(jsonPath("$[0].deleteComment").value(1))
                .andExpect(jsonPath("$[0].upvote").value(4))
                .andExpect(jsonPath("$[0].downvote").value(2))
                .andExpect(jsonPath("$[0].settings").value(1));
    }
    
    @Test
    @WithMockUser(authorities = "USER")
    public void getAnalyticsByUserId_success() throws Exception {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Contributions contrib = new Contributions(today, 4L, 2L, 1L, 3L, 0L, 5L, 1L, 2L);
        List<Contributions> contributions = Arrays.asList(contrib);
        
        when(changeService.getAnalyticsByUserId(eq(PROJECT_ID), eq(USER_ID), eq(AUTH_HEADER), eq(180)))
                .thenReturn(contributions);
        
        mockMvc.perform(get("/projects/{projectId}/changes/analytics/{userId}", PROJECT_ID, USER_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").exists())
                .andExpect(jsonPath("$[0].addIdea").value(4))
                .andExpect(jsonPath("$[0].editIdea").value(2))
                .andExpect(jsonPath("$[0].closeIdea").value(1))
                .andExpect(jsonPath("$[0].addComment").value(3))
                .andExpect(jsonPath("$[0].deleteComment").value(0))
                .andExpect(jsonPath("$[0].upvote").value(5))
                .andExpect(jsonPath("$[0].downvote").value(1))
                .andExpect(jsonPath("$[0].settings").value(2));
    }
    
    @Test
    @WithMockUser(authorities = "USER")
    public void getAnalyticsByUserId_withCustomDays_success() throws Exception {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Contributions contrib = new Contributions(today, 3L, 1L, 0L, 4L, 1L, 2L, 0L, 1L);
        List<Contributions> contributions = Arrays.asList(contrib);
        
        when(changeService.getAnalyticsByUserId(eq(PROJECT_ID), eq(USER_ID), eq(AUTH_HEADER), eq(30)))
                .thenReturn(contributions);
        
        mockMvc.perform(get("/projects/{projectId}/changes/analytics/{userId}", PROJECT_ID, USER_ID)
                .param("days", "30")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").exists())
                .andExpect(jsonPath("$[0].addIdea").value(3))
                .andExpect(jsonPath("$[0].editIdea").value(1))
                .andExpect(jsonPath("$[0].closeIdea").value(0))
                .andExpect(jsonPath("$[0].addComment").value(4))
                .andExpect(jsonPath("$[0].deleteComment").value(1))
                .andExpect(jsonPath("$[0].upvote").value(2))
                .andExpect(jsonPath("$[0].downvote").value(0))
                .andExpect(jsonPath("$[0].settings").value(1));
    }

    private Change createTestChange(String changeId, ChangeType changeType) {
        Change change = new Change();
        change.setChangeId(changeId);
        change.setChangeType(changeType);
        change.setChangeDescription(changeType.getDescription());
        change.setProjectId(PROJECT_ID);
        change.setOwnerId(USER_ID);
        change.setCreatedAt(LocalDateTime.now());
        return change;
    }
}