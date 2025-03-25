package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.models.User;
import ch.uzh.ifi.hase.soprafs24.models.UserLogin;
import ch.uzh.ifi.hase.soprafs24.models.UserRegister;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

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
        
        if (!userService.checkLoginRequest(loginRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        
        String token = userService.getTokenById(loginRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).header("Authorization", "Bearer " + token).body("Login successful");
    }
}
