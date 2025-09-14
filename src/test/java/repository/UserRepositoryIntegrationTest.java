package repository;

import tracko.constant.UserStatus;
import tracko.models.user.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import config.MongoTestConfig;
import tracko.repository.UserRepository;

@SpringBootTest(classes = {MongoTestConfig.class})
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class UserRepositoryIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  public void findByUsername_success() {
    User user = new User();
    user.setUsername("Firstname Lastname");
    user.setStatus(UserStatus.OFFLINE);

    userRepository.save(user);

    User found = userRepository.findByUsername(user.getUsername());

    assertNotNull(found.getId());
    assertEquals(found.getUsername(), user.getUsername());
    assertEquals(found.getStatus(), user.getStatus());
  }
}