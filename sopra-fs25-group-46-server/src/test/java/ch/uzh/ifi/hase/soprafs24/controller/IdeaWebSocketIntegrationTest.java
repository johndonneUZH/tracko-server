// package ch.uzh.ifi.hase.soprafs24;

// // Spring WebSocket imports
// import org.springframework.messaging.converter.MappingJackson2MessageConverter;
// import org.springframework.messaging.simp.stomp.StompFrameHandler;
// import org.springframework.messaging.simp.stomp.StompHeaders;
// import org.springframework.messaging.simp.stomp.StompSession;
// import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
// import org.springframework.web.socket.client.standard.StandardWebSocketClient;
// import org.springframework.web.socket.messaging.WebSocketStompClient;
// import org.springframework.web.socket.sockjs.client.SockJsClient;
// import org.springframework.web.socket.sockjs.client.Transport;
// import org.springframework.web.socket.sockjs.client.WebSocketTransport;

// // Spring test and REST imports
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.boot.test.web.server.LocalServerPort;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpMethod;
// import org.springframework.context.annotation.Import;
// import org.springframework.test.context.ActiveProfiles;

// // JUnit and test assertions
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.junit.jupiter.api.Assertions.fail;

// // Java utilities
// import org.springframework.beans.factory.annotation.Autowired;
// import java.lang.reflect.Type;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.ExecutionException;
// import java.util.concurrent.TimeUnit;
// import java.util.concurrent.TimeoutException;

// // Application-specific imports
// import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
// import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;
// import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaRegister;
// import ch.uzh.ifi.hase.soprafs24.models.websocket.IdeaUpdateMessage;
// import ch.uzh.ifi.hase.soprafs24.config.MongoTestConfig;

// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import ch.uzh.ifi.hase.soprafs24.models.project.Project;
// import ch.uzh.ifi.hase.soprafs24.models.project.ProjectRegister;
// import ch.uzh.ifi.hase.soprafs24.models.user.User;
// import ch.uzh.ifi.hase.soprafs24.models.user.UserRegister;

// import org.springframework.messaging.simp.SimpMessagingTemplate;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @Import(MongoTestConfig.class)
// @ActiveProfiles("test")
// public class IdeaWebSocketIntegrationTest {

//     @LocalServerPort
//     private int port;

//     @Autowired
//     private TestRestTemplate restTemplate;

//     @Autowired
//     private JwtUtil jwtUtil;  // To generate test tokens

//     private WebSocketStompClient stompClient;
//     private String websocketUrl;
//     private CompletableFuture<IdeaUpdateMessage> completableFuture;
//     private String baseUrl;

    
//     // Inject the messaging template and send a test message
//     @Autowired
//     private SimpMessagingTemplate testTemplate;

//     @BeforeEach
//     public void setup() {
//         System.out.println("====== TEST SETUP RUNNING ======");

//         baseUrl = "http://localhost:" + port;
//         websocketUrl = "ws://localhost:" + port + "/ws";
//         completableFuture = new CompletableFuture<>();

//         // Setup STOMP client
//         List<Transport> transports = new ArrayList<>();
//         transports.add(new WebSocketTransport(new StandardWebSocketClient()));
//         SockJsClient sockJsClient = new SockJsClient(transports);

//         stompClient = new WebSocketStompClient(sockJsClient);
//         stompClient.setMessageConverter(new MappingJackson2MessageConverter());

//         System.out.println("SimpMessagingTemplate available: " + (testTemplate != null));
//     }

//     @Test
//     public void testSimpleWebSocketMessage() throws Exception {
//         // Connect to WebSocket
//         StompSession session = stompClient
//                 .connect(websocketUrl, new StompSessionHandlerAdapter() {})
//                 .get(5, TimeUnit.SECONDS);

//         // Subscribe to a test topic
//         CompletableFuture<String> simpleFuture = new CompletableFuture<>();
//         session.subscribe("/topic/test", new StompFrameHandler() {
//             @Override
//             public Type getPayloadType(StompHeaders headers) {
//                 return String.class;
//             }

//             @Override
//             public void handleFrame(StompHeaders headers, Object payload) {
//                 simpleFuture.complete((String) payload);
//             }
//         });

        
//         testTemplate.convertAndSend("/topic/test", "Test Message");

//         // Check if message is received
//         String message = simpleFuture.get(5, TimeUnit.SECONDS);
//         assertEquals("Test Message", message);
//     }

//     // @Test
//     // public void testIdeaCreationWebSocketMessage() throws Exception {
//     //     // First, create a user and project in the database that we can reference
//     //     String userId = createTestUser();
//     //     String projectId = createTestProject(userId);
        
