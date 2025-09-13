package tracko.models.change;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import tracko.constant.ChangeType;

@Document(collection = "Changes")
public class Change {
    
    @Id
    private String changeId;
    private String projectId;
    private String ownerId;
    private ChangeType changeType;
    private String changeDescription;
    private LocalDateTime createdAt;

    public String getChangeId() { return changeId; }
    public void setChangeId(String changeId) { this.changeId = changeId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public ChangeType getChangeType() { return changeType; }
    public void setChangeType(ChangeType changeType) { this.changeType = changeType; }

    public String getChangeDescription() { return changeDescription; }
    public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
}
