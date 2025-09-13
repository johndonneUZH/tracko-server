package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import auth.JwtUtil;
import config.MongoTestConfig;
import repository.CommentRepository;
import constant.ChangeType;
import models.comment.Comment;
import models.comment.CommentRegister;
import models.idea.Idea;
import models.project.Project;
import service.ChangeService;
import service.CommentService;
import service.IdeaService;
import service.ProjectAuthorizationService;
import service.UserService;

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
    private JwtUtil jwtUtil;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private ProjectAuthorizationService projectAuthorizationService;
    
    @MockBean
    private ChangeService changeService;

    @Captor
    private ArgumentCaptor<Comment> commentCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> payloadCaptor;

    private final String VALID_AUTH_HEADER = "Bearer valid-token";
    private final String PROJECT_ID = "project-123";
    private final String IDEA_ID = "idea-123";
    private final String COMMENT_ID = "comment-123";
    private final String USER_ID = "user-123";

    @BeforeEach
    public void setup() {
        when(userService.getUserIdByToken(VALID_AUTH_HEADER)).thenReturn(USER_ID);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(USER_ID);
        Project mockProject = new Project();
        when(projectAuthorizationService.authenticateProject(anyString(), anyString())).thenReturn(mockProject);

        Idea idea = new Idea();
        idea.setIdeaId(IDEA_ID);
        idea.setProjectId(PROJECT_ID);
        when(ideaService.getIdeaById(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER)).thenReturn(idea);
    }

    @Test
    public void getCommentsByIdea_success() {
        Comment comment1 = createTestComment("comment-1", "Comment 1", null);
        Comment comment2 = createTestComment("comment-2", "Comment 2", null);
        List<Comment> comments = Arrays.asList(comment1, comment2);
        
        when(commentRepository.findByIdeaId(IDEA_ID)).thenReturn(comments);

        List<Comment> result = commentService.getCommentsByIdea(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER);

        assertEquals(2, result.size());
        assertEquals("comment-1", result.get(0).getCommentId());
        assertEquals("comment-2", result.get(1).getCommentId());
        verify(ideaService, times(1)).getIdeaById(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER);
    }

    @Test
    public void getCommentsByIdeaId_success() {
        Comment comment1 = createTestComment("comment-1", "Comment 1", null);
        Comment comment2 = createTestComment("comment-2", "Comment 2", null);
        List<Comment> comments = Arrays.asList(comment1, comment2);
        
        when(commentRepository.findByIdeaId(IDEA_ID)).thenReturn(comments);

        List<Comment> result = commentService.getCommentsByIdeaId(IDEA_ID);

        assertEquals(2, result.size());
        assertEquals("comment-1", result.get(0).getCommentId());
        assertEquals("comment-2", result.get(1).getCommentId());
        verify(commentRepository, times(1)).findByIdeaId(IDEA_ID);
    }

    @Test
    public void createComment_rootComment_success() {

        CommentRegister commentRegister = new CommentRegister();
        commentRegister.setCommentText("New comment");
        
        Comment createdComment = createTestComment(COMMENT_ID, "New comment", null);
        
        when(commentRepository.save(any(Comment.class))).thenReturn(createdComment);


        Comment result = commentService.createComment(PROJECT_ID, IDEA_ID, null, VALID_AUTH_HEADER, commentRegister);


        assertNotNull(result);
        assertEquals(COMMENT_ID, result.getCommentId());
        assertEquals("New comment", result.getCommentText());
        assertEquals(IDEA_ID, result.getIdeaId());
        assertEquals(USER_ID, result.getOwnerId());
        

        verify(commentRepository, times(1)).save(commentCaptor.capture());
        Comment capturedComment = commentCaptor.getValue();
        assertNotNull(capturedComment.getCreatedAt());
        

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/comments/" + IDEA_ID), payloadCaptor.capture());
        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals(createdComment, payload.get("comment"));
        

        verify(changeService, times(1)).markChange(PROJECT_ID, ChangeType.ADDED_COMMENT, VALID_AUTH_HEADER, false, null);
    }
    
    @Test
    public void createComment_replyComment_success() {

        String parentId = "parent-123";
        CommentRegister commentRegister = new CommentRegister();
        commentRegister.setCommentText("Reply comment");
        
        Comment createdComment = createTestComment(COMMENT_ID, "Reply comment", null);
        Comment parentComment = createTestComment(parentId, "Parent comment", null);
        parentComment.setReplies(new ArrayList<>());
        
        when(commentRepository.save(any(Comment.class))).thenReturn(createdComment);
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(parentComment));


        Comment result = commentService.createComment(PROJECT_ID, IDEA_ID, parentId, VALID_AUTH_HEADER, commentRegister);

        assertNotNull(result);
        

        verify(commentRepository, times(2)).save(commentCaptor.capture());
        List<Comment> capturedComments = commentCaptor.getAllValues();

        Comment newCommentCapture = capturedComments.get(0);
        assertNotNull(newCommentCapture.getCreatedAt());
        assertEquals(IDEA_ID, newCommentCapture.getIdeaId());

        Comment updatedParent = capturedComments.get(1);
        assertEquals(parentId, updatedParent.getCommentId());
        assertEquals(1, updatedParent.getReplies().size());
        assertEquals(COMMENT_ID, updatedParent.getReplies().get(0));
        

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/comments/" + IDEA_ID), payloadCaptor.capture());
        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals(createdComment, payload.get("comment"));
        assertEquals(parentId, payload.get("parentId"));
    }

    @Test
    public void createComment_replyToNonExistentParent_throwsNotFound() {

        String nonExistentParentId = "nonexistent-parent";
        CommentRegister commentRegister = new CommentRegister();
        commentRegister.setCommentText("Reply comment");
        
        when(commentRepository.findById(nonExistentParentId)).thenReturn(Optional.empty());


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            commentService.createComment(PROJECT_ID, IDEA_ID, nonExistentParentId, VALID_AUTH_HEADER, commentRegister);
        });
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Parent comment not found", exception.getReason());
    }

    @Test
    public void getCommentById_success() {

        String commentId = "comment-123";
        Comment expectedComment = createTestComment(commentId, "Test comment text", null);
        
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(expectedComment));

        Comment result = commentService.getCommentById(PROJECT_ID, IDEA_ID, commentId, VALID_AUTH_HEADER);
        
        assertNotNull(result);
        assertEquals(commentId, result.getCommentId());
        assertEquals("Test comment text", result.getCommentText());
        assertEquals(IDEA_ID, result.getIdeaId());
        verify(ideaService, times(1)).getIdeaById(PROJECT_ID, IDEA_ID, VALID_AUTH_HEADER);
    }
    
    @Test
    public void getCommentById_notFound_throwsException() {
        String nonExistentCommentId = "nonexistent-comment";
        when(commentRepository.findById(nonExistentCommentId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            commentService.getCommentById(PROJECT_ID, IDEA_ID, nonExistentCommentId, VALID_AUTH_HEADER);
        });
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Comment not found", exception.getReason());
    }
    
    @Test
    public void deleteComment_success() {

        Comment comment = createTestComment(COMMENT_ID, "Comment to delete", null);
        comment.setOwnerId(USER_ID);
        comment.setReplies(new ArrayList<>());
        comment.setProjectId(PROJECT_ID);
        
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));
        
        commentService.deleteComment(COMMENT_ID, VALID_AUTH_HEADER);

        verify(commentRepository, times(1)).delete(comment);
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/comments/" + IDEA_ID), 
            eq(Map.of("deletedId", COMMENT_ID))
        );
        verify(changeService, times(1)).markChange(PROJECT_ID, ChangeType.DELETED_COMMENT, VALID_AUTH_HEADER, false, null);
    }
    
    @Test
    public void deleteComment_withReplies_deletesAllReplies() {

        Comment comment = createTestComment(COMMENT_ID, "Comment with replies", null);
        comment.setOwnerId(USER_ID);
        comment.setProjectId(PROJECT_ID);
        

        List<String> replies = new ArrayList<>();
        replies.add("reply-1");
        replies.add("reply-2");
        comment.setReplies(replies);
        
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));
        

        commentService.deleteComment(COMMENT_ID, VALID_AUTH_HEADER);
        

        verify(commentRepository, times(1)).deleteById("reply-1");
        verify(commentRepository, times(1)).deleteById("reply-2");
        verify(commentRepository, times(1)).delete(comment);
    }
    
    @Test
    public void deleteComment_notOwner_throwsForbidden() {

        Comment comment = createTestComment(COMMENT_ID, "Comment by different user", null);
        comment.setOwnerId("different-user-id");
        
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            commentService.deleteComment(COMMENT_ID, VALID_AUTH_HEADER);
        });
        
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("You are not the owner of this comment", exception.getReason());
    }

    private Comment createTestComment(String commentId, String commentText, String parentCommentId) {
        Comment comment = new Comment();
        comment.setCommentId(commentId);
        comment.setCommentText(commentText);
        comment.setIdeaId(IDEA_ID);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setOwnerId(USER_ID);

        return comment;
    }
}