package ch.uzh.ifi.hase.soprafs24.models.idea;

import java.util.List;


public class IdeaUpdate {
        
    private String ideaName;
    private String ideaDescription;
    private float x;
    private float y;
    private List<String>  upVotes;
    private List<String>  downVotes;
    private List<String> comments;

    public String getIdeaName() { return ideaName; }
    public void setIdeaName(String ideaName) { this.ideaName = ideaName; }

    public String getIdeaDescription() { return ideaDescription; }
    public void setIdeaDescription(String ideaDescription) { this.ideaDescription = ideaDescription; }
    

    public List<String> getUpVotes() { return upVotes; }
    public void setUpVotes(List<String> upVotes) { this.upVotes = upVotes; }

    public List<String> getDownVotes() { return downVotes; }
    public void setDownVotes(List<String> downVotes) { this.downVotes = downVotes; }

    public float getX() { return x; }
    public void setx(float x) { this.x = x; }

    public List<String> getComments() { return comments; }
    public void setComments(List<String> comments) { this.comments = comments; }

    public float gety() { return y; }
    public void sety(float y) { this.y = y; }

    // public List<String> getSubIdeas() { return subIdeas; }
    // public void setSubIdeas(List<String> subIdeas) { this.subIdeas = subIdeas; }
}
