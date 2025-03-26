package ch.uzh.ifi.hase.soprafs24.models.project;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Projects")
public class Project {
    
    @Id
    private String projectId;
    private String projectName;
    private String projectDescription;
    private List<String> projectMembers;
    private String ownerId;

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String projectDescription) { this.projectDescription = projectDescription; }

    public List<String> getProjectMembers() { return projectMembers; }
    public void setProjectMembers(List<String> projectMembers) { this.projectMembers = projectMembers; }
}