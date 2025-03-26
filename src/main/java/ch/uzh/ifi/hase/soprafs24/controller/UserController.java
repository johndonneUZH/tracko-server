package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.models.User;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class UserController {

  private final UserService userService;
  
  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @PreAuthorize("hasAuthority('USER')")
  public List<User> getUsers() {
    return userService.getUsers();
  }
  
  @GetMapping("/users/{userId}")
  @PreAuthorize("hasAuthority('USER')")
  public ResponseEntity<User> getUser(@PathVariable String userId) {
    User user = userService.getUserById(userId);
    if (user == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(user);
  }
}

