package controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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

import auth.JwtUtil;
import controller.CommentController;
import models.comment.Comment;
import models.comment.CommentRegister;
import service.CommentService;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable Spring Security filters
@ActiveProfiles("test")
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CommentService commentService;

    private final String AUTH_HEADER = "Bearer valid-token";
    private final String PROJECT_ID = "project-123";
    private final String IDEA_ID = "idea-123";
    private final String COMMENT_ID = "comment-123";
    private final String USER_ID = "user-123";

    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @WithMockUser(authorities = "USER")
    public void getCommentsByIdeaId_success() throws Exception {
        Comment comment1 = createTestComment("comment-1", "Comment 1", USER_ID);
        Comment comment2 = createTestComment("comment-2", "Comment 2", USER_ID);
        List<Comment> comments = Arrays.asList(comment1, comment2);

        when(commentService.getCommentsByIdea(PROJECT_ID, IDEA_ID, AUTH_HEADER)).thenReturn(comments);

        mockMvc.perform(get("/projects/{projectId}/ideas/{ideaId}/comments", PROJECT_ID, IDEA_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].commentId").value("comment-1"))
                .andExpect(jsonPath("$[0].commentText").value("Comment 1"))
                .andExpect(jsonPath("$[1].commentId").value("comment-2"))
                .andExpect(jsonPath("$[1].commentText").value("Comment 2"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void createRootComment_success() throws Exception {
        CommentRegister commentRegister = new CommentRegister();
        commentRegister.setCommentText("New comment");

        Comment createdComment = createTestComment(COMMENT_ID, "New comment", USER_ID);

        when(commentService.createComment(eq(PROJECT_ID), eq(IDEA_ID), eq(null), eq(AUTH_HEADER), any(CommentRegister.class)))
                .thenReturn(createdComment);

        mockMvc.perform(post("/projects/{projectId}/ideas/{ideaId}/comments", PROJECT_ID, IDEA_ID)
            .header("Authorization", AUTH_HEADER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(commentRegister)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.commentId").value(COMMENT_ID))
            .andExpect(jsonPath("$.commentText").value("New comment"))
            .andExpect(jsonPath("$.ideaId").value(IDEA_ID))
            .andExpect(jsonPath("$.ownerId").value(USER_ID))
            .andExpect(jsonPath("$.replies").isArray());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void createNestedComment_success() throws Exception {
        CommentRegister commentRegister = new CommentRegister();
        commentRegister.setCommentText("Reply comment");

        Comment createdComment = createTestComment(COMMENT_ID+1, "Reply comment", USER_ID);

        when(commentService.createComment(eq(PROJECT_ID), eq(IDEA_ID), eq(COMMENT_ID), eq(AUTH_HEADER), any(CommentRegister.class)))
            .thenReturn(createdComment);

        mockMvc.perform(post("/projects/{projectId}/ideas/{ideaId}/comments/{commentId}", PROJECT_ID, IDEA_ID, COMMENT_ID)
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentRegister)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentId").value(COMMENT_ID+1))
                .andExpect(jsonPath("$.commentText").value("Reply comment"));    }

    @Test
    @WithMockUser(authorities = "USER")
    public void getCommentById_success() throws Exception {
        Comment comment = createTestComment(COMMENT_ID, "Test comment content", USER_ID);
        
        when(commentService.getCommentById(eq(PROJECT_ID), eq(IDEA_ID), eq(COMMENT_ID), eq(AUTH_HEADER)))
            .thenReturn(comment);

        mockMvc.perform(get("/projects/{projectId}/ideas/{ideaId}/comments/{commentId}", 
                    PROJECT_ID, IDEA_ID, COMMENT_ID)
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(COMMENT_ID))
                .andExpect(jsonPath("$.commentText").value("Test comment content"))
                .andExpect(jsonPath("$.ideaId").value(IDEA_ID))
                .andExpect(jsonPath("$.ownerId").value(USER_ID))
                .andExpect(jsonPath("$.replies").isArray());
    }
    private Comment createTestComment(String commentId, String commentText, String ownerId) {
        Comment comment = new Comment();
        comment.setCommentId(commentId);
        comment.setCommentText(commentText);
        comment.setIdeaId(IDEA_ID);
        comment.setOwnerId(ownerId);
        comment.setReplies(Collections.emptyList());

        return comment;
    }
}