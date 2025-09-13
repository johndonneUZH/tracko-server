package controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import models.idea.Idea;
import models.idea.IdeaRegister;
import models.idea.IdeaUpdate;
import service.IdeaService;
import auth.JwtUtil;
import controller.IdeaController;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;


@WebMvcTest(IdeaController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable Spring Security filters
@ActiveProfiles("test")
public class IdeaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private IdeaService ideaService;

    private final String PROJECT_ID = "project-123";
    private final String IDEA_ID = "idea-123";
    private final String AUTH_HEADER = "Bearer valid-token";

    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @WithMockUser(authorities = "USER")
    public void createIdea_success() throws Exception {
        IdeaRegister ideaRegister = new IdeaRegister();
        ideaRegister.setIdeaName("Test Idea");
        ideaRegister.setIdeaDescription("Test Description");

        Idea createdIdea = new Idea();
        createdIdea.setIdeaId(IDEA_ID);
        createdIdea.setIdeaName("Test Idea");
        createdIdea.setIdeaDescription("Test Description");
        createdIdea.setProjectId(PROJECT_ID);
        createdIdea.setUpVotes(Collections.emptyList());
        createdIdea.setDownVotes(Collections.emptyList());


        when(ideaService.createIdea(eq(PROJECT_ID), any(IdeaRegister.class), eq(AUTH_HEADER), any())).thenReturn(createdIdea);

        mockMvc.perform(post("/projects/{projectId}/ideas", PROJECT_ID)
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(ideaRegister)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ideaId").value(IDEA_ID))
                .andExpect(jsonPath("$.ideaName").value("Test Idea"))
                .andExpect(jsonPath("$.ideaDescription").value("Test Description"))
                .andExpect(jsonPath("$.projectId").value(PROJECT_ID))
                .andExpect(jsonPath("$.upVotes").isArray())
                .andExpect(jsonPath("$.downVotes").isArray());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void getIdeasByProject_success() throws Exception {
        Idea idea1 = new Idea();
        idea1.setIdeaId("idea-1");
        idea1.setIdeaName("Idea 1");
        idea1.setProjectId(PROJECT_ID);
        
        Idea idea2 = new Idea();
        idea2.setIdeaId("idea-2");
        idea2.setIdeaName("Idea 2");
        idea2.setProjectId(PROJECT_ID);
        
        List<Idea> ideas = Arrays.asList(idea1, idea2);
        
        when(ideaService.getIdeasByProject(PROJECT_ID, AUTH_HEADER)).thenReturn(ideas);

        mockMvc.perform(get("/projects/{projectId}/ideas", PROJECT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ideaId").value("idea-1"))
                .andExpect(jsonPath("$[0].ideaName").value("Idea 1"))
                .andExpect(jsonPath("$[1].ideaId").value("idea-2"))
                .andExpect(jsonPath("$[1].ideaName").value("Idea 2"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void getIdeaById_success() throws Exception {
        Idea idea = new Idea();
        idea.setIdeaId(IDEA_ID);
        idea.setIdeaName("Test Idea");
        idea.setIdeaDescription("Test Description");
        idea.setProjectId(PROJECT_ID);

        
        when(ideaService.getIdeaById(PROJECT_ID, IDEA_ID, AUTH_HEADER)).thenReturn(idea);

        mockMvc.perform(get("/projects/{projectId}/ideas/{ideaId}", PROJECT_ID, IDEA_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ideaId").value(IDEA_ID))
                .andExpect(jsonPath("$.ideaName").value("Test Idea"))
                .andExpect(jsonPath("$.ideaDescription").value("Test Description"))
                .andExpect(jsonPath("$.projectId").value(PROJECT_ID));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void getIdeaById_notFound() throws Exception {
        when(ideaService.getIdeaById(PROJECT_ID, IDEA_ID, AUTH_HEADER))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));

        mockMvc.perform(get("/projects/{projectId}/ideas/{ideaId}", PROJECT_ID, IDEA_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void updateIdea_success() throws Exception {
        IdeaUpdate ideaUpdate = new IdeaUpdate();
        ideaUpdate.setIdeaName("Updated Name");
        ideaUpdate.setIdeaDescription("Updated Description");

        
        Idea updatedIdea = new Idea();
        updatedIdea.setIdeaId(IDEA_ID);
        updatedIdea.setIdeaName("Updated Name");
        updatedIdea.setIdeaDescription("Updated Description");
        updatedIdea.setProjectId(PROJECT_ID);

        
        when(ideaService.updateIdea(eq(PROJECT_ID), eq(IDEA_ID), any(IdeaUpdate.class), eq(AUTH_HEADER)))
            .thenReturn(updatedIdea);

        mockMvc.perform(put("/projects/{projectId}/ideas/{ideaId}", PROJECT_ID, IDEA_ID)
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(ideaUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ideaId").value(IDEA_ID))
                .andExpect(jsonPath("$.ideaName").value("Updated Name"))
                .andExpect(jsonPath("$.ideaDescription").value("Updated Description"))
                .andExpect(jsonPath("$.projectId").value(PROJECT_ID));
            }

    @Test
    @WithMockUser(authorities = "USER")
    public void updateIdea_forbidden() throws Exception {
        IdeaUpdate ideaUpdate = new IdeaUpdate();
        ideaUpdate.setIdeaName("Updated Name");
        
        when(ideaService.updateIdea(eq(PROJECT_ID), eq(IDEA_ID), any(IdeaUpdate.class), eq(AUTH_HEADER)))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this idea"));

        mockMvc.perform(put("/projects/{projectId}/ideas/{ideaId}", PROJECT_ID, IDEA_ID)
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(ideaUpdate)))
                .andExpect(status().isForbidden());
    }
}