package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.models.User;
import org.springframework.security.access.prepost.PreAuthorize;
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
  
}

