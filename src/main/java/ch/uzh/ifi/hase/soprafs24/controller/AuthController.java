package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.models.user.User;
import ch.uzh.ifi.hase.soprafs24.models.user.UserLogin;
import ch.uzh.ifi.hase.soprafs24.models.user.UserRegister;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.constant.LoginStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegister newUser) {

        if (userService.checkIfUserExists(newUser)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }

        User savedUser = userService.createUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLogin loginRequest) {
    
        LoginStatus status = userService.checkLoginRequest(loginRequest);
    
        switch (status) {
            case USER_NOT_FOUND:
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            case INVALID_PASSWORD:
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
            case SUCCESS:
                break;
        }
    
        // Only runs if status == SUCCESS
        HashMap<String, String> userDetails = userService.getTokenById(loginRequest);
        userService.setStatus(userDetails.get("userId"), UserStatus.ONLINE);
        return ResponseEntity.status(HttpStatus.OK)
                             .header("Authorization", "Bearer " + userDetails.get("token"))
                             .header("userId", userDetails.get("userId"))
                             .body("Login successful");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String authHeader) {
        String userId = userService.getUserIdByToken(authHeader);
        userService.setStatus(userId, UserStatus.OFFLINE);
        return ResponseEntity.status(HttpStatus.OK).body("Logout successful");
    }
    
}
