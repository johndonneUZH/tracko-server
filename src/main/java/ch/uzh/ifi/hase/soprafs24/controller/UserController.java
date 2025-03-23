package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.repository.Repository;
import ch.uzh.ifi.hase.soprafs24.models.Userrr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  @Autowired
  private Repository repository;

  @GetMapping("/users")
  public List<Userrr> getUsers() {
    return repository.findAll();
  }

  @PostMapping("/users")
  public Userrr createUser(@RequestBody Userrr newUser) {
    return repository.save(newUser);
  }
}

