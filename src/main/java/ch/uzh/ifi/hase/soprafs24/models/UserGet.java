package ch.uzh.ifi.hase.soprafs24.models;

// import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

public class UserGet {
    private String id;
    // private String name;
    private String username;
    private String email;
    // private UserStatus status; 

    // public UserGet(String id, String name, String username, String email, UserStatus status) {
    public UserGet(String id, String username, String email) {
        this.id = id;
        // this.name = name;
        this.username = username;
        this.email = email;
        // this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // public String getName() { return name; }
    // public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // public UserStatus getStatus() { return status; }
    // public void setStatus(UserStatus status) { this.status = status; }
}
