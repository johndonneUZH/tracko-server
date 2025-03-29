package ch.uzh.ifi.hase.soprafs24;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.models.user.User;
import ch.uzh.ifi.hase.soprafs24.models.user.UserRegister;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.context.annotation.Import;
import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;


/**
 * Integration test for the entire user flow
 * This test requires the application to be running
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoTestConfig.class)
@ActiveProfiles("test") // Use test profile for MongoDB test container or in-memory DB
public class UserIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;
    
    private String baseUrl;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port;
        // Clean the repository before each test
        userRepository.deleteAll();
    }

    @Test
    public void testUserRegistrationLoginLogout() {
        // User registration data
        UserRegister newUser = new UserRegister();
        newUser.setName("Integration Test");
        newUser.setUsername("integrationtest");
        newUser.setEmail("integration@example.com");
        newUser.setPassword("password123");

        // Step 1: Register a new user
        ResponseEntity<User> registrationResponse = restTemplate.postForEntity(
                baseUrl + "/auth/register",
                newUser,
                User.class);

        assertEquals(HttpStatus.CREATED, registrationResponse.getStatusCode());
        assertNotNull(registrationResponse.getBody());
        assertEquals(newUser.getUsername(), registrationResponse.getBody().getUsername());
        assertEquals(UserStatus.ONLINE, registrationResponse.getBody().getStatus());

        // Step 2: Login with the registered user
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String loginBody = "{\"username\":\"" + newUser.getUsername() + "\",\"password\":\"" + newUser.getPassword() + "\"}";
        HttpEntity<String> loginRequest = new HttpEntity<>(loginBody, headers);
        
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                loginRequest,
                String.class);
        
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        
        // Extract token from headers
        HttpHeaders responseHeaders = loginResponse.getHeaders();
        String authHeader = responseHeaders.getFirst("Authorization");
        String userId = responseHeaders.getFirst("userId");
        
        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith("Bearer "));
        assertNotNull(userId);
        
        // Step 3: Get user profile using the token
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.add("Authorization", authHeader);
        HttpEntity<?> requestEntity = new HttpEntity<>(authHeaders);
        
        ResponseEntity<User> userResponse = restTemplate.exchange(
                baseUrl + "/users/" + userId,
                HttpMethod.GET,
                requestEntity,
                User.class);
                
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());
        assertEquals(newUser.getUsername(), userResponse.getBody().getUsername());
        
        // Step 4: Logout the user
        ResponseEntity<String> logoutResponse = restTemplate.exchange(
                baseUrl + "/auth/logout",
                HttpMethod.POST,
                requestEntity,
                String.class);
                
        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());
        
        // Verify user status is now OFFLINE
        User loggedOutUser = userRepository.findByUsername(newUser.getUsername());
        assertEquals(UserStatus.OFFLINE, loggedOutUser.getStatus());
    }
}