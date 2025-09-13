package service;

import auth.JwtUtil;
import repository.UserRepository;
import constant.ChangeType;
import constant.LoginStatus;
import constant.UserStatus;
import models.project.Project;
import models.user.User;
import models.user.UserLogin;
import models.user.UserRegister;
import models.user.UserUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ProjectService projectService;
    private final ChangeService changeService;
    private final UserRepository userRepository;

    UserService(JwtUtil jwtUtil, @Lazy ProjectService projectService, UserRepository userRepository, ChangeService changeService) {
        this.userRepository = userRepository;
        this.changeService = changeService;
        this.projectService = projectService;
        this.jwtUtil = jwtUtil;
    }


    public List<User> getUsers() {
        return userRepository.findAll();
    } 

    public boolean checkIfUserExists(UserRegister newUser) {
        // If either there is a user with the same username, or email, it returns True
        User userByUsername = userRepository.findByUsername(newUser.getUsername());
        User userByEmail = userRepository.findByEmail(newUser.getEmail());

        if (userByUsername != null || userByEmail!= null) {
        return true;
        }
        return false;
    }

    public User createUser(UserRegister newUser) {
        /**Sets name/username/password/email from input (required).
        Sets projects and status to default values*/;
        User toBeSavedUser = new User();
        toBeSavedUser.setName(newUser.getName());
        toBeSavedUser.setUsername(newUser.getUsername());
        toBeSavedUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        toBeSavedUser.setEmail(newUser.getEmail());
        toBeSavedUser.setProjectIds(new ArrayList<>());
        toBeSavedUser.setStatus(UserStatus.ONLINE);
        toBeSavedUser.setCreateAt(LocalDateTime.now());
        toBeSavedUser.setLastLoginAt(LocalDateTime.now());
        toBeSavedUser.setFriendsIds(new ArrayList<>());
        toBeSavedUser.setFriendRequestsIds(new ArrayList<>());
        toBeSavedUser.setFriendRequestsSentIds(new ArrayList<>());
        toBeSavedUser.setAvatarUrl("https://avatar.vercel.sh/" + newUser.getUsername());
        toBeSavedUser.setBirthday(null);
        toBeSavedUser.setBio(null);

        toBeSavedUser = userRepository.save(toBeSavedUser);

        log.debug("Created Information for User: " + newUser);
        return toBeSavedUser;
    }

    public LoginStatus checkLoginRequest(UserLogin loginRequest) {
        /*
            Try to test the credentials. 
            return USER_NOT_FOUND or INVALID_PASSWORD or SUCCESS
        */

        User user = userRepository.findByUsername(loginRequest.getUsername());
        if (user == null) {
            return LoginStatus.USER_NOT_FOUND;
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return LoginStatus.INVALID_PASSWORD;
        }

        // If the password is correct, update the lastLoginAt field
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return LoginStatus.SUCCESS;
    }


    public HashMap<String, String> getTokenById(UserLogin loginRequest) {
        /*Return the token of this user*/
        User user = userRepository.findByUsername(loginRequest.getUsername());
        
        if (user == null) { 
            return null; 
        }

        String userId = user.getId().toString();

        HashMap<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", jwtUtil.generateToken(userId));
        tokenMap.put("userId", userId);

        return tokenMap;
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }
    public User getUserByEmail(String email) {
    return userRepository.findByEmail(email); 
    }

    public User getUserByToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String userId = jwtUtil.extractUserId(token);
        return userRepository.findById(userId).orElse(null);
    }


    public void setStatus(String userId, UserStatus status) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        user.setStatus(status);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public String getUserIdByToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        return jwtUtil.extractUserId(token);
    }

    public void authenticateUser(String userId, String authHeader) {
        /* Authenticates the User by making sure the token provided and userId are the same*/
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String tokenUserId = jwtUtil.extractUserId(token);

        if (!tokenUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this user");
        }
    }
    
    public User updateUser(String userId, UserUpdate userUpdate) {
        /*Updates username/password if username is not already used*/
        User userById = userRepository.findById(userId).orElse(null);
        User userByUsername = userRepository.findByUsername(userUpdate.getUsername());
        if (userById == null) {
            return null;
        }

        if (userUpdate.getName() != null) {
            userById.setName(userUpdate.getName());
        }
        if (userByUsername != null && !userByUsername.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userUpdate.getUsername() != null) {
            userById.setUsername(userUpdate.getUsername());
        }
        if (userUpdate.getBirthday() != null) {
            userById.setBirthday(userUpdate.getBirthday());
        }

        if (userUpdate.getEmail() != null) {
            userById.setEmail(userUpdate.getEmail());
        }

        if (userUpdate.getPassword() != null) {
            userById.setPassword(passwordEncoder.encode(userUpdate.getPassword()));
        }
        
        return userRepository.save(userById);
    }

    public List<Project> getUserProjects(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return projectService.getProjectsByUserId(userId);
    }

    public void deleteProjectFromUser(String userId, String projectId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        user.getProjectIds().remove(projectId);
        userRepository.save(user);
    }

    public void addProjectIdToUser(String userId, String projectId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        user.getProjectIds().add(projectId);
     
        userRepository.save(user);
    }

    public List<User> getUserFriends(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        List<User> friends = new ArrayList<>();
        for (String friendId : user.getFriendsIds()) {
            User friend = userRepository.findById(friendId).orElse(null);
            if (friend != null) {
                friends.add(friend);
            }
        }
        return friends;
    }
    public void inviteFriend(String userId, String friendId, String authHeader) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        User friend = userRepository.findById(friendId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend not found"));
    
        // Check if request already exists
        if (user.getFriendRequestsSentIds().contains(friendId) || 
            friend.getFriendRequestsIds().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Friend request already exists");
        }
    
        // Update both sides of the relationship
        user.getFriendRequestsSentIds().add(friendId);
        friend.getFriendRequestsIds().add(userId);
    
        // Save both entities
        userRepository.saveAll(List.of(user, friend));
    
        changeService.markChange(userId, ChangeType.SENT_FRIEND_REQUEST, authHeader, true, friendId);
    }

    public void acceptFriend(String userId, String friendId, String authHeader) {
        User user = userRepository.findById(userId).orElse(null);
        User friend = userRepository.findById(friendId).orElse(null);
        if (user == null || friend == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or friend not found");
        }
        if (user.getFriendRequestsIds().contains(friendId)) {
            user.getFriendsIds().add(friendId);
            friend.getFriendsIds().add(userId);
            user.getFriendRequestsIds().remove(friendId);
            friend.getFriendRequestsSentIds().remove(userId);
            userRepository.save(user);
            userRepository.save(friend);
        }

        changeService.markChange(userId, ChangeType.ACCEPTED_FRIEND_REQUEST, authHeader, true, friendId);
    }

    public void rejectFriend(String userId, String friendId, String authHeader) {
        User user = userRepository.findById(userId).orElse(null);
        User friend = userRepository.findById(friendId).orElse(null);
        if (user == null || friend == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or friend not found");
        }
        if (user.getFriendRequestsIds().contains(friendId)) {
            user.getFriendRequestsIds().remove(friendId);
            friend.getFriendRequestsSentIds().remove(userId);
            userRepository.save(user);
            userRepository.save(friend);
        }

        changeService.markChange(userId, ChangeType.REJECTED_FRIEND_REQUEST, authHeader, true, friendId);
    }

    public void removeFriend(String userId, String friendId, String authHeader) {
        User user = userRepository.findById(userId).orElse(null);
        User friend = userRepository.findById(friendId).orElse(null);
        if (user == null || friend == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or friend not found");
        }
        if (user.getFriendsIds().contains(friendId)) {
            user.getFriendsIds().remove(friendId);
            friend.getFriendsIds().remove(userId);
            userRepository.save(user);
            userRepository.save(friend);
        }

        changeService.markChange(userId, ChangeType.REMOVED_FRIEND, authHeader, true, friendId);
    }

    public void cancelFriendRequest(String userId, String friendId) {
        User user = userRepository.findById(userId).orElse(null);
        User friend = userRepository.findById(friendId).orElse(null);
        if (user == null || friend == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or friend not found");
        }
        if (user.getFriendRequestsSentIds().contains(friendId)) {
            user.getFriendRequestsSentIds().remove(friendId);
            friend.getFriendRequestsIds().remove(userId);
            userRepository.save(user);
            userRepository.save(friend);
        }
    }
}