package ch.uzh.ifi.hase.soprafs24.models.comment;

public class CommentRegister {
    
    private String commentText;
    private String parentId;

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

}
