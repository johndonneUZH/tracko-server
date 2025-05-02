package ch.uzh.ifi.hase.soprafs24.models.comment;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Comments")
public class Comment {

    @Id
    private String commentId;
    private String commentText;
    private String ideaId;
    private String ownerId;
    private List<String> replies; 



    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public String getIdeaId() { return ideaId; }
    public void setIdeaId(String ideaId) { this.ideaId = ideaId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public List<String> getReplies() { return replies; }
    public void setReplies(List<String> replies) { this.replies = replies; }

    // public String getParentCommentId() { return parentCommentId; }
    // public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }

    // public LocalDateTime getCreatedAt() { return createdAt; }
    // public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // public String getProjectId() { return projectId; }
    // public void setProjectId(String projectId) { this.projectId = projectId; }
}
