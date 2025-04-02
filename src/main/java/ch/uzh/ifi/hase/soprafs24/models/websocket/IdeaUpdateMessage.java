// This class has several important roles:

// Flow of information is from the server to the client
// Data Transfer Object (DTO): It carries information about idea changes from your server to connected clients.
// Event Classification: The action field helps clients understand what happened (was an idea created, updated, or deleted?).
// Context Information: It provides the necessary context (projectId, ideaId) so clients know which specific entity changed.
// Payload: The idea field contains the actual data that changed, allowing clients to update their local state.

package ch.uzh.ifi.hase.soprafs24.models.websocket;

import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;

public class IdeaUpdateMessage {
    private String ideaId;
    private String projectId;
    private String action; // "CREATE", "UPDATE", "DELETE"
    private Idea idea;
    
    // Constructors
    public IdeaUpdateMessage() {}
    
    public IdeaUpdateMessage(String action, String projectId, String ideaId, Idea idea) {
        this.action = action;
        this.projectId = projectId;
        this.ideaId = ideaId;
        this.idea = idea;
    }
    
    // Getters and setters
    public String getIdeaId() {
        return ideaId;
    }
    
    public void setIdeaId(String ideaId) {
        this.ideaId = ideaId;
    }
    
    public String getProjectId() {
        return projectId;
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public Idea getIdea() {
        return idea;
    }
    
    public void setIdea(Idea idea) {
        this.idea = idea;
    }
}