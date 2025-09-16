package service;

import tracko.auth.JwtUtil;
import tracko.repository.UserRepository;
import tracko.constant.LoginStatus;
import tracko.constant.UserStatus;
import tracko.models.project.Project;
import tracko.models.user.User;
import tracko.models.user.UserLogin;
import tracko.models.user.UserRegister;
import tracko.models.user.UserUpdate;
import tracko.service.ChangeService;
import tracko.service.ProjectService;
import tracko.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ProjectService projectService;

    @Mock
    private ChangeService changeService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User testFriend;
    private UserRegister testUserRegister;
    private UserLogin testUserLogin;
    private UserUpdate testUserUpdate;
    private Project testProject;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        userService = new UserService(jwtUtil, projectService, userRepository, changeService);

        testUser = new User();
        testUser.setId("1");
        testUser.setName("Test User");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$ssssssssssssssssssssssssssssssssssssssssss"); // BCrypt hashed
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setProjectIds(new ArrayList<>());
        testUser.setFriendsIds(new ArrayList<>());
        testUser.setFriendRequestsIds(new ArrayList<>());
        testUser.setFriendRequestsSentIds(new ArrayList<>());
        testUser.setAvatarUrl("https://avatar.vercel.sh/testuser");
        testUser.setCreateAt(LocalDateTime.now());
        testUser.setLastLoginAt(LocalDateTime.now());

        testFriend = new User();
        testFriend.setId("2");
        testFriend.setName("Friend User");
        testFriend.setUsername("frienduser");
        testFriend.setEmail("friend@example.com");
        testFriend.setPassword("$2a$10$ffffffffffffffffffffffffffffffffffffff"); // BCrypt hashed
        testFriend.setStatus(UserStatus.ONLINE);
        testFriend.setProjectIds(new ArrayList<>());
        testFriend.setFriendsIds(new ArrayList<>());
        testFriend.setFriendRequestsIds(new ArrayList<>());
        testFriend.setFriendRequestsSentIds(new ArrayList<>());

        testUserRegister = new UserRegister();
        testUserRegister.setName("Test User");
        testUserRegister.setUsername("testuser");
        testUserRegister.setEmail("test@example.com");
        testUserRegister.setPassword("password");

        testUserLogin = new UserLogin();
        testUserLogin.setUsername("testuser");
        testUserLogin.setPassword("password");

        testUserUpdate = new UserUpdate();
        testUserUpdate.setName("Updated User");
        testUserUpdate.setUsername("updateduser");
        testUserUpdate.setBirthday("1, 2, 3");

        testProject = new Project();
        testProject.setProjectId("p1");
        testProject.setProjectName("Test Project");
    }

    @Test
    public void createUser_validInputs_success() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId("1");
            return savedUser;
        });

        User createdUser = userService.createUser(testUserRegister);

        assertEquals("1", createdUser.getId());
        assertEquals(testUserRegister.getUsername(), createdUser.getUsername());
        assertEquals(testUserRegister.getEmail(), createdUser.getEmail());
        assertEquals(UserStatus.ONLINE, createdUser.getStatus());
        
        assertNotNull(createdUser.getCreateAt());
        assertNotNull(createdUser.getLastLoginAt());
        assertEquals("https://avatar.vercel.sh/" + testUserRegister.getUsername(), createdUser.getAvatarUrl());
        assertTrue(createdUser.getFriendsIds().isEmpty());
        assertTrue(createdUser.getFriendRequestsIds().isEmpty());
        assertTrue(createdUser.getFriendRequestsSentIds().isEmpty());
    }

    @Test
    public void checkIfUserExists_existingUsername_returnsTrue() {
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);
        when(userRepository.findByEmail(anyString())).thenReturn(null);

        boolean exists = userService.checkIfUserExists(testUserRegister);

        assertTrue(exists);
    }

    @Test
    public void checkIfUserExists_existingEmail_returnsTrue() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(testUser);

        boolean exists = userService.checkIfUserExists(testUserRegister);

        assertTrue(exists);
    }

    @Test
    public void checkIfUserExists_newUser_returnsFalse() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(null);

        boolean exists = userService.checkIfUserExists(testUserRegister);

        assertFalse(exists);
    }

    @Test
    public void checkLoginRequest_validCredentials_updatesLastLoginAndReturnsSuccess() {
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);
        
        userService.checkLoginRequest(testUserLogin);
        
        verify(userRepository).findByUsername(eq(testUserLogin.getUsername()));
    }

    @Test
    public void checkLoginRequest_userNotFound_returnsUserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);

        LoginStatus status = userService.checkLoginRequest(testUserLogin);
        
        assertEquals(LoginStatus.USER_NOT_FOUND, status);
    }

    @Test
    public void getTokenById_validCredentials_returnsTokenMap() {
        when(userRepository.findByUsername(anyString())).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("fake.jwt.token");

        HashMap<String, String> tokenMap = userService.getTokenById(testUserLogin);
        
        assertNotNull(tokenMap);
        assertEquals("fake.jwt.token", tokenMap.get("token"));
        assertEquals(testUser.getId(), tokenMap.get("userId"));
    }

    @Test
    public void getTokenById_invalidUser_returnsNull() {
        when(userRepository.findByUsername(anyString())).thenReturn(null);

        HashMap<String, String> tokenMap = userService.getTokenById(testUserLogin);
        
        assertNull(tokenMap);
    }

    @Test
    public void getUserById_validId_returnsUser() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));

        User result = userService.getUserById("1");
        
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
    }

    @Test
    public void getUserById_invalidId_returnsNull() {
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        User result = userService.getUserById("999");
        
        assertNull(result);
    }

    @Test
    public void getUserByToken_validToken_returnsUser() {
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUserId(token)).thenReturn("1");
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));

        User result = userService.getUserByToken(authHeader);
        
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
    }

    @Test
    public void getUserByToken_invalidToken_throwsException() {
        String token = "invalid.jwt.token";
        String authHeader = "Bearer " + token;
        when(jwtUtil.validateToken(token)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> {
            userService.getUserByToken(authHeader);
        });
    }

    @Test
    public void getUserIdByToken_validToken_returnsUserId() {
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUserId(token)).thenReturn("1");

        String userId = userService.getUserIdByToken(authHeader);
        
        assertEquals("1", userId);
    }

    @Test
    public void getUserIdByToken_invalidToken_throwsException() {
        String token = "invalid.jwt.token";
        String authHeader = "Bearer " + token;
        when(jwtUtil.validateToken(token)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> {
            userService.getUserIdByToken(authHeader);
        });
    }

    @Test
    public void getUserIdByToken_invalidHeader_throwsException() {
        assertThrows(ResponseStatusException.class, () -> {
            userService.getUserIdByToken("InvalidHeader");
        });
    }

    @Test
    public void authenticateUser_validToken_success() {
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUserId(token)).thenReturn("1");

        assertDoesNotThrow(() -> {
            userService.authenticateUser("1", authHeader);
        });
    }

    @Test
    public void authenticateUser_differentUserId_throwsException() {
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUserId(token)).thenReturn("1");

        assertThrows(ResponseStatusException.class, () -> {
            userService.authenticateUser("2", authHeader);
        });
    }

    @Test
    public void updateUser_validUpdate_success() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername(testUserUpdate.getUsername())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser("1", testUserUpdate);

        assertNotNull(result);
        assertEquals(testUserUpdate.getName(), result.getName());
        assertEquals(testUserUpdate.getUsername(), result.getUsername());
        assertEquals(testUserUpdate.getBirthday(), result.getBirthday());
    }

    @Test
    public void updateUser_usernameConflict_throwsException() {
        User conflictingUser = new User();
        conflictingUser.setId("2");
        conflictingUser.setUsername(testUserUpdate.getUsername());

        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername(testUserUpdate.getUsername())).thenReturn(conflictingUser);

        assertThrows(ResponseStatusException.class, () -> {
            userService.updateUser("1", testUserUpdate);
        });
    }

    @Test
    public void setStatus_validUser_success() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> {
            userService.setStatus("1", UserStatus.OFFLINE);
        });

        verify(userRepository).save(any(User.class));
    }

    @Test
    public void setStatus_invalidUser_throwsException() {
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            userService.setStatus("999", UserStatus.OFFLINE);
        });
    }

    @Test
    public void getUserProjects_validUser_returnsProjects() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        List<Project> projects = List.of(testProject);
        when(projectService.getProjectsByUserId("1")).thenReturn(projects);

        List<Project> result = userService.getUserProjects("1");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProject.getProjectId(), result.get(0).getProjectId());
    }

    @Test
    public void getUserProjects_invalidUser_throwsException() {
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            userService.getUserProjects("999");
        });
    }

    @Test
    public void addProjectIdToUser_validUser_success() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> {
            userService.addProjectIdToUser("1", "p1");
        });

        verify(userRepository).save(any(User.class));
    }

    @Test
    public void deleteProjectFromUser_validUser_success() {
        testUser.getProjectIds().add("p1");
        
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> {
            userService.deleteProjectFromUser("1", "p1");
        });

        verify(userRepository).save(any(User.class));
    }

    @Test
    public void getUserFriends_validUser_returnsFriends() {
        testUser.getFriendsIds().add("2");
        
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("2")).thenReturn(Optional.of(testFriend));

        List<User> result = userService.getUserFriends("1");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testFriend.getId(), result.get(0).getId());
    }

    @Test
    public void inviteFriend_validUsers_success() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("2")).thenReturn(Optional.of(testFriend));
        when(userRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> {
            userService.inviteFriend("1", "2", "Bearer token");
        });

        verify(userRepository, times(1)).saveAll(anyList());
        ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
        verify(userRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    public void acceptFriend_validRequest_success() {
        testUser.getFriendRequestsIds().add("2");
        testFriend.getFriendRequestsSentIds().add("1");
        
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("2")).thenReturn(Optional.of(testFriend));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> {
            userService.acceptFriend("1", "2", "Bearer token");
        });

        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    public void rejectFriend_validRequest_success() {
        testUser.getFriendRequestsIds().add("2");
        testFriend.getFriendRequestsSentIds().add("1");
        
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("2")).thenReturn(Optional.of(testFriend));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> {
            userService.rejectFriend("1", "2", "Bearer token");
        });

        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    public void removeFriend_validFriendship_success() {
        testUser.getFriendsIds().add("2");
        testFriend.getFriendsIds().add("1");
        
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("2")).thenReturn(Optional.of(testFriend));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> {
            userService.removeFriend("1", "2", "Bearer token");
        });

        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    public void cancelFriendRequest_validRequest_success() {
        testUser.getFriendRequestsSentIds().add("2");
        testFriend.getFriendRequestsIds().add("1");
        
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("2")).thenReturn(Optional.of(testFriend));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> {
            userService.cancelFriendRequest("1", "2");
        });

        verify(userRepository, times(2)).save(any(User.class));
    }
}
