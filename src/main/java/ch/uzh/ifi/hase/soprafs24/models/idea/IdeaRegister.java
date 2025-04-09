package ch.uzh.ifi.hase.soprafs24.models.idea;


public class IdeaRegister {
    
    private String ideaName;
    private String ideaDescription;
    private float x;
    private float y;

    public String getIdeaName() { return ideaName; }
    public void setIdeaName(String ideaName) { this.ideaName = ideaName; }

    public String getIdeaDescription() { return ideaDescription; }
    public void setIdeaDescription(String ideaDescription) { this.ideaDescription = ideaDescription; }

    public float getX() { return x; }
    public void setx(float x) { this.x = x; }



    public float gety() { return y; }
    public void sety(float y) { this.y = y; }
}
