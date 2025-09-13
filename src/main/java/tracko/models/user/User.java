package tracko.models.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tracko.constant.UserStatus;

@Document(collection = "Users")
public class User {

    @Id
    private String id;  
    private String name;
    private String username;
    private String email;
    private String password;
    private UserStatus status;
    private List<String> projectIds;
    private LocalDateTime createAt;
    private LocalDateTime lastLoginAt;
    private List<String> friendsIds;
    private List<String> friendRequestsIds;
    private List<String> friendRequestsSentIds;
    private String avatarUrl;
    private String birthday;
    private String bio;

    public String getId() { return id; }   
    public void setId(String id) { this.id = id; } 

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public List<String> getProjectIds() { return projectIds; }
    public void setProjectIds(List<String> projectIds) { this.projectIds = projectIds; }

    public LocalDateTime getCreateAt() { return createAt; }
    public void setCreateAt(LocalDateTime createAt) { this.createAt = createAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public List<String> getFriendsIds() { return friendsIds; }
    public void setFriendsIds(List<String> friendsIds) { this.friendsIds = friendsIds; }

    public List<String> getFriendRequestsIds() { return friendRequestsIds; }
    public void setFriendRequestsIds(List<String> friendRequestsIds) { this.friendRequestsIds = friendRequestsIds; }

    public List<String> getFriendRequestsSentIds() { return friendRequestsSentIds; }
    public void setFriendRequestsSentIds(List<String> friendRequestsSentIds) { this.friendRequestsSentIds = friendRequestsSentIds; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

}
