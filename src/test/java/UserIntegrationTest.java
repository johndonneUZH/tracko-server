

import tracko.repository.UserRepository;
import tracko.constant.UserStatus;
import tracko.models.user.User;
import tracko.models.user.UserRegister;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.context.annotation.Import;
import config.MongoTestConfig;
import tracko.Application;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoTestConfig.class)
@ActiveProfiles("test")
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
        userRepository.deleteAll();
    }

    @Test
    public void testUserRegistrationLoginLogout() {
        UserRegister newUser = new UserRegister();
        newUser.setName("Integration Test");
        newUser.setUsername("integrationtest");
        newUser.setEmail("integration@example.com");
        newUser.setPassword("password123");
    
        ResponseEntity<String> registrationResponse = restTemplate.postForEntity(
                baseUrl + "/auth/register",
                newUser,
                String.class);
    
        assertEquals(HttpStatus.CREATED, registrationResponse.getStatusCode());
    
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String loginBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}",
                newUser.getUsername(), newUser.getPassword());
        HttpEntity<String> loginRequest = new HttpEntity<>(loginBody, headers);
    
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                loginRequest,
                String.class);
    
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertEquals("Login successful", loginResponse.getBody());
    
        String token = loginResponse.getHeaders().getFirst("Authorization");
        String userId = loginResponse.getHeaders().getFirst("userId");
    
        assertNotNull(token);
        assertTrue(token.startsWith("Bearer "));
        assertNotNull(userId);
    
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(token.substring(7)); // Strip "Bearer "
        HttpEntity<Void> getRequest = new HttpEntity<>(authHeaders);
    
        ResponseEntity<String> userResponse = restTemplate.exchange(
                baseUrl + "/users/" + userId,
                HttpMethod.GET,
                getRequest,
                String.class);
    
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());
    
        String body = userResponse.getBody();
        assertNotNull(body);
        assertTrue(body.contains("\"username\":\"integrationtest\""));
        assertTrue(body.contains("\"status\":\"ONLINE\""));
    
        ResponseEntity<String> logoutResponse = restTemplate.exchange(
                baseUrl + "/auth/logout",
                HttpMethod.POST,
                getRequest,
                String.class);
    
        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());
        assertEquals("Logout successful", logoutResponse.getBody());
    
        User userInDb = userRepository.findByUsername(newUser.getUsername());
        assertEquals(UserStatus.OFFLINE, userInDb.getStatus());
    }    
}