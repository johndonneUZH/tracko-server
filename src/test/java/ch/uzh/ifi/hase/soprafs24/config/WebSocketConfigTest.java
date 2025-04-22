package ch.uzh.ifi.hase.soprafs24.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.SockJsServiceRegistration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
        // Add this line to mock the SockJsServiceRegistration
        SockJsServiceRegistration mockSockJsRegistration = mock(SockJsServiceRegistration.class);

        // Setup the chain for mocking
        when(mockEndpointRegistry.addEndpoint("/ws")).thenReturn(mockRegistration);
        
        // Handle both potential method calls
        when(mockRegistration.setAllowedOrigins(any(String.class))).thenReturn(mockRegistration);
        when(mockRegistration.setAllowedOriginPatterns(any(String.class))).thenReturn(mockRegistration);
        
        // Mock the withSockJS() call to return our mock SockJsServiceRegistration
        when(mockRegistration.withSockJS()).thenReturn(mockSockJsRegistration);
        // Mock the setSupressCors call if needed
        when(mockSockJsRegistration.setSupressCors(anyBoolean())).thenReturn(mockSockJsRegistration);

        // Call the method being tested
        webSocketConfig.registerStompEndpoints(mockEndpointRegistry);

        // Verify method calls
        verify(mockEndpointRegistry).addEndpoint("/ws");
        
        // Verify either setAllowedOrigins OR setAllowedOriginPatterns was called
        try {
            verify(mockRegistration).setAllowedOrigins(any(String.class));
        } catch (Error e) {
            verify(mockRegistration).setAllowedOriginPatterns(any(String.class));
        }
        
        verify(mockRegistration).withSockJS();
        // Verify setSupressCors was called if that's part of your implementation
        verify(mockSockJsRegistration).setSupressCors(anyBoolean());
    }
}