package repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import config.MongoTestConfig;
import tracko.models.comment.Comment;
import tracko.repository.CommentRepository;

@SpringBootTest(classes = {MongoTestConfig.class})
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;
    
    @BeforeEach
    public void setup() {
        commentRepository.deleteAll();
    }
    
    @Test
    public void testMongoConnection() {
        Comment testComment = new Comment();
        testComment.setCommentText("This is a test comment");
        testComment.setIdeaId("idea-123");
        testComment.setOwnerId("user-123");
        
        Comment savedComment = commentRepository.save(testComment);
        
        assertNotNull(savedComment.getCommentId());
        
        Optional<Comment> retrievedComment = commentRepository.findById(savedComment.getCommentId());
        assertTrue(retrievedComment.isPresent());
        assertEquals("This is a test comment", retrievedComment.get().getCommentText());
    }

    @Test
    public void findByIdeaId_success() {
        String ideaId = "idea-" + System.currentTimeMillis();
        
        Comment comment1 = new Comment();
        comment1.setCommentText("Comment 1");
        comment1.setIdeaId(ideaId);
        comment1.setOwnerId("user-123");
        
        Comment comment2 = new Comment();
        comment2.setCommentText("Comment 2");
        comment2.setIdeaId(ideaId);
        comment2.setOwnerId("user-123");
        
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        List<Comment> foundComments = commentRepository.findByIdeaId(ideaId);

        assertEquals(2, foundComments.size());
        assertTrue(foundComments.stream().anyMatch(comment -> comment.getCommentText().equals("Comment 1")));
        assertTrue(foundComments.stream().anyMatch(comment -> comment.getCommentText().equals("Comment 2")));
    }
    
    @Test
    public void findByOwnerId_success() {
        String ownerId = "owner-" + System.currentTimeMillis();
        
        Comment comment1 = new Comment();
        comment1.setCommentText("Comment 1");
        comment1.setIdeaId("idea-123");
        comment1.setOwnerId(ownerId);
        
        Comment comment2 = new Comment();
        comment2.setCommentText("Comment 2");
        comment2.setIdeaId("idea-456");
        comment2.setOwnerId(ownerId);
        
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        List<Comment> foundComments = commentRepository.findByOwnerId(ownerId);

        assertEquals(2, foundComments.size());
        assertTrue(foundComments.stream().anyMatch(comment -> comment.getCommentText().equals("Comment 1")));
        assertTrue(foundComments.stream().anyMatch(comment -> comment.getCommentText().equals("Comment 2")));
    }
}