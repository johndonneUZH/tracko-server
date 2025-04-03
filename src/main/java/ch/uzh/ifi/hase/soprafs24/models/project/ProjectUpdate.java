package ch.uzh.ifi.hase.soprafs24.models.project;

import java.util.List;

public class ProjectUpdate {
    private String projectName;
    private String projectDescription;
    private List<String> membersToAdd;
    private List<String> membersToRemove;

    // Getters and Setters
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String projectDescription) { this.projectDescription = projectDescription; }

    public List<String> getMembersToAdd() { return membersToAdd; }
    public void setMembersToAdd(List<String> membersToAdd) { this.membersToAdd = membersToAdd; }

    public List<String> getMembersToRemove() { return membersToRemove; }
    public void setMembersToRemove(List<String> membersToRemove) { this.membersToRemove = membersToRemove; }
}