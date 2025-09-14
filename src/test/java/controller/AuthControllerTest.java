package controller;

import tracko.constant.LoginStatus;
import tracko.constant.UserStatus;
import tracko.models.user.User;
import tracko.models.user.UserLogin;
import tracko.models.user.UserRegister;
import tracko.service.UserService;
import tracko.auth.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ContextConfiguration;

@WebMvcTest(controllers = tracko.controller.AuthController.class)
@ContextConfiguration(classes = tracko.Application.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private User testUser;
    private UserRegister testUserRegister;
    private UserLogin testUserLogin;

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

        testUserRegister = new UserRegister();
        testUserRegister.setName("Test User");
        testUserRegister.setUsername("testuser");
        testUserRegister.setEmail("test@example.com");
        testUserRegister.setPassword("password");

        testUserLogin = new UserLogin();
        testUserLogin.setUsername("testuser");
        testUserLogin.setPassword("password");
    }

    @Test
    public void registerUser_validInput_userCreated() throws Exception {
        given(userService.checkIfUserExists(Mockito.any(UserRegister.class))).willReturn(false);
        given(userService.createUser(Mockito.any(UserRegister.class))).willReturn(testUser);

        MockHttpServletRequestBuilder postRequest = post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(testUserRegister));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(testUser.getId())))
                .andExpect(jsonPath("$.name", is(testUser.getName())))
                .andExpect(jsonPath("$.username", is(testUser.getUsername())))
                .andExpect(jsonPath("$.email", is(testUser.getEmail())));
    }

    @Test
    public void registerUser_duplicateUser_conflict() throws Exception {
        given(userService.checkIfUserExists(Mockito.any(UserRegister.class))).willReturn(true);

        MockHttpServletRequestBuilder postRequest = post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(testUserRegister));

        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());
    }

    @Test
    public void loginUser_validCredentials_success() throws Exception {
        given(userService.checkLoginRequest(Mockito.any(UserLogin.class))).willReturn(LoginStatus.SUCCESS);

        HashMap<String, String> userDetails = new HashMap<>();
        userDetails.put("token", "jwt-token");
        userDetails.put("userId", "1");
        given(userService.getTokenById(Mockito.any(UserLogin.class))).willReturn(userDetails);
        
        doNothing().when(userService).setStatus(anyString(), any(UserStatus.class));

        MockHttpServletRequestBuilder postRequest = post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(testUserLogin));

        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer jwt-token"))
                .andExpect(header().string("userId", "1"))
                .andExpect(content().string("Login successful"));
    }

    @Test
    public void loginUser_invalidCredentials_unauthorized() throws Exception {
        given(userService.checkLoginRequest(Mockito.any(UserLogin.class))).willReturn(LoginStatus.INVALID_PASSWORD);

        MockHttpServletRequestBuilder postRequest = post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(testUserLogin));

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid password"));
    }

    @Test
    public void loginUser_userNotFound_notFound() throws Exception {
        given(userService.checkLoginRequest(Mockito.any(UserLogin.class))).willReturn(LoginStatus.USER_NOT_FOUND);

        MockHttpServletRequestBuilder postRequest = post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(testUserLogin));

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    public void logoutUser_validToken_success() throws Exception {
        String userId = "1";
        given(userService.getUserIdByToken(anyString())).willReturn(userId);
        doNothing().when(userService).setStatus(userId, UserStatus.OFFLINE);

        MockHttpServletRequestBuilder postRequest = post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer jwt-token");

        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successful"));
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