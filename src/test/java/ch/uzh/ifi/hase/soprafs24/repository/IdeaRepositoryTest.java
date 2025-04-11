package ch.uzh.ifi.hase.soprafs24.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;
import ch.uzh.ifi.hase.soprafs24.constant.IdeaStatus;
import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;

@SpringBootTest
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class IdeaRepositoryTest {

    @Autowired
    private IdeaRepository ideaRepository;
    
    @BeforeEach
    public void setup() {
        // Clear the test database before each test
        ideaRepository.deleteAll();
    }
    
    @Test
    public void testMongoConnection() {
        // Create a test idea
        Idea testIdea = new Idea();
        testIdea.setIdeaName("Test Idea");
        testIdea.setIdeaDescription("This is a test idea");
        testIdea.setProjectId("project-123");
        testIdea.setOwnerId("user-123");
        testIdea.setCreatedAt(LocalDateTime.now());
        testIdea.setUpdatedAt(LocalDateTime.now());
        testIdea.setIdeaStatus(IdeaStatus.OPEN);
        testIdea.setUpVotes(0L);
        testIdea.setDownVotes(0L);
        testIdea.setSubIdeas(new ArrayList<>());
        
        // Save to repository
        Idea savedIdea = ideaRepository.save(testIdea);
        
        // Verify the idea was saved
        assertNotNull(savedIdea.getIdeaId());
        
        // Retrieve and verify
        Optional<Idea> retrievedIdea = ideaRepository.findById(savedIdea.getIdeaId());
        assertTrue(retrievedIdea.isPresent());
        assertEquals("Test Idea", retrievedIdea.get().getIdeaName());
    }

    @Test
    public void findByProjectId_success() {
        // given
        String projectId = "project-" + System.currentTimeMillis();
        
        Idea idea1 = new Idea();
        idea1.setIdeaName("Idea 1");
        idea1.setIdeaDescription("Description 1");
        idea1.setProjectId(projectId);
        idea1.setOwnerId("user-123");
        idea1.setCreatedAt(LocalDateTime.now());
        idea1.setUpdatedAt(LocalDateTime.now());
        idea1.setIdeaStatus(IdeaStatus.OPEN);
        idea1.setUpVotes(0L);
        idea1.setDownVotes(0L);
        idea1.setSubIdeas(new ArrayList<>());
        
        Idea idea2 = new Idea();
        idea2.setIdeaName("Idea 2");
        idea2.setIdeaDescription("Description 2");
        idea2.setProjectId(projectId);
        idea2.setOwnerId("user-123");
        idea2.setCreatedAt(LocalDateTime.now());
        idea2.setUpdatedAt(LocalDateTime.now());
        idea2.setIdeaStatus(IdeaStatus.OPEN);
        idea2.setUpVotes(0L);
        idea2.setDownVotes(0L);
        idea2.setSubIdeas(new ArrayList<>());
        
        ideaRepository.save(idea1);
        ideaRepository.save(idea2);

        // when
        List<Idea> foundIdeas = ideaRepository.findByProjectId(projectId);

        // then
        assertEquals(2, foundIdeas.size());
        assertTrue(foundIdeas.stream().anyMatch(idea -> idea.getIdeaName().equals("Idea 1")));
        assertTrue(foundIdeas.stream().anyMatch(idea -> idea.getIdeaName().equals("Idea 2")));
    }
    
    @Test
    public void findByOwnerId_success() {
        // given
        String ownerId = "owner-" + System.currentTimeMillis();
        
        Idea idea1 = new Idea();
        idea1.setIdeaName("Idea 1");
        idea1.setIdeaDescription("Description 1");
        idea1.setProjectId("project-123");
        idea1.setOwnerId(ownerId);
        idea1.setCreatedAt(LocalDateTime.now());
        idea1.setUpdatedAt(LocalDateTime.now());
        idea1.setIdeaStatus(IdeaStatus.OPEN);
        idea1.setUpVotes(0L);
        idea1.setDownVotes(0L);
        idea1.setSubIdeas(new ArrayList<>());
        
        Idea idea2 = new Idea();
        idea2.setIdeaName("Idea 2");
        idea2.setIdeaDescription("Description 2");
        idea2.setProjectId("project-456");
        idea2.setOwnerId(ownerId);
        idea2.setCreatedAt(LocalDateTime.now());
        idea2.setUpdatedAt(LocalDateTime.now());
        idea2.setIdeaStatus(IdeaStatus.OPEN);
        idea2.setUpVotes(0L);
        idea2.setDownVotes(0L);
        idea2.setSubIdeas(new ArrayList<>());
        
        ideaRepository.save(idea1);
        ideaRepository.save(idea2);

        // when
        List<Idea> foundIdeas = ideaRepository.findByOwnerId(ownerId);

        // then
        assertEquals(2, foundIdeas.size());
        assertTrue(foundIdeas.stream().anyMatch(idea -> idea.getIdeaName().equals("Idea 1")));
        assertTrue(foundIdeas.stream().anyMatch(idea -> idea.getIdeaName().equals("Idea 2")));
    }
}