package ch.uzh.ifi.hase.soprafs24.models.project;

import java.util.List;

public class ProjectRegister {
    private String projectName;
    private String projectDescription;
    private List<String> projectMembers;
    private String projectLogoUrl;

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String projectDescription) { this.projectDescription = projectDescription; }

    public List<String> getProjectMembers() { return projectMembers; }
    public void setProjectMembers(List<String> projectMembers) { this.projectMembers = projectMembers; }

    public String getProjectLogoUrl() { return projectLogoUrl; }
    public void setProjectLogoUrl(String projectLogoUrl) { this.projectLogoUrl = projectLogoUrl; }
}
