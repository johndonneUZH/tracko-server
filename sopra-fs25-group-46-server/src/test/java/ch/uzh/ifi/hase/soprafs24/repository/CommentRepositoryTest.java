package ch.uzh.ifi.hase.soprafs24.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;
import ch.uzh.ifi.hase.soprafs24.models.comment.Comment;

@SpringBootTest
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;
    
    @BeforeEach
    public void setup() {
        // Clear the test database before each test
        commentRepository.deleteAll();
    }
    
    @Test
    public void testMongoConnection() {
        // Create a test comment
        Comment testComment = new Comment();
        testComment.setCommentText("This is a test comment");
        testComment.setIdeaId("idea-123");
        testComment.setOwnerId("user-123");
        // testComment.setCreatedAt(LocalDateTime.now());
        // testComment.setProjectId("project-123");
        
        // Save to repository
        Comment savedComment = commentRepository.save(testComment);
        
        // Verify the comment was saved
        assertNotNull(savedComment.getCommentId());
        
        // Retrieve and verify
        Optional<Comment> retrievedComment = commentRepository.findById(savedComment.getCommentId());
        assertTrue(retrievedComment.isPresent());
        assertEquals("This is a test comment", retrievedComment.get().getCommentText());
    }

    @Test
    public void findByIdeaId_success() {
        // given
        String ideaId = "idea-" + System.currentTimeMillis();
        
        Comment comment1 = new Comment();
        comment1.setCommentText("Comment 1");
        comment1.setIdeaId(ideaId);
        comment1.setOwnerId("user-123");
        // comment1.setCreatedAt(LocalDateTime.now());
        // comment1.setProjectId("project-123");
        
        Comment comment2 = new Comment();
        comment2.setCommentText("Comment 2");
        comment2.setIdeaId(ideaId);
        comment2.setOwnerId("user-123");
        // comment2.setCreatedAt(LocalDateTime.now());
        // comment2.setProjectId("project-123");
        
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        // when
        List<Comment> foundComments = commentRepository.findByIdeaId(ideaId);

        // then
        assertEquals(2, foundComments.size());
        assertTrue(foundComments.stream().anyMatch(comment -> comment.getCommentText().equals("Comment 1")));
        assertTrue(foundComments.stream().anyMatch(comment -> comment.getCommentText().equals("Comment 2")));
    }
    
    // @Test
    // public void findByParentCommentId_success() {
    //     // given
    //     String parentCommentId = "parent-" + System.currentTimeMillis();
        
    //     Comment reply1 = new Comment();
    //     reply1.setCommentText("Reply 1");
    //     reply1.setIdeaId("idea-123");
    //     reply1.setOwnerId("user-123");
    //     // reply1.setCreatedAt(LocalDateTime.now());
    //     // reply1.setProjectId("project-123");
    //     // reply1.setParentCommentId(parentCommentId);
        
    //     Comment reply2 = new Comment();
    //     reply2.setCommentText("Reply 2");
    //     reply2.setIdeaId("idea-123");
    //     reply2.setOwnerId("user-123");
    //     // reply2.setCreatedAt(LocalDateTime.now());
    //     // reply2.setProjectId("project-123");
    //     // reply2.setParentCommentId(parentCommentId);
        
    //     commentRepository.save(reply1);
    //     commentRepository.save(reply2);

    //     // when
    //     List<Comment> foundReplies = commentRepository.findByParentCommentId(parentCommentId);

    //     // then
    //     assertEquals(2, foundReplies.size());
    //     assertTrue(foundReplies.stream().anyMatch(comment -> comment.getCommentText().equals("Reply 1")));
    //     assertTrue(foundReplies.stream().anyMatch(comment -> comment.getCommentText().equals("Reply 2")));
    // }

    @Test
    public void findByOwnerId_success() {
        // given
        String ownerId = "owner-" + System.currentTimeMillis();
        
        Comment comment1 = new Comment();
        comment1.setCommentText("Comment 1");
        comment1.setIdeaId("idea-123");
        comment1.setOwnerId(ownerId);
        // comment1.setCreatedAt(LocalDateTime.now());
        // comment1.setProjectId("project-123");
        
        Comment comment2 = new Comment();
        comment2.setCommentText("Comment 2");
        comment2.setIdeaId("idea-456");
        comment2.setOwnerId(ownerId);
        // comment2.setCreatedAt(LocalDateTime.now());
        // comment2.setProjectId("project-456");
        
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        // when
        List<Comment> foundComments = commentRepository.findByOwnerId(ownerId);

        // then
        assertEquals(2, foundComments.size());
        assertTrue(foundComments.stream().anyMatch(comment -> comment.getCommentText().equals("Comment 1")));
        assertTrue(foundComments.stream().anyMatch(comment -> comment.getCommentText().equals("Comment 2")));
    }
}