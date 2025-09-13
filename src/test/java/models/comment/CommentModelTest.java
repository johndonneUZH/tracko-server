package models.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import models.comment.Comment;
import models.comment.CommentRegister;

public class CommentModelTest {

    @Test
    public void testCommentModel() {
        String commentId = "comment-123";
        String commentText = "Test comment";
        String ideaId = "idea-123";
        String ownerId = "user-123";

        Comment comment = new Comment();
        comment.setCommentId(commentId);
        comment.setCommentText(commentText);
        comment.setIdeaId(ideaId);
        comment.setOwnerId(ownerId);

        assertEquals(commentId, comment.getCommentId());
        assertEquals(commentText, comment.getCommentText());
        assertEquals(ideaId, comment.getIdeaId());
        assertEquals(ownerId, comment.getOwnerId());
    }

    @Test
    public void testCommentRegisterModel() {
        String commentText = "Test comment";

        CommentRegister commentRegister = new CommentRegister();
        commentRegister.setCommentText(commentText);

        assertEquals(commentText, commentRegister.getCommentText());
    }
}