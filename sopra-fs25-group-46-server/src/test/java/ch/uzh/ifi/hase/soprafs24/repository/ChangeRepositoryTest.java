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
import ch.uzh.ifi.hase.soprafs24.constant.ChangeType;
import ch.uzh.ifi.hase.soprafs24.models.change.Change;

@SpringBootTest
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class ChangeRepositoryTest {

    @Autowired
    private ChangeRepository changeRepository;
    
    @BeforeEach
    public void setup() {
        // Clear the test database before each test
        changeRepository.deleteAll();
    }
    
    @Test
    public void testMongoConnection() {
        // Create a test change
        Change testChange = new Change();
        testChange.setChangeType(ChangeType.ADDED_IDEA);
        testChange.setChangeDescription("This is a test change");
        testChange.setProjectId("project-123");
        testChange.setOwnerId("user-123");
        testChange.setCreatedAt(LocalDateTime.now());
        
        // Save to repository
        Change savedChange = changeRepository.save(testChange);
        
        // Verify the change was saved
        assertNotNull(savedChange.getChangeId());
        
        // Retrieve and verify
        Optional<Change> retrievedChange = changeRepository.findById(savedChange.getChangeId());
        assertTrue(retrievedChange.isPresent());
        assertEquals(ChangeType.ADDED_IDEA, retrievedChange.get().getChangeType());
        assertEquals("This is a test change", retrievedChange.get().getChangeDescription());
    }

    @Test
    public void findByProjectId_success() {
        // given
        String projectId = "project-" + System.currentTimeMillis();
        
        Change change1 = new Change();
        change1.setChangeType(ChangeType.ADDED_IDEA);
        change1.setChangeDescription("Change 1");
        change1.setProjectId(projectId);
        change1.setOwnerId("user-123");
        change1.setCreatedAt(LocalDateTime.now());
        
        Change change2 = new Change();
        change2.setChangeType(ChangeType.CHANGED_PROJECT_SETTINGS);
        change2.setChangeDescription("Change 2");
        change2.setProjectId(projectId);
        change2.setOwnerId("user-123");
        change2.setCreatedAt(LocalDateTime.now());
        
        changeRepository.save(change1);
        changeRepository.save(change2);

        // when
        List<Change> foundChanges = changeRepository.findByProjectId(projectId);

        // then
        assertEquals(2, foundChanges.size());
        assertTrue(foundChanges.stream().anyMatch(change -> change.getChangeDescription().equals("Change 1")));
        assertTrue(foundChanges.stream().anyMatch(change -> change.getChangeDescription().equals("Change 2")));
    }
    
    @Test
    public void findByOwnerId_success() {
        // given
        String ownerId = "owner-" + System.currentTimeMillis();
        
        Change change1 = new Change();
        change1.setChangeType(ChangeType.ADDED_IDEA);
        change1.setChangeDescription("Change 1");
        change1.setProjectId("project-123");
        change1.setOwnerId(ownerId);
        change1.setCreatedAt(LocalDateTime.now());
        
        Change change2 = new Change();
        change2.setChangeType(ChangeType.ADDED_COMMENT);
        change2.setChangeDescription("Change 2");
        change2.setProjectId("project-456");
        change2.setOwnerId(ownerId);
        change2.setCreatedAt(LocalDateTime.now());
        
        changeRepository.save(change1);
        changeRepository.save(change2);

        // when
        List<Change> foundChanges = changeRepository.findByOwnerId(ownerId);

        // then
        assertEquals(2, foundChanges.size());
        assertTrue(foundChanges.stream().anyMatch(change -> change.getChangeDescription().equals("Change 1")));
        assertTrue(foundChanges.stream().anyMatch(change -> change.getChangeDescription().equals("Change 2")));
    }
    
    @Test
    public void testMultipleChangeTypes() {
        // given
        String projectId = "project-" + System.currentTimeMillis();
        
        Change modifiedIdeaChange = new Change();
        modifiedIdeaChange.setChangeType(ChangeType.MODIFIED_IDEA);
        modifiedIdeaChange.setChangeDescription("Modified idea description");
        modifiedIdeaChange.setProjectId(projectId);
        modifiedIdeaChange.setOwnerId("user-123");
        modifiedIdeaChange.setCreatedAt(LocalDateTime.now());
        
        Change addedIdeaChange = new Change();
        addedIdeaChange.setChangeType(ChangeType.ADDED_IDEA);
        addedIdeaChange.setChangeDescription("Added new idea");
        addedIdeaChange.setProjectId(projectId);
        addedIdeaChange.setOwnerId("user-123");
        addedIdeaChange.setCreatedAt(LocalDateTime.now());
        
        Change changedSettingsChange = new Change();
        changedSettingsChange.setChangeType(ChangeType.CHANGED_PROJECT_SETTINGS);
        changedSettingsChange.setChangeDescription("Updated project name");
        changedSettingsChange.setProjectId(projectId);
        changedSettingsChange.setOwnerId("user-123");
        changedSettingsChange.setCreatedAt(LocalDateTime.now());
        
        Change closedIdeaChange = new Change();
        closedIdeaChange.setChangeType(ChangeType.CLOSED_IDEA);
        closedIdeaChange.setChangeDescription("Closed implemented idea");
        closedIdeaChange.setProjectId(projectId);
        closedIdeaChange.setOwnerId("user-123");
        closedIdeaChange.setCreatedAt(LocalDateTime.now());
        
        Change addedCommentChange = new Change();
        addedCommentChange.setChangeType(ChangeType.ADDED_COMMENT);
        addedCommentChange.setChangeDescription("Added comment to idea");
        addedCommentChange.setProjectId(projectId);
        addedCommentChange.setOwnerId("user-123");
        addedCommentChange.setCreatedAt(LocalDateTime.now());
        
        changeRepository.save(modifiedIdeaChange);
        changeRepository.save(addedIdeaChange);
        changeRepository.save(changedSettingsChange);
        changeRepository.save(closedIdeaChange);
        changeRepository.save(addedCommentChange);

        // when
        List<Change> foundChanges = changeRepository.findByProjectId(projectId);

        // then
        assertEquals(5, foundChanges.size());
        assertTrue(foundChanges.stream().anyMatch(change -> change.getChangeType() == ChangeType.MODIFIED_IDEA));
        assertTrue(foundChanges.stream().anyMatch(change -> change.getChangeType() == ChangeType.ADDED_IDEA));
        assertTrue(foundChanges.stream().anyMatch(change -> change.getChangeType() == ChangeType.CHANGED_PROJECT_SETTINGS));
        assertTrue(foundChanges.stream().anyMatch(change -> change.getChangeType() == ChangeType.CLOSED_IDEA));
        assertTrue(foundChanges.stream().anyMatch(change -> change.getChangeType() == ChangeType.ADDED_COMMENT));
    }
}