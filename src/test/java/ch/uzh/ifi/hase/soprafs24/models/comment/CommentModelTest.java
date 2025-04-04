package ch.uzh.ifi.hase.soprafs24.models.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class CommentModelTest {

    @Test
    public void testCommentModel() {
        // Create test data
        String commentId = "comment-123";
        String commentText = "Test comment";
        String ideaId = "idea-123";
        String ownerId = "user-123";
        String parentCommentId = "parent-comment-123";
        LocalDateTime createdAt = LocalDateTime.now();
        String projectId = "project-123";

        // Create and set Comment object
        Comment comment = new Comment();
        comment.setCommentId(commentId);
        comment.setCommentText(commentText);
        comment.setIdeaId(ideaId);
        comment.setOwnerId(ownerId);
        comment.setParentCommentId(parentCommentId);
        comment.setCreatedAt(createdAt);
        comment.setProjectId(projectId);

        // Verify getters return correct values
        assertEquals(commentId, comment.getCommentId());
        assertEquals(commentText, comment.getCommentText());
        assertEquals(ideaId, comment.getIdeaId());
        assertEquals(ownerId, comment.getOwnerId());
        assertEquals(parentCommentId, comment.getParentCommentId());
        assertEquals(createdAt, comment.getCreatedAt());
        assertEquals(projectId, comment.getProjectId());
    }

    @Test
    public void testCommentRegisterModel() {
        // Create test data
        String commentText = "Test comment";

        // Create and set CommentRegister object
        CommentRegister commentRegister = new CommentRegister();
        commentRegister.setCommentText(commentText);

        // Verify getters return correct values
        assertEquals(commentText, commentRegister.getCommentText());
    }

    @Test
    public void testCommentWithoutParent() {
        // Create test data for a root comment
        String commentId = "comment-123";
        String commentText = "Root comment";
        String ideaId = "idea-123";
        String ownerId = "user-123";
        LocalDateTime createdAt = LocalDateTime.now();
        String projectId = "project-123";

        // Create and set Comment object without parent
        Comment rootComment = new Comment();
        rootComment.setCommentId(commentId);
        rootComment.setCommentText(commentText);
        rootComment.setIdeaId(ideaId);
        rootComment.setOwnerId(ownerId);
        rootComment.setCreatedAt(createdAt);
        rootComment.setProjectId(projectId);
        // No parent set

        // Verify getters return correct values
        assertEquals(commentId, rootComment.getCommentId());
        assertEquals(commentText, rootComment.getCommentText());
        assertEquals(ideaId, rootComment.getIdeaId());
        assertEquals(ownerId, rootComment.getOwnerId());
        assertEquals(null, rootComment.getParentCommentId()); // Should be null
        assertEquals(createdAt, rootComment.getCreatedAt());
        assertEquals(projectId, rootComment.getProjectId());
    }
}