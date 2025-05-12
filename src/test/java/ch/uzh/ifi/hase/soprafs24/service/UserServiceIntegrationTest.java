package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.models.user.User;
import ch.uzh.ifi.hase.soprafs24.models.user.UserRegister;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class UserServiceIntegrationTest {

  @Qualifier("userRepository")
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @BeforeEach
  public void setup() {
    userRepository.deleteAll();
  }

  @Test
  public void createUser_validInputs_success() {
    assertNull(userRepository.findByUsername("testUsername"));

    UserRegister testUser = new UserRegister();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");

    User createdUser = userService.createUser(testUser);

    assertEquals(testUser.getName(), createdUser.getName());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
  }

  @Test
  public void createUser_duplicateUsername_throwsException() {
    assertNull(userRepository.findByUsername("testUsername"));

    UserRegister testUser = new UserRegister();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    User createdUser = userService.createUser(testUser);

    UserRegister testUser2 = new UserRegister();

    testUser2.setName("testName2");
    testUser2.setUsername("testUsername");
    testUser2.setPassword("testPassword2");
  }
}
