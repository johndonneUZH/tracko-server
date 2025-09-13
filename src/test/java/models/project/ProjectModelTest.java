package models.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import models.project.Project;
import models.project.ProjectRegister;
import models.project.ProjectUpdate;

public class ProjectModelTest {

    @Test
    public void testProjectModel() {
        String projectId = "project-123";
        String projectName = "Test Project";
        String projectDescription = "Test Description";
        List<String> projectMembers = Arrays.asList("user-1", "user-2");
        String ownerId = "owner-123";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        Project project = new Project();
        project.setProjectId(projectId);
        project.setProjectName(projectName);
        project.setProjectDescription(projectDescription);
        project.setProjectMembers(projectMembers);
        project.setOwnerId(ownerId);
        project.setCreatedAt(createdAt);
        project.setUpdatedAt(updatedAt);

        assertEquals(projectId, project.getProjectId());
        assertEquals(projectName, project.getProjectName());
        assertEquals(projectDescription, project.getProjectDescription());
        assertEquals(projectMembers, project.getProjectMembers());
        assertEquals(ownerId, project.getOwnerId());
        assertEquals(createdAt, project.getCreatedAt());
        assertEquals(updatedAt, project.getUpdatedAt());
    }

    @Test
    public void testProjectRegisterModel() {
        String projectName = "Test Project";
        String projectDescription = "Test Description";

        ProjectRegister projectRegister = new ProjectRegister();
        projectRegister.setProjectName(projectName);
        projectRegister.setProjectDescription(projectDescription);

        assertEquals(projectName, projectRegister.getProjectName());
        assertEquals(projectDescription, projectRegister.getProjectDescription());
    }

    @Test
    public void testProjectUpdateModel() {
        String projectName = "Updated Project";
        String projectDescription = "Updated Description";
        List<String> membersToAdd = Arrays.asList("user-3", "user-4");
        List<String> membersToRemove = Arrays.asList("user-1");

        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectName(projectName);
        projectUpdate.setProjectDescription(projectDescription);
        projectUpdate.setMembersToAdd(membersToAdd);
        projectUpdate.setMembersToRemove(membersToRemove);

        assertEquals(projectName, projectUpdate.getProjectName());
        assertEquals(projectDescription, projectUpdate.getProjectDescription());
        assertEquals(membersToAdd, projectUpdate.getMembersToAdd());
        assertEquals(membersToRemove, projectUpdate.getMembersToRemove());
    }
}