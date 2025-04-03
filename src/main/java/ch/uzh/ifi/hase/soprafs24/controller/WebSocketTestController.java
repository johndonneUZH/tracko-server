package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
// import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketTestController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handles test messages from clients and echoes them back
     * Client should send to: /app/test-message
     */
    @MessageMapping("/test-message")
    public void processTestMessage(String message) {
        System.out.println("Received test message: " + message);
        
        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("type", "ECHO");
        response.put("originalMessage", message);
        response.put("serverTimestamp", LocalDateTime.now().toString());
        response.put("content", "Server received your message: " + message);
        
        // Send response to a topic that clients can subscribe to
        messagingTemplate.convertAndSend("/topic/test-responses", response);
    }
    
    /**
     * REST endpoint to test server-initiated message broadcast
     * Can be used to test if clients are properly subscribed
     */
    @org.springframework.web.bind.annotation.GetMapping("/api/test-broadcast")
    public Map<String, Object> testBroadcast() {
        Map<String, Object> broadcastMessage = new HashMap<>();
        broadcastMessage.put("type", "BROADCAST_TEST");
        broadcastMessage.put("serverTimestamp", LocalDateTime.now().toString());
        broadcastMessage.put("content", "This is a test broadcast from the server");
        
        // Broadcast the message
        messagingTemplate.convertAndSend("/topic/test-responses", broadcastMessage);
        
        // Return confirmation to the REST caller
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Test broadcast sent");
        return response;
    }
}