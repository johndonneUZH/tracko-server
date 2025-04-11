package ch.uzh.ifi.hase.soprafs24.models.idea;

import java.util.List;

import ch.uzh.ifi.hase.soprafs24.constant.IdeaStatus;

public class IdeaUpdate {
        
    private String ideaName;
    private String ideaDescription;
    private IdeaStatus ideaStatus;
    private Long upVotes;
    private Long downVotes;
    private List<String> subIdeas;

    public String getIdeaName() { return ideaName; }
    public void setIdeaName(String ideaName) { this.ideaName = ideaName; }

    public String getIdeaDescription() { return ideaDescription; }
    public void setIdeaDescription(String ideaDescription) { this.ideaDescription = ideaDescription; }

    public IdeaStatus getIdeaStatus() { return ideaStatus; }
    public void setIdeaStatus(IdeaStatus ideaStatus) { this.ideaStatus = ideaStatus; }

    public Long getUpVotes() { return upVotes; }
    public void setUpVotes(Long upVotes) { this.upVotes = upVotes; }

    public Long getDownVotes() { return downVotes; }
    public void setDownVotes(Long downVotes) { this.downVotes = downVotes; }

    public List<String> getSubIdeas() { return subIdeas; }
    public void setSubIdeas(List<String> subIdeas) { this.subIdeas = subIdeas; }
}
