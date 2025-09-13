package models.idea;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import constant.IdeaStatus;
import models.idea.Idea;
import models.idea.IdeaRegister;
import models.idea.IdeaUpdate;

public class IdeaModelTest {

    @Test
    public void testIdeaModel() {
        String ideaId = "idea-123";
        String ideaName = "Test Idea";
        String ideaDescription = "Test Description";
        String projectId = "project-123";
        String ownerId = "user-123";
        List<String> upVotes = Collections.emptyList();
        List<String> downVotes = Collections.emptyList();

        Idea idea = new Idea();
        idea.setIdeaId(ideaId);
        idea.setIdeaName(ideaName);
        idea.setIdeaDescription(ideaDescription);
        idea.setProjectId(projectId);
        idea.setOwnerId(ownerId);
        idea.setUpVotes(upVotes);
        idea.setDownVotes(downVotes);

        assertEquals(ideaId, idea.getIdeaId());
        assertEquals(ideaName, idea.getIdeaName());
        assertEquals(ideaDescription, idea.getIdeaDescription());
        assertEquals(projectId, idea.getProjectId());
        assertEquals(ownerId, idea.getOwnerId());
        assertEquals(upVotes, idea.getUpVotes());
        assertEquals(downVotes, idea.getDownVotes());
    }

    @Test
    public void testIdeaRegisterModel() {
        String ideaName = "Test Idea";
        String ideaDescription = "Test Description";

        IdeaRegister ideaRegister = new IdeaRegister();
        ideaRegister.setIdeaName(ideaName);
        ideaRegister.setIdeaDescription(ideaDescription);

        assertEquals(ideaName, ideaRegister.getIdeaName());
        assertEquals(ideaDescription, ideaRegister.getIdeaDescription());
    }

    @Test
    public void testIdeaUpdateModel() {
        String ideaName = "Updated Idea";
        String ideaDescription = "Updated Description";
        List<String> upVotes = Arrays.asList("1", "2");
        List<String> downVotes = Arrays.asList("3");

        IdeaUpdate ideaUpdate = new IdeaUpdate();
        ideaUpdate.setIdeaName(ideaName);
        ideaUpdate.setIdeaDescription(ideaDescription);
        ideaUpdate.setUpVotes(upVotes);
        ideaUpdate.setDownVotes(downVotes);

        assertEquals(ideaName, ideaUpdate.getIdeaName());
        assertEquals(ideaDescription, ideaUpdate.getIdeaDescription());
        assertEquals(upVotes, ideaUpdate.getUpVotes());
        assertEquals(downVotes, ideaUpdate.getDownVotes());
    }
}