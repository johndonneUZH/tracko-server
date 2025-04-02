package ch.uzh.ifi.hase.soprafs24.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.Mockito.*;

@SpringBootTest
public class WebSocketConfigTest {

    @Autowired
    private WebSocketConfig webSocketConfig;

    @Test
    public void testWebSocketConfiguration() {
        // Create the config instance manually
        WebSocketConfig webSocketConfig = new WebSocketConfig();
        
        // Mock the broker registry
        MessageBrokerRegistry mockRegistry = mock(MessageBrokerRegistry.class);
        webSocketConfig.configureMessageBroker(mockRegistry);
        
        verify(mockRegistry).enableSimpleBroker("/topic");
        verify(mockRegistry).setApplicationDestinationPrefixes("/app");

        // For the endpoint registry, we need to mock the chain
        StompEndpointRegistry mockEndpointRegistry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration mockRegistration = mock(StompWebSocketEndpointRegistration.class);

        // Setup the chain
        when(mockEndpointRegistry.addEndpoint("/ws")).thenReturn(mockRegistration);
        when(mockRegistration.setAllowedOrigins("*")).thenReturn(mockRegistration);

        webSocketConfig.registerStompEndpoints(mockEndpointRegistry);
        
        // Verify the expected methods were called
        verify(mockEndpointRegistry).addEndpoint("/ws");
        verify(mockRegistration).setAllowedOrigins("*");
        verify(mockRegistration).withSockJS();
    }
}