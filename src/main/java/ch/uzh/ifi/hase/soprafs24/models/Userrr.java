package ch.uzh.ifi.hase.soprafs24.models;

import org.springframework.data.mongodb.core.mapping.Document;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

@Document(collection = "Users")
public class Userrr {
    private String name;
    private String username;
    private UserStatus status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
}
