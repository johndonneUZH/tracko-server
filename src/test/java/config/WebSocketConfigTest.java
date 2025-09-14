package config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.SockJsServiceRegistration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import tracko.config.WebSocketAuthInterceptor;
import tracko.config.WebSocketConfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class WebSocketConfigTest {

    @Test
    public void testWebSocketConfiguration() {
        WebSocketAuthInterceptor mockInterceptor = Mockito.mock(WebSocketAuthInterceptor.class);
        WebSocketConfig webSocketConfig = new WebSocketConfig(mockInterceptor);
        
        MessageBrokerRegistry mockRegistry = mock(MessageBrokerRegistry.class);
        webSocketConfig.configureMessageBroker(mockRegistry);
        
        verify(mockRegistry).enableSimpleBroker("/topic", "/queue");
        verify(mockRegistry).setApplicationDestinationPrefixes("/app");
        verify(mockRegistry).setUserDestinationPrefix("/user");

        StompEndpointRegistry mockEndpointRegistry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration mockRegistration = mock(StompWebSocketEndpointRegistration.class);
        SockJsServiceRegistration mockSockJsRegistration = mock(SockJsServiceRegistration.class);

        when(mockEndpointRegistry.addEndpoint("/ws")).thenReturn(mockRegistration);
        when(mockRegistration.setAllowedOrigins(any(String[].class))).thenReturn(mockRegistration);
        when(mockRegistration.setAllowedOriginPatterns(any(String[].class))).thenReturn(mockRegistration);
        when(mockRegistration.withSockJS()).thenReturn(mockSockJsRegistration);
        when(mockSockJsRegistration.setHeartbeatTime(anyLong())).thenReturn(mockSockJsRegistration);

        webSocketConfig.registerStompEndpoints(mockEndpointRegistry);

        verify(mockEndpointRegistry).addEndpoint("/ws");
        // Verify either setAllowedOrigins OR setAllowedOriginPatterns was called
        try {
            verify(mockRegistration).setAllowedOrigins(any(String[].class));
        } catch (Error e) {
            verify(mockRegistration).setAllowedOriginPatterns(any(String[].class));
        }
        verify(mockRegistration).withSockJS();
        verify(mockSockJsRegistration).setHeartbeatTime(4000L);
    }
}