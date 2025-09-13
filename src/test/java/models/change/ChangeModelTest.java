package models.change;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import constant.ChangeType;
import models.change.Change;
import models.change.ChangeRegister;

public class ChangeModelTest {

    @Test
    public void testChangeModel() {
        String changeId = "change-123";
        String projectId = "project-123";
        String ownerId = "user-123";
        ChangeType changeType = ChangeType.ADDED_IDEA;
        String changeDescription = "Added a new feature idea";
        LocalDateTime createdAt = LocalDateTime.now();

        Change change = new Change();
        change.setChangeId(changeId);
        change.setProjectId(projectId);
        change.setOwnerId(ownerId);
        change.setChangeType(changeType);
        change.setChangeDescription(changeDescription);
        change.setCreatedAt(createdAt);

        assertEquals(changeId, change.getChangeId());
        assertEquals(projectId, change.getProjectId());
        assertEquals(ownerId, change.getOwnerId());
        assertEquals(changeType, change.getChangeType());
        assertEquals(changeDescription, change.getChangeDescription());
        assertEquals(createdAt, change.getCreatedAt());
    }

    @Test
    public void testChangeRegisterModel() {
        ChangeType changeType = ChangeType.CHANGED_PROJECT_SETTINGS;

        ChangeRegister changeRegister = new ChangeRegister();
        changeRegister.setChangeType(changeType);

        assertEquals(changeType, changeRegister.getChangeType());
    }

    @Test
    public void testAllChangeTypes() {
        Change modifiedIdeaChange = new Change();
        modifiedIdeaChange.setChangeType(ChangeType.MODIFIED_IDEA);
        assertEquals(ChangeType.MODIFIED_IDEA, modifiedIdeaChange.getChangeType());
        assertEquals("Modified an idea", modifiedIdeaChange.getChangeType().getDescription());

        Change addedIdeaChange = new Change();
        addedIdeaChange.setChangeType(ChangeType.ADDED_IDEA);
        assertEquals(ChangeType.ADDED_IDEA, addedIdeaChange.getChangeType());
        assertEquals("Added an idea", addedIdeaChange.getChangeType().getDescription());

        Change closedIdeaChange = new Change();
        closedIdeaChange.setChangeType(ChangeType.CLOSED_IDEA);
        assertEquals(ChangeType.CLOSED_IDEA, closedIdeaChange.getChangeType());
        assertEquals("Closed an idea", closedIdeaChange.getChangeType().getDescription());

        Change changedProjectSettingsChange = new Change();
        changedProjectSettingsChange.setChangeType(ChangeType.CHANGED_PROJECT_SETTINGS);
        assertEquals(ChangeType.CHANGED_PROJECT_SETTINGS, changedProjectSettingsChange.getChangeType());
        assertEquals("Changed project settings", changedProjectSettingsChange.getChangeType().getDescription());

        Change addedCommentChange = new Change();
        addedCommentChange.setChangeType(ChangeType.ADDED_COMMENT);
        assertEquals(ChangeType.ADDED_COMMENT, addedCommentChange.getChangeType());
        assertEquals("Added a comment", addedCommentChange.getChangeType().getDescription());
    }
}