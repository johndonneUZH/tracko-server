package ch.uzh.ifi.hase.soprafs24.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.models.ai.AnthropicResponseDTO;
import ch.uzh.ifi.hase.soprafs24.models.ai.ContentDTO;
import ch.uzh.ifi.hase.soprafs24.models.ai.IdeaCombinationRequestDTO;
import ch.uzh.ifi.hase.soprafs24.models.ai.IdeaRefinementRequestDTO;
import ch.uzh.ifi.hase.soprafs24.models.ai.IdeaTwistRequestDTO;
import ch.uzh.ifi.hase.soprafs24.service.AnthropicService;

@WebMvcTest(AIController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable Spring Security filters
@ActiveProfiles("test")
public class AIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnthropicService anthropicService;

    @MockBean
    private JwtUtil jwtUtil;
 
    private ObjectMapper mapper = new ObjectMapper();
    private AnthropicResponseDTO mockResponse;

    @BeforeEach
    public void setup() {
        mockResponse = new AnthropicResponseDTO();
        ContentDTO content = new ContentDTO();
        content.setType("text");
        content.setText("AI generated response");
        List<ContentDTO> contentList = new ArrayList<>();
        contentList.add(content);
        mockResponse.setContent(contentList);
    }

    @Test
    public void refineIdea_success() throws Exception {
        IdeaRefinementRequestDTO request = new IdeaRefinementRequestDTO();
        request.setIdeaContent("My raw idea to refine");

        when(anthropicService.refineIdea(anyString())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/ai/refine")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("text"))
                .andExpect(jsonPath("$.content[0].text").value("AI generated response"));
    }

    @Test
    public void combineIdeas_success() throws Exception {
        IdeaCombinationRequestDTO request = new IdeaCombinationRequestDTO();
        request.setIdeaOne("First idea");
        request.setIdeaTwo("Second idea");

        when(anthropicService.combineIdeas(eq("First idea"), eq("Second idea"))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/ai/combine")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("text"))
                .andExpect(jsonPath("$.content[0].text").value("AI generated response"));
    }

    @Test
    public void generateFromTemplate_success() throws Exception {
        String template = "Create an idea about renewable energy";

        when(anthropicService.generateFromTemplate(eq(template))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/ai/template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(template))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("text"))
                .andExpect(jsonPath("$.content[0].text").value("AI generated response"));
    }

    @Test
    public void suggestWithTwist_success() throws Exception {
        IdeaTwistRequestDTO request = new IdeaTwistRequestDTO();
        request.setOriginalIdea("Original idea");
        request.setTwist("Make it eco-friendly");

        when(anthropicService.suggestRelatedIdea(eq("Original idea"), eq("Make it eco-friendly"))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/ai/twist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("text"))
                .andExpect(jsonPath("$.content[0].text").value("AI generated response"));
    }

    @Test
    public void generateContent_success() throws Exception {
        String prompt = "Generate creative content about space travel";

        when(anthropicService.generateContent(eq(prompt))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/ai/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(prompt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("text"))
                .andExpect(jsonPath("$.content[0].text").value("AI generated response"));
    }
}