package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.models.user.User;
import ch.uzh.ifi.hase.soprafs24.models.user.UserUpdate;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.test.context.ActiveProfiles;
// import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.context.annotation.Import;

// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(UserController.class)
// @SpringBootTest
// @Import(MongoTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable Spring Security filters
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private User testUser;
    private UserUpdate testUserUpdate;

    @BeforeEach
    public void setup() {
        // Create test user
        testUser = new User();
        testUser.setId("1");
        testUser.setName("Test User");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setProjectIds(new ArrayList<>());

        // Create test user update
        testUserUpdate = new UserUpdate();
        testUserUpdate.setName("Updated User");
        testUserUpdate.setUsername("updateduser");
    }

    @Test
    public void getUsers_validRequest_success() throws Exception {
        // given
        List<User> allUsers = Arrays.asList(testUser);

        // Mock userService's getUsers method
        given(userService.getUsers()).willReturn(allUsers);

        // when/then
        MockHttpServletRequestBuilder getRequest = get("/users")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testUser.getId())))
                .andExpect(jsonPath("$[0].name", is(testUser.getName())))
                .andExpect(jsonPath("$[0].username", is(testUser.getUsername())));
    }

    @Test
    public void getUser_validId_userFound() throws Exception {
        // given
        given(userService.getUserById(testUser.getId())).willReturn(testUser);

        // when/then
        MockHttpServletRequestBuilder getRequest = get("/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId())))
                .andExpect(jsonPath("$.name", is(testUser.getName())))
                .andExpect(jsonPath("$.username", is(testUser.getUsername())));
    }

    @Test
    public void getUser_invalidId_notFound() throws Exception {
        // given
        String nonExistentId = "999";
        given(userService.getUserById(nonExistentId)).willReturn(null);

        // when/then
        MockHttpServletRequestBuilder getRequest = get("/users/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUser_validRequest_success() throws Exception {
        // given
        User updatedUser = new User();
        updatedUser.setId(testUser.getId());
        updatedUser.setName(testUserUpdate.getName());
        updatedUser.setUsername(testUserUpdate.getUsername());
        updatedUser.setEmail(testUser.getEmail());
        updatedUser.setStatus(testUser.getStatus());
        updatedUser.setProjectIds(testUser.getProjectIds());

        // Mock authentication and update
        doNothing().when(userService).authenticateUser(anyString(), anyString());
        // given(userService.updateUser(testUser.getId(), testUserUpdate)).willReturn(updatedUser);
        given(userService.updateUser(eq(testUser.getId()), any(UserUpdate.class)))
            .willReturn(updatedUser);

        // when/then
        MockHttpServletRequestBuilder putRequest = put("/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer validToken")
                .content(asJsonString(testUserUpdate));

        mockMvc.perform(putRequest)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(updatedUser.getId())))
                .andExpect(jsonPath("$.name", is(updatedUser.getName())))
                .andExpect(jsonPath("$.username", is(updatedUser.getUsername())));
    }

    @Test
    public void updateUser_authenticationFailed_forbidden() throws Exception {
        // given - authentication fails
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(userService).authenticateUser(anyString(), anyString());

        // when/then
        MockHttpServletRequestBuilder putRequest = put("/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer invalidToken")
                .content(asJsonString(testUserUpdate));

        mockMvc.perform(putRequest)
                .andExpect(status().isForbidden());

        // Verify updateUser was never called
        verify(userService, never()).updateUser(anyString(), any(UserUpdate.class));
    }

    /**
     * Helper Method to convert userPostDTO into a JSON string such that the input
     * can be processed Input will look like this: {"name": "Test User", "username":
     * "testUsername"}
     * 
     * @param object
     * @return string
     */
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}