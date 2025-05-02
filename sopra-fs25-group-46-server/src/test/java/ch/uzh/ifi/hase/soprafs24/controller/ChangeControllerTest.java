package ch.uzh.ifi.hase.soprafs24.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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

import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.constant.ChangeType;
import ch.uzh.ifi.hase.soprafs24.models.change.Change;
import ch.uzh.ifi.hase.soprafs24.models.change.ChangeRegister;
import ch.uzh.ifi.hase.soprafs24.service.ChangeService;

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
        // given
        Change change1 = createTestChange("change-1", ChangeType.ADDED_IDEA);
        Change change2 = createTestChange("change-2", ChangeType.CHANGED_PROJECT_SETTINGS);
        List<Change> changes = Arrays.asList(change1, change2);
    
        when(changeService.getChangesByProject(PROJECT_ID, AUTH_HEADER)).thenReturn(changes);
    
        // when/then
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
        // given
        ChangeRegister changeRegister = new ChangeRegister();
        changeRegister.setChangeType(ChangeType.ADDED_IDEA);
    
        Change createdChange = createTestChange("generated-change-id", ChangeType.ADDED_IDEA); // Simulate the generated ID
    
        when(changeService.createChange(eq(PROJECT_ID), any(ChangeRegister.class), eq(AUTH_HEADER)))
                .thenReturn(createdChange);
    
        // when/then
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