package ch.uzh.ifi.hase.soprafs24.repository;
import java.util.Optional;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.models.user.User;

@SpringBootTest
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    public void setup() {
        // Clear the test database before each test
        userRepository.deleteAll();
    }
    
    @Test
    public void testMongoConnection() {
        // Create a test user with timestamp to make it unique
        User testUser = new User();
        String uniqueUsername = "test-user-" + System.currentTimeMillis();
        testUser.setName("Test User");
        testUser.setUsername(uniqueUsername);
        testUser.setEmail(uniqueUsername + "@example.com");
        testUser.setPassword("password");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setProjectIds(new ArrayList<>());
        
        // Save to repository
        User savedUser = userRepository.save(testUser);
        
        // Print info
        System.out.println("SAVED USER WITH USERNAME: " + savedUser.getUsername());
        
        // Verify the user was saved
        User retrievedUser = userRepository.findByUsername(uniqueUsername);
        assertNotNull(retrievedUser);
        assertEquals(uniqueUsername, retrievedUser.getUsername());
    }

    @Test
    public void findByUsername_success() {
        // given
        User user = new User();
        user.setName("Test User");
        user.setUsername("testUsername");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setStatus(UserStatus.ONLINE);
        user.setProjectIds(new ArrayList<>());
        
        userRepository.save(user);

        // when
        User found = userRepository.findByUsername(user.getUsername());

        // then
        assertNotNull(found);
        assertEquals(found.getUsername(), user.getUsername());
        assertEquals(found.getEmail(), user.getEmail());
    }

    @Test
    public void findByEmail_success() {
        // given
        User user = new User();
        user.setName("Test User");
        user.setUsername("testUsername");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setStatus(UserStatus.ONLINE);
        user.setProjectIds(new ArrayList<>());
        
        userRepository.save(user);

        // when
        User found = userRepository.findByEmail(user.getEmail());

        // then
        assertNotNull(found);
        assertEquals(found.getUsername(), user.getUsername());
        assertEquals(found.getEmail(), user.getEmail());
    }

    @Test
    public void findById_success() {
        // given
        User user = new User();
        user.setName("Test User");
        user.setUsername("testUsername");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setStatus(UserStatus.ONLINE);
        user.setProjectIds(new ArrayList<>());
        
        User savedUser = userRepository.save(user);

        // when
        Optional<User> found = userRepository.findById(savedUser.getId());

        // then
        assertTrue(found.isPresent());
        assertEquals(found.get().getUsername(), user.getUsername());
        assertEquals(found.get().getEmail(), user.getEmail());
    }

    @Test
    public void existsByUsername_success() {
        // given
        User user = new User();
        user.setName("Test User");
        user.setUsername("testUsername");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setStatus(UserStatus.ONLINE);
        user.setProjectIds(new ArrayList<>());
        
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByUsername(user.getUsername());

        // then
        assertTrue(exists);
    }

    @Test
    public void existsByEmail_success() {
        // given
        User user = new User();
        user.setName("Test User");
        user.setUsername("testUsername");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setStatus(UserStatus.ONLINE);
        user.setProjectIds(new ArrayList<>());
        
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail(user.getEmail());

        // then
        assertTrue(exists);
    }
}