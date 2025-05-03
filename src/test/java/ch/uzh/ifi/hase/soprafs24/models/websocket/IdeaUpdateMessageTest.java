package ch.uzh.ifi.hase.soprafs24.models.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;

public class IdeaUpdateMessageTest {

    @Test
    public void testDefaultConstructor() {
        // Create instance with default constructor
        IdeaUpdateMessage message = new IdeaUpdateMessage();
        
        // Verify all fields are null with default constructor
        assertNull(message.getIdeaId());
        assertNull(message.getProjectId());
        assertNull(message.getAction());
        assertNull(message.getIdea());
    }
    
    @Test
    public void testParameterizedConstructor() {
        // Create test data
        String ideaId = "idea-123";
        String projectId = "project-123";
        String action = "CREATE";
        Idea idea = new Idea();
        idea.setIdeaId(ideaId);
        
        // Create message using parameterized constructor
        IdeaUpdateMessage message = new IdeaUpdateMessage(action, projectId, ideaId, idea);
        
        // Verify constructor sets all fields correctly
        assertEquals(ideaId, message.getIdeaId());
        assertEquals(projectId, message.getProjectId());
        assertEquals(action, message.getAction());
        assertEquals(idea, message.getIdea());
    }
    
    @Test
    public void testSettersAndGetters() {
        // Create test data
        String ideaId = "idea-123";
        String projectId = "project-123";
        String action = "UPDATE";
        Idea idea = new Idea();
        idea.setIdeaId(ideaId);
        
        // Create empty message
        IdeaUpdateMessage message = new IdeaUpdateMessage();
        
        // Use setters
        message.setIdeaId(ideaId);
        message.setProjectId(projectId);
        message.setAction(action);
        message.setIdea(idea);
        
        // Verify getters return correct values
        assertEquals(ideaId, message.getIdeaId());
        assertEquals(projectId, message.getProjectId());
        assertEquals(action, message.getAction());
        assertEquals(idea, message.getIdea());
    }
    
    @Test
    public void testCreateAction() {
        // Create test data for CREATE action
        String ideaId = "idea-123";
        String projectId = "project-123";
        String action = "CREATE";
        Idea idea = new Idea();
        idea.setIdeaId(ideaId);
        idea.setIdeaName("New Idea");
        
        // Create message
        IdeaUpdateMessage message = new IdeaUpdateMessage(action, projectId, ideaId, idea);
        
        // Verify message is correctly configured for CREATE action
        assertEquals("CREATE", message.getAction());
        assertEquals(ideaId, message.getIdeaId());
        assertEquals(projectId, message.getProjectId());
        assertEquals("New Idea", message.getIdea().getIdeaName());
    }
    
    @Test
    public void testUpdateAction() {
        // Create test data for UPDATE action
        String ideaId = "idea-123";
        String projectId = "project-123";
        String action = "UPDATE";
        Idea idea = new Idea();
        idea.setIdeaId(ideaId);
        idea.setIdeaName("Updated Idea");
        
        // Create message
        IdeaUpdateMessage message = new IdeaUpdateMessage(action, projectId, ideaId, idea);
        
        // Verify message is correctly configured for UPDATE action
        assertEquals("UPDATE", message.getAction());
        assertEquals(ideaId, message.getIdeaId());
        assertEquals(projectId, message.getProjectId());
        assertEquals("Updated Idea", message.getIdea().getIdeaName());
    }
    
    @Test
    public void testDeleteAction() {
        // Create test data for DELETE action
        String ideaId = "idea-123";
        String projectId = "project-123";
        String action = "DELETE";
        
        // For delete actions, the idea object might be null
        IdeaUpdateMessage message = new IdeaUpdateMessage(action, projectId, ideaId, null);
        
        // Verify message is correctly configured for DELETE action
        assertEquals("DELETE", message.getAction());
        assertEquals(ideaId, message.getIdeaId());
        assertEquals(projectId, message.getProjectId());
        assertNull(message.getIdea());
    }
    
    @Test
    public void testModifyingFieldsAfterCreation() {
        // Create initial message
        String initialIdeaId = "idea-123";
        String initialProjectId = "project-123";
        String initialAction = "CREATE";
        Idea initialIdea = new Idea();
        initialIdea.setIdeaId(initialIdeaId);
        
        IdeaUpdateMessage message = new IdeaUpdateMessage(
            initialAction, 
            initialProjectId, 
            initialIdeaId, 
            initialIdea
        );
        
        // Modify fields
        String updatedIdeaId = "idea-456";
        String updatedProjectId = "project-456";
        String updatedAction = "UPDATE";
        Idea updatedIdea = new Idea();
        updatedIdea.setIdeaId(updatedIdeaId);
        
        message.setIdeaId(updatedIdeaId);
        message.setProjectId(updatedProjectId);
        message.setAction(updatedAction);
        message.setIdea(updatedIdea);
        
        // Verify fields were updated
        assertEquals(updatedIdeaId, message.getIdeaId());
        assertEquals(updatedProjectId, message.getProjectId());
        assertEquals(updatedAction, message.getAction());
        assertEquals(updatedIdea, message.getIdea());
    }
}