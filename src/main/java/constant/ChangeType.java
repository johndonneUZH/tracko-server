package constant;

public enum ChangeType {
    MODIFIED_IDEA("Modified an idea"),
    ADDED_IDEA("Added an idea"),
    CLOSED_IDEA("Closed an idea"),

    CHANGED_PROJECT_SETTINGS("Changed project settings"),
    LEFT_PROJECT("Left the project"),

    ADDED_COMMENT("Added a comment"),
    DELETED_COMMENT("Deleted a comment"),

    ADDED_MEMBER("Added a member"),
    REMOVED_MEMBER("Removed a member"),

    UPVOTE("Added a vote"),
    DOWNVOTE("Removed a vote"),

    ACCEPTED_FRIEND_REQUEST("Accepted your friend request"),
    REJECTED_FRIEND_REQUEST("Rejected your friend request"),
    SENT_FRIEND_REQUEST("Sent you a friend request"),
    REMOVED_FRIEND("Removed you as a friend");

    private final String description;

    ChangeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

