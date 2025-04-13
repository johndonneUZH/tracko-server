package ch.uzh.ifi.hase.soprafs24.constant;

public enum ChangeType {
    MODIFIED_IDEA("Modified an idea"),
    ADDED_IDEA("Added an idea"),
    CLOSED_IDEA("Closed an idea"),
    ADDED_PROJECT("Added a project"),
    DELETED_PROJECT("Deleted a project"),
    CHANGED_PROJECT_SETTINGS("Changed project settings"),
    ADDED_COMMENT("Added a comment"),
    DELETED_COMMENT("Deleted a comment");

    private final String description;

    ChangeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

