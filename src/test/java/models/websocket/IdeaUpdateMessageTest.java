package models.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import models.idea.Idea;
import models.websocket.IdeaUpdateMessage;

public class IdeaUpdateMessageTest {

    @Test
    public void testDefaultConstructor() {
        IdeaUpdateMessage message = new IdeaUpdateMessage();
        
        assertNull(message.getIdeaId());
        assertNull(message.getProjectId());
        assertNull(message.getAction());
        assertNull(message.getIdea());
    }
    
    @Test
    public void testParameterizedConstructor() {
        String ideaId = "idea-123";
        String projectId = "project-123";
        String action = "CREATE";
        Idea idea = new Idea();
        idea.setIdeaId(ideaId);
        
        IdeaUpdateMessage message = new IdeaUpdateMessage(action, projectId, ideaId, idea);
        
        assertEquals(ideaId, message.getIdeaId());
        assertEquals(projectId, message.getProjectId());
        assertEquals(action, message.getAction());
        assertEquals(idea, message.getIdea());
    }
    
    @Test
    public void testSettersAndGetters() {
        String ideaId = "idea-123";
        String projectId = "project-123";
        String action = "UPDATE";
        Idea idea = new Idea();
        idea.setIdeaId(ideaId);
        
        IdeaUpdateMessage message = new IdeaUpdateMessage();
        
        message.setIdeaId(ideaId);
        message.setProjectId(projectId);
        message.setAction(action);
        message.setIdea(idea);
        
        assertEquals(ideaId, message.getIdeaId());
        assertEquals(projectId, message.getProjectId());
        assertEquals(action, message.getAction());
        assertEquals(idea, message.getIdea());
    }
    
    @Test
    public void testCreateAction() {
        String ideaId = "idea-123";
        String projectId = "project-123";
        String action = "CREATE";
        Idea idea = new Idea();
        idea.setIdeaId(ideaId);
        idea.setIdeaName("New Idea");
        
        IdeaUpdateMessage message = new IdeaUpdateMessage(action, projectId, ideaId, idea);
        
        assertEquals("CREATE", message.getAction());
        assertEquals(ideaId, message.getIdeaId());
        assertEquals(projectId, message.getProjectId());
        assertEquals("New Idea", message.getIdea().getIdeaName());
    }
    
    @Test
    public void testUpdateAction() {
        String ideaId = "idea-123";
        String projectId = "project-123";
        String action = "UPDATE";
        Idea idea = new Idea();
        idea.setIdeaId(ideaId);
        idea.setIdeaName("Updated Idea");
        
        IdeaUpdateMessage message = new IdeaUpdateMessage(action, projectId, ideaId, idea);
        
        assertEquals("UPDATE", message.getAction());
        assertEquals(ideaId, message.getIdeaId());
        assertEquals(projectId, message.getProjectId());
        assertEquals("Updated Idea", message.getIdea().getIdeaName());
    }
    
    @Test
    public void testDeleteAction() {
        String ideaId = "idea-123";
        String projectId = "project-123";
        String action = "DELETE";
        
        IdeaUpdateMessage message = new IdeaUpdateMessage(action, projectId, ideaId, null);
        
        assertEquals("DELETE", message.getAction());
        assertEquals(ideaId, message.getIdeaId());
        assertEquals(projectId, message.getProjectId());
        assertNull(message.getIdea());
    }
    
    @Test
    public void testModifyingFieldsAfterCreation() {
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
        
        String updatedIdeaId = "idea-456";
        String updatedProjectId = "project-456";
        String updatedAction = "UPDATE";
        Idea updatedIdea = new Idea();
        updatedIdea.setIdeaId(updatedIdeaId);
        
        message.setIdeaId(updatedIdeaId);
        message.setProjectId(updatedProjectId);
        message.setAction(updatedAction);
        message.setIdea(updatedIdea);
        
        assertEquals(updatedIdeaId, message.getIdeaId());
        assertEquals(updatedProjectId, message.getProjectId());
        assertEquals(updatedAction, message.getAction());
        assertEquals(updatedIdea, message.getIdea());
    }
}