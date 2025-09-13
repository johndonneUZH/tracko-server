package controller;

import constant.LoginStatus;
import constant.UserStatus;
import models.user.User;
import models.user.UserLogin;
import models.user.UserRegister;
import models.user.UserUpdate;
import service.UserService;

import java.util.Map;
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
    public ResponseEntity<Object> registerUser(@RequestBody UserRegister newUser) {
        if (userService.checkIfUserExists(newUser)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "User already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        User savedUser = userService.createUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }


    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserLogin loginRequest) {
    
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
        final String USER_ID_KEY = "userId";

        userService.setStatus(userDetails.get(USER_ID_KEY), UserStatus.ONLINE);
        return ResponseEntity.status(HttpStatus.OK)
                             .header("Authorization", "Bearer " + userDetails.get("token"))
                             .header(USER_ID_KEY, userDetails.get(USER_ID_KEY))
                             .body("Login successful");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestHeader("Authorization") String authHeader) {
        String userId = userService.getUserIdByToken(authHeader);
        userService.setStatus(userId, UserStatus.OFFLINE);
        return ResponseEntity.status(HttpStatus.OK).body("Logout successful");
    }
    @PostMapping("/check-email")
    public ResponseEntity<Object> checkEmailExists(@RequestBody HashMap<String, String> request) {
    String email = request.get("email");

    if (email == null || email.isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is required");
    }

    User user = userService.getUserByEmail(email);

    if (user != null) {
        Map<String, Object> response = new HashMap<>();
        response.put("exists", true);
        response.put("username", user.getUsername());
        return ResponseEntity.ok(response);
    } else {
        Map<String, Object> response = new HashMap<>();
        response.put("exists", false);
        return ResponseEntity.ok(response);
    }
}

@PutMapping("/reset-password-with-otp")
    public ResponseEntity<String> resetPasswordWithOtp(@RequestBody HashMap<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
    
        if (email == null || email.isEmpty() || otp == null || otp.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email and OTP are required");
        }
    
        User existingUser = userService.getUserByEmail(email);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No account found with that email address.");
        }
    
        UserUpdate userUpdate = new UserUpdate();
        userUpdate.setPassword(otp); 
    
        userService.updateUser(existingUser.getId(), userUpdate);
    
        return ResponseEntity.status(HttpStatus.OK).body("Password reset successfully. Use your OTP as the new password to log in.");
    }
    
}
