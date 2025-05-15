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
import ch.uzh.ifi.hase.soprafs24.models.project.Project;

@SpringBootTest
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;
    
    @BeforeEach
    public void setup() {
        projectRepository.deleteAll();
    }
    
    @Test
    public void testMongoConnection() {
        Project testProject = new Project();
        testProject.setProjectName("Test Project");
        testProject.setProjectDescription("This is a test project");
        testProject.setOwnerId("user-123");
        testProject.setProjectMembers(new ArrayList<>());
        testProject.setCreatedAt(LocalDateTime.now());
        testProject.setUpdatedAt(LocalDateTime.now());
        
        Project savedProject = projectRepository.save(testProject);
        
        assertNotNull(savedProject.getProjectId());
        
        Optional<Project> retrievedProject = projectRepository.findById(savedProject.getProjectId());
        assertTrue(retrievedProject.isPresent());
        assertEquals("Test Project", retrievedProject.get().getProjectName());
    }

    @Test
    public void findByProjectName_success() {
        String uniqueProjectName = "TestProject-" + System.currentTimeMillis();
        
        Project project = new Project();
        project.setProjectName(uniqueProjectName);
        project.setProjectDescription("Test Description");
        project.setOwnerId("user-123");
        project.setProjectMembers(new ArrayList<>());
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        
        projectRepository.save(project);

        Project found = projectRepository.findByProjectName(uniqueProjectName);

        assertNotNull(found);
        assertEquals(uniqueProjectName, found.getProjectName());
    }
    
    @Test
    public void findByOwnerId_success() {
        String ownerId = "owner-" + System.currentTimeMillis();
        
        Project project1 = new Project();
        project1.setProjectName("Project 1");
        project1.setProjectDescription("Description 1");
        project1.setOwnerId(ownerId);
        project1.setProjectMembers(new ArrayList<>());
        project1.setCreatedAt(LocalDateTime.now());
        project1.setUpdatedAt(LocalDateTime.now());
        
        Project project2 = new Project();
        project2.setProjectName("Project 2");
        project2.setProjectDescription("Description 2");
        project2.setOwnerId(ownerId);
        project2.setProjectMembers(new ArrayList<>());
        project2.setCreatedAt(LocalDateTime.now());
        project2.setUpdatedAt(LocalDateTime.now());
        
        projectRepository.save(project1);
        projectRepository.save(project2);

        List<Project> foundProjects = projectRepository.findByOwnerId(ownerId);

        assertEquals(2, foundProjects.size());
        assertTrue(foundProjects.stream().anyMatch(project -> project.getProjectName().equals("Project 1")));
        assertTrue(foundProjects.stream().anyMatch(project -> project.getProjectName().equals("Project 2")));
    }
    
    @Test
    public void existsByProjectName_success() {
        String uniqueProjectName = "TestProject-" + System.currentTimeMillis();
        
        Project project = new Project();
        project.setProjectName(uniqueProjectName);
        project.setProjectDescription("Test Description");
        project.setOwnerId("user-123");
        project.setProjectMembers(new ArrayList<>());
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        
        projectRepository.save(project);

        boolean exists = projectRepository.existsByProjectName(uniqueProjectName);

        assertTrue(exists);
    }
}