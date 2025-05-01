package ch.uzh.ifi.hase.soprafs24.models.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        // Create and set Comment object
        Comment comment = new Comment();
        comment.setCommentId(commentId);
        comment.setCommentText(commentText);
        comment.setIdeaId(ideaId);
        comment.setOwnerId(ownerId);

        // Verify getters return correct values
        assertEquals(commentId, comment.getCommentId());
        assertEquals(commentText, comment.getCommentText());
        assertEquals(ideaId, comment.getIdeaId());
        assertEquals(ownerId, comment.getOwnerId());
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
}