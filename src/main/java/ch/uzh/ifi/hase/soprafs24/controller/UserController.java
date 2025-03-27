package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.models.user.User;
import ch.uzh.ifi.hase.soprafs24.models.user.UserUpdate;


import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;
  
  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("")
  @PreAuthorize("hasAuthority('USER')")
  public List<User> getUsers() {
    return userService.getUsers();
  }
  
  @GetMapping("/{userId}")
  @PreAuthorize("hasAuthority('USER')")
  public ResponseEntity<User> getUser(@PathVariable String userId) {
    User user = userService.getUserById(userId);
    if (user == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    return ResponseEntity.status(HttpStatus.OK).body(user);
  }

  @PutMapping("/{userId}")
  @PreAuthorize("hasAuthority('USER')")
  public ResponseEntity<User> updateUser(
          @PathVariable String userId,
          @RequestBody UserUpdate updatedUser,
          @RequestHeader("Authorization") String authHeader) {
  
      userService.authenticateUser(userId, authHeader);
  
      User user = userService.updateUser(userId, updatedUser);
      if (user == null) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
  
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(user);
  }
  
}