//     //     // Now generate a token for this real user
//     //     String testToken = jwtUtil.generateToken(userId);
        
//     //     // Connect to WebSocket
//     //     StompSession session = stompClient
//     //             .connect(websocketUrl, new StompSessionHandlerAdapter() {})
//     //             .get(5, TimeUnit.SECONDS);

//     //     // Subscribe to the topic
//     //     session.subscribe("/topic/projects/" + projectId + "/ideas", new StompFrameHandler() {
//     //         @Override
//     //         public Type getPayloadType(StompHeaders headers) {
//     //             return IdeaUpdateMessage.class;
//     //         }

//     //         @Override
//     //         public void handleFrame(StompHeaders headers, Object payload) {
//     //             completableFuture.complete((IdeaUpdateMessage) payload);
//     //         }
//     //     });

//     //     // Create test data
//     //     IdeaRegister ideaToCreate = new IdeaRegister();
//     //     ideaToCreate.setIdeaName("Test WebSocket Idea");
//     //     ideaToCreate.setIdeaDescription("This idea should trigger a WebSocket message");
        
//     //     // Create HTTP headers with authentication
//     //     HttpHeaders headers = new HttpHeaders();
//     //     headers.set("Authorization", "Bearer " + testToken);
//     //     headers.setContentType(MediaType.APPLICATION_JSON);

//     //     // Create the HTTP entity with headers and body
//     //     HttpEntity<IdeaRegister> request = new HttpEntity<>(ideaToCreate, headers);
        
//     //     // Make the REST call to create the idea and print the response
//     //     ResponseEntity<Idea> response = restTemplate.exchange(
//     //         baseUrl + "/projects/{projectId}/ideas",
//     //         HttpMethod.POST,
//     //         request,
//     //         Idea.class,
//     //         projectId
//     //     );
        
//     //     // Check if the HTTP request was successful
//     //     assertTrue(response.getStatusCode().is2xxSuccessful(), 
//     //             "HTTP request failed with status: " + response.getStatusCode());

//     //     // Wait for the WebSocket message and verify its content
//     //     try {
//     //         IdeaUpdateMessage receivedMessage = completableFuture.get(10, TimeUnit.SECONDS);
//     //         assertNotNull(receivedMessage);
//     //         assertEquals("CREATE", receivedMessage.getAction());
//     //         assertEquals(projectId, receivedMessage.getProjectId());
//     //         assertNotNull(receivedMessage.getIdeaId());
//     //         assertEquals("Test WebSocket Idea", receivedMessage.getIdea().getIdeaName());
//     //     } catch (TimeoutException e) {
//     //         fail("WebSocket message was not received within the timeout period. Check if the WebSocket message is being sent properly.");
//     //     }
//     // }

//     // // Helper methods to set up test data
//     // private String createTestUser() {
//     //     // Create a user and return its ID
//     //     UserRegister userRegister = new UserRegister();
//     //     userRegister.setName("Test User");
//     //     userRegister.setUsername("testuser" + System.currentTimeMillis());
//     //     userRegister.setEmail("test" + System.currentTimeMillis() + "@example.com");
//     //     userRegister.setPassword("password");
        
//     //     ResponseEntity<User> response = restTemplate.postForEntity(
//     //         baseUrl + "/auth/register",
//     //         userRegister,
//     //         User.class
//     //     );
        
//     //     assertTrue(response.getStatusCode().is2xxSuccessful(), 
//     //             "Failed to create test user: " + response.getStatusCode());
        
//     //     return response.getBody().getId();
//     // }

//     // private String createTestProject(String userId) {
//     //     // Create a project and return its ID
//     //     ProjectRegister projectRegister = new ProjectRegister();
//     //     projectRegister.setProjectName("Test Project" + System.currentTimeMillis());
//     //     projectRegister.setProjectDescription("Test Description");
        
//     //     HttpHeaders headers = new HttpHeaders();
//     //     headers.set("Authorization", "Bearer " + jwtUtil.generateToken(userId));
        
//     //     HttpEntity<ProjectRegister> request = new HttpEntity<>(projectRegister, headers);
        
//     //     ResponseEntity<Project> response = restTemplate.exchange(
//     //         baseUrl + "/projects",
//     //         HttpMethod.POST,
//     //         request,
//     //         Project.class
//     //     );
        
//     //     assertTrue(response.getStatusCode().is2xxSuccessful(), 
//     //             "Failed to create test project: " + response.getStatusCode());
        
//     //     return response.getBody().getProjectId();
//     // }
// }

