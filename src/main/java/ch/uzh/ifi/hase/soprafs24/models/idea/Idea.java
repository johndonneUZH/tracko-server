package ch.uzh.ifi.hase.soprafs24.models.idea;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import ch.uzh.ifi.hase.soprafs24.constant.IdeaStatus;

@Document(collection = "Ideas")
public class Idea {
    @Id
    private String ideaId;
    private String ideaName;
    private String ideaDescription;
    private String projectId;
    private String ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private IdeaStatus ideaStatus;
    private Long upVotes;
    private Long downVotes;
    private List<String> subIdeas;

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public IdeaStatus getIdeaStatus() { return ideaStatus; }
    public void setIdeaStatus(IdeaStatus ideaStatus) { this.ideaStatus = ideaStatus; }

    public Long getUpVotes() { return upVotes; }
    public void setUpVotes(Long upVotes) { this.upVotes = upVotes; }

    public Long getDownVotes() { return downVotes; }
    public void setDownVotes(Long downVotes) { this.downVotes = downVotes; }

    public List<String> getSubIdeas() { return subIdeas; }
    public void setSubIdeas(List<String> subIdeas) { this.subIdeas = subIdeas; }

}
