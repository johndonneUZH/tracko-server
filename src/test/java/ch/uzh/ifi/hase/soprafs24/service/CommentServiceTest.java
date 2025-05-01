package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;

import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;
import ch.uzh.ifi.hase.soprafs24.models.comment.Comment;
import ch.uzh.ifi.hase.soprafs24.models.comment.CommentRegister;
import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;
import ch.uzh.ifi.hase.soprafs24.repository.CommentRepository;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @MockBean
    private CommentRepository commentRepository;

    @MockBean
    private IdeaService ideaService;

    @MockBean
    private UserService userService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    private final String VALID_AUTH_HEADER = "Bearer valid-token";
    private final String PROJECT_ID = "project-123";
    private final String IDEA_ID = "idea-123";
    private final String COMMENT_ID = "comment-123";
    private final String USER_ID = "user-123";

    @BeforeEach
    public void setup() {
        // Mock authentication
        when(userService.getUserIdByToken(VALID_AUTH_HEADER)).thenReturn(USER_ID);

        // Mock idea authentication
        Idea idea = new Idea();
        idea.setIdeaId(IDEA_ID);
        idea.setProjectId(PROJECT_ID);
        when(ideaService.getIdeaById(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER)).thenReturn(idea);
    }

    @Test
    public void getCommentsByIdea_success() {
        // given
        Comment comment1 = createTestComment("comment-1", "Comment 1", null);
        Comment comment2 = createTestComment("comment-2", "Comment 2", null);
        List<Comment> comments = Arrays.asList(comment1, comment2);
        
        when(commentRepository.findByIdeaId(IDEA_ID)).thenReturn(comments);

        // when
        List<Comment> result = commentService.getCommentsByIdea(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER);

        // then
        assertEquals(2, result.size());
        assertEquals("comment-1", result.get(0).getCommentId());
        assertEquals("comment-2", result.get(1).getCommentId());
        verify(ideaService, times(1)).getIdeaById(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER);
    }

    @Test
    public void createComment_rootComment_success() {
        // given
        CommentRegister commentRegister = new CommentRegister();
        commentRegister.setCommentText("New comment");
        
        Comment createdComment = createTestComment(COMMENT_ID, "New comment", null);
        
        when(commentRepository.save(any(Comment.class))).thenReturn(createdComment);

        // when
        Comment result = commentService.createComment(PROJECT_ID, IDEA_ID, null, VALID_AUTH_HEADER, commentRegister);

        // then
        assertNotNull(result);
        assertEquals(COMMENT_ID, result.getCommentId());
        assertEquals("New comment", result.getCommentText());
        assertEquals(IDEA_ID, result.getIdeaId());;
        verify(ideaService, times(1)).getIdeaById(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    public void getCommentById_success() {
        // given
        String commentId = "comment-123";
        Comment expectedComment = createTestComment(commentId, "Test comment text", null);
        
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(expectedComment));
        
        // when
        Comment result = commentService.getCommentById(PROJECT_ID, IDEA_ID, commentId, VALID_AUTH_HEADER);
        
        // then
        assertNotNull(result);
        assertEquals(commentId, result.getCommentId());
        assertEquals("Test comment text", result.getCommentText());
        assertEquals(IDEA_ID, result.getIdeaId());
        verify(ideaService, times(1)).getIdeaById(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER);
    }

    private Comment createTestComment(String commentId, String commentText, String parentCommentId) {
        Comment comment = new Comment();
        comment.setCommentId(commentId);
        comment.setCommentText(commentText);
        comment.setIdeaId(IDEA_ID);

        return comment;
    }
}