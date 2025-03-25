package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.repository.Repository;
import ch.uzh.ifi.hase.soprafs24.models.UserRegister;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
public class UserController {
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private Repository repository;

  @GetMapping("/users")
  @PreAuthorize("hasAuthority('USER')")
  public List<UserRegister> getUsers() {
    return repository.findAll();
  }

  @PostMapping("/users")
  public ResponseEntity<UserRegister> createUser(@RequestBody UserRegister newUser) {
      UserRegister savedUser = repository.save(newUser);
      UserGet fullUser
      
      // Return the saved user WITH the generated ID
      return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
  }
}

