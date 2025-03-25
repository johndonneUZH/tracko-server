package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.models.UserRegister;
import ch.uzh.ifi.hase.soprafs24.models.UserLogin;
import ch.uzh.ifi.hase.soprafs24.repository.Repository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final Repository repository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(Repository repository, JwtUtil jwtUtil) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegister newUser) {
        if (repository.findByUsername(newUser.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword())); // Hash password
        UserRegister savedUser = repository.save(newUser);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLogin loginRequest) {
        UserRegister user = repository.findByUsername(loginRequest.getUsername());

        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        String token = jwtUtil.generateToken(String.valueOf(user.getId()));

        return ResponseEntity.ok().header("Authorization", "Bearer " + token).body("Login successful");
    }
}
