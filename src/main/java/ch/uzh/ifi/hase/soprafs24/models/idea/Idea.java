package ch.uzh.ifi.hase.soprafs24.models.idea;


import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;



@Document(collection = "Ideas")
public class Idea {
    @Id
    private String ideaId;
    private String ideaName;
    private String ideaDescription;
    @Indexed
    private String projectId;
    private String ownerId;
    private Float x;
    private Float y;
    // private LocalDateTime createdAt;
    // private LocalDateTime updatedAt;
    // private IdeaStatus ideaStatus;
    private List<String> upVotes;
    private List<String> downVotes;
    private List<String> comments;

    public String getIdeaId() { return ideaId; }
    public void setIdeaId(String ideaId) { this.ideaId = ideaId; }

    public String getIdeaName() { return ideaName; }
    public void setIdeaName(String ideaName) { this.ideaName = ideaName; }

    public String getIdeaDescription() { return ideaDescription; }
    public void setIdeaDescription(String ideaDescription) { this.ideaDescription = ideaDescription; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    // public LocalDateTime getCreatedAt() { return createdAt; }
    // public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // public LocalDateTime getUpdatedAt() { return updatedAt; }
    // public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // public IdeaStatus getIdeaStatus() { return ideaStatus; }
    // public void setIdeaStatus(IdeaStatus ideaStatus) { this.ideaStatus = ideaStatus; }

    public List<String> getUpVotes() { return upVotes; }
    public void setUpVotes(List<String> upVotes) { this.upVotes = upVotes; }

    public List<String> getDownVotes() { return downVotes; }
    public void setDownVotes(List<String> downVotes) { this.downVotes = downVotes; }

    public Float getX() { return x; }
    public void setx(Float x) { this.x = x; }

    public List<String> getComments() { return comments; }
    public void setComments(List<String> comments) { this.comments = comments; }

    public Float gety() { return y; }
    public void sety(Float y) { this.y = y; }

}
