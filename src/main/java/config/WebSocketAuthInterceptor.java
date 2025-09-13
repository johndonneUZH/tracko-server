package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import auth.JwtUtil;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    public WebSocketAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message, StompHeaderAccessor.class);
        
        if (accessor != null && accessor.getCommand() != null) {
            logger.debug("Processing STOMP command: {}", accessor.getCommand());
            
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String token = extractToken(accessor);
                logger.debug("Extracted token: {}", token);
                
                if (token == null) {
                    logger.warn("No token provided in CONNECT");
                    throw new AuthenticationCredentialsNotFoundException("No auth token");
                }
                
                if (!jwtUtil.validateToken(token)) {
                    logger.warn("Invalid token provided");
                    throw new AuthenticationCredentialsNotFoundException("Invalid token");
                }
                
                Authentication auth = jwtUtil.getAuthentication(token);
                accessor.setUser(auth);
                logger.debug("Authenticated user: {}", auth.getName());
            }
        }
        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}