// package ch.uzh.ifi.hase.soprafs24.models.idea;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.List;

// import org.junit.jupiter.api.Test;

// import ch.uzh.ifi.hase.soprafs24.constant.IdeaStatus;

// public class IdeaModelTest {

//     @Test
//     public void testIdeaModel() {
//         // Create test data
//         String ideaId = "idea-123";
//         String ideaName = "Test Idea";
//         String ideaDescription = "Test Description";
//         String projectId = "project-123";
//         String ownerId = "user-123";
//         LocalDateTime createdAt = LocalDateTime.now();
//         LocalDateTime updatedAt = LocalDateTime.now();
//         IdeaStatus ideaStatus = IdeaStatus.OPEN;
//         Long upVotes = 5L;
//         Long downVotes = 2L;
//         List<String> subIdeas = Arrays.asList("sub-idea-1", "sub-idea-2");

//         // Create and set Idea object
//         Idea idea = new Idea();
//         idea.setIdeaId(ideaId);
//         idea.setIdeaName(ideaName);
//         idea.setIdeaDescription(ideaDescription);
//         idea.setProjectId(projectId);
//         idea.setOwnerId(ownerId);
//         idea.setCreatedAt(createdAt);
//         idea.setUpdatedAt(updatedAt);
//         idea.setIdeaStatus(ideaStatus);
//         idea.setUpVotes(upVotes);
//         idea.setDownVotes(downVotes);
//         idea.setSubIdeas(subIdeas);

//         // Verify getters return correct values
//         assertEquals(ideaId, idea.getIdeaId());
//         assertEquals(ideaName, idea.getIdeaName());
//         assertEquals(ideaDescription, idea.getIdeaDescription());
//         assertEquals(projectId, idea.getProjectId());
//         assertEquals(ownerId, idea.getOwnerId());
//         assertEquals(createdAt, idea.getCreatedAt());
//         assertEquals(updatedAt, idea.getUpdatedAt());
//         assertEquals(ideaStatus, idea.getIdeaStatus());
//         assertEquals(upVotes, idea.getUpVotes());
//         assertEquals(downVotes, idea.getDownVotes());
//         assertEquals(subIdeas, idea.getSubIdeas());
//     }

//     @Test
//     public void testIdeaRegisterModel() {
//         // Create test data
//         String ideaName = "Test Idea";
//         String ideaDescription = "Test Description";

//         // Create and set IdeaRegister object
//         IdeaRegister ideaRegister = new IdeaRegister();
//         ideaRegister.setIdeaName(ideaName);
//         ideaRegister.setIdeaDescription(ideaDescription);

//         // Verify getters return correct values
//         assertEquals(ideaName, ideaRegister.getIdeaName());
//         assertEquals(ideaDescription, ideaRegister.getIdeaDescription());
//     }

//     @Test
//     public void testIdeaUpdateModel() {
//         // Create test data
//         String ideaName = "Updated Idea";
//         String ideaDescription = "Updated Description";
//         IdeaStatus ideaStatus = IdeaStatus.CLOSED;
//         Long upVotes = 10L;
//         Long downVotes = 3L;
//         List<String> subIdeas = Arrays.asList("sub-idea-3", "sub-idea-4");

//         // Create and set IdeaUpdate object
//         IdeaUpdate ideaUpdate = new IdeaUpdate();
//         ideaUpdate.setIdeaName(ideaName);
//         ideaUpdate.setIdeaDescription(ideaDescription);
//         ideaUpdate.setIdeaStatus(ideaStatus);
//         ideaUpdate.setUpVotes(upVotes);
//         ideaUpdate.setDownVotes(downVotes);
//         ideaUpdate.setSubIdeas(subIdeas);

//         // Verify getters return correct values
//         assertEquals(ideaName, ideaUpdate.getIdeaName());
//         assertEquals(ideaDescription, ideaUpdate.getIdeaDescription());
//         assertEquals(ideaStatus, ideaUpdate.getIdeaStatus());
//         assertEquals(upVotes, ideaUpdate.getUpVotes());
//         assertEquals(downVotes, ideaUpdate.getDownVotes());
//         assertEquals(subIdeas, ideaUpdate.getSubIdeas());
//     }
// }