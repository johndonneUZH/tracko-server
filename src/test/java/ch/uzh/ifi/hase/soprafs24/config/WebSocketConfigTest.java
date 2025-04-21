package ch.uzh.ifi.hase.soprafs24.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WebSocketConfigTest {

    @Test
    public void testWebSocketConfiguration() {
        // Mock the WebSocketAuthInterceptor
        WebSocketAuthInterceptor mockInterceptor = Mockito.mock(WebSocketAuthInterceptor.class);
        WebSocketConfig webSocketConfig = new WebSocketConfig(mockInterceptor);
        
        // Mock the broker registry
        MessageBrokerRegistry mockRegistry = mock(MessageBrokerRegistry.class);
        webSocketConfig.configureMessageBroker(mockRegistry);
        
        verify(mockRegistry).enableSimpleBroker("/topic");
        verify(mockRegistry).setApplicationDestinationPrefixes("/app");

        // Mock the endpoint registry and registration
        StompEndpointRegistry mockEndpointRegistry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration mockRegistration = mock(StompWebSocketEndpointRegistration.class);

        // Setup the chain for mocking
        when(mockEndpointRegistry.addEndpoint("/ws")).thenReturn(mockRegistration);
        
        // Handle both potential method calls - use the one that matches your actual implementation
        when(mockRegistration.setAllowedOrigins(any(String.class))).thenReturn(mockRegistration);
        when(mockRegistration.setAllowedOriginPatterns(any(String.class))).thenReturn(mockRegistration);
        
        // Call the method being tested
        webSocketConfig.registerStompEndpoints(mockEndpointRegistry);

        // Verify method calls
        verify(mockEndpointRegistry).addEndpoint("/ws");
        
        // Verify either setAllowedOrigins OR setAllowedOriginPatterns was called
        // (choose the one that matches your actual implementation)
        try {
            verify(mockRegistration).setAllowedOrigins(any(String.class));
        } catch (Error e) {
            verify(mockRegistration).setAllowedOriginPatterns(any(String.class));
        }
        
        verify(mockRegistration).withSockJS();
    }
}