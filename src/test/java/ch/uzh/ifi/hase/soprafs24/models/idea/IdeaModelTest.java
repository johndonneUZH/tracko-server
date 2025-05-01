package ch.uzh.ifi.hase.soprafs24.models.idea;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs24.constant.IdeaStatus;

public class IdeaModelTest {

    @Test
    public void testIdeaModel() {
        // Create test data
        String ideaId = "idea-123";
        String ideaName = "Test Idea";
        String ideaDescription = "Test Description";
        String projectId = "project-123";
        String ownerId = "user-123";
        List<String> upVotes = Collections.emptyList();
        List<String> downVotes = Collections.emptyList();

        // Create and set Idea object
        Idea idea = new Idea();
        idea.setIdeaId(ideaId);
        idea.setIdeaName(ideaName);
        idea.setIdeaDescription(ideaDescription);
        idea.setProjectId(projectId);
        idea.setOwnerId(ownerId);
        idea.setUpVotes(upVotes);
        idea.setDownVotes(downVotes);

        // Verify getters return correct values
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
        // Create test data
        String ideaName = "Test Idea";
        String ideaDescription = "Test Description";

        // Create and set IdeaRegister object
        IdeaRegister ideaRegister = new IdeaRegister();
        ideaRegister.setIdeaName(ideaName);
        ideaRegister.setIdeaDescription(ideaDescription);

        // Verify getters return correct values
        assertEquals(ideaName, ideaRegister.getIdeaName());
        assertEquals(ideaDescription, ideaRegister.getIdeaDescription());
    }

    @Test
    public void testIdeaUpdateModel() {
        // Create test data
        String ideaName = "Updated Idea";
        String ideaDescription = "Updated Description";
        List<String> upVotes = Arrays.asList("1", "2");
        List<String> downVotes = Arrays.asList("3");

        // Create and set IdeaUpdate object
        IdeaUpdate ideaUpdate = new IdeaUpdate();
        ideaUpdate.setIdeaName(ideaName);
        ideaUpdate.setIdeaDescription(ideaDescription);
        ideaUpdate.setUpVotes(upVotes);
        ideaUpdate.setDownVotes(downVotes);

        // Verify getters return correct values
        assertEquals(ideaName, ideaUpdate.getIdeaName());
        assertEquals(ideaDescription, ideaUpdate.getIdeaDescription());
        assertEquals(upVotes, ideaUpdate.getUpVotes());
        assertEquals(downVotes, ideaUpdate.getDownVotes());
    }
}