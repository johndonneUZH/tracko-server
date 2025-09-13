package models.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import constant.UserStatus;
import models.user.User;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for the User entity
 */
public class UserTest {

    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId("1");
        testUser.setName("Test User");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setProjectIds(new ArrayList<>());
    }

    @Test
    public void testGettersAndSetters() {
        assertEquals("1", testUser.getId());
        assertEquals("Test User", testUser.getName());
        assertEquals("testuser", testUser.getUsername());
        assertEquals("test@example.com", testUser.getEmail());
        assertEquals("password", testUser.getPassword());
        assertEquals(UserStatus.ONLINE, testUser.getStatus());
        assertNotNull(testUser.getProjectIds());
        assertEquals(0, testUser.getProjectIds().size());
    }

    @Test
    public void testProjectIdsManipulation() {
        testUser.setProjectIds(Arrays.asList("project1", "project2"));
        
        assertEquals(2, testUser.getProjectIds().size());
        assertEquals("project1", testUser.getProjectIds().get(0));
        assertEquals("project2", testUser.getProjectIds().get(1));
    }
}