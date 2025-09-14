package service;

import config.MongoTestConfig;
import tracko.repository.UserRepository;
import tracko.constant.UserStatus;
import tracko.models.user.User;
import tracko.models.user.UserRegister;
import tracko.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@SpringBootTest(classes = tracko.Application.class)
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
    userService.createUser(testUser);

    UserRegister testUser2 = new UserRegister();

    testUser2.setName("testName2");
    testUser2.setUsername("testUsername");
    testUser2.setPassword("testPassword2");
  }
}
