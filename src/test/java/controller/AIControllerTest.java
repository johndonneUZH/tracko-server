package controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import tracko.auth.JwtUtil;
import java.util.Map;
import java.util.HashMap;
import tracko.service.AIService;
import org.springframework.test.context.ContextConfiguration;

@WebMvcTest(controllers = tracko.controller.AIController.class)
@ContextConfiguration(classes = tracko.Application.class)
@AutoConfigureMockMvc(addFilters = false)
public class AIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIService aiService;

    @MockBean
    private JwtUtil jwtUtil;
 
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void refineIdea_success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("ideaContent", "My raw idea to refine");

        when(aiService.refineIdea(anyString())).thenReturn("AI generated refined idea");

        mockMvc.perform(post("/api/ai/refine")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("AI generated refined idea"));
    }

    @Test
    public void combineIdeas_success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("ideaOne", "First idea");
        request.put("ideaTwo", "Second idea");

        when(aiService.combineIdeas(eq("First idea"), eq("Second idea"))).thenReturn("AI generated combined idea");

        mockMvc.perform(post("/api/ai/combine")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("AI generated combined idea"));
    }

    @Test
    public void generateFromTemplate_success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("ideaContent", "Create an idea about renewable energy");

        when(aiService.generateFromTemplate(eq("Create an idea about renewable energy"))).thenReturn("AI generated template response");

        mockMvc.perform(post("/api/ai/template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("AI generated template response"));
    }

    @Test
    public void suggestWithTwist_success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("ideaContent", "Original idea");
        request.put("ideaTwist", "Make it eco-friendly");

        when(aiService.suggestRelatedIdea(eq("Original idea"), eq("Make it eco-friendly"))).thenReturn("AI generated twisted idea");

        mockMvc.perform(post("/api/ai/twist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("AI generated twisted idea"));
    }

}
