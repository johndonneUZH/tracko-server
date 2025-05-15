package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.models.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;

@DataMongoTest
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