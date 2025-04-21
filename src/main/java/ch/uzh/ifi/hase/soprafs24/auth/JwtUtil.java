package ch.uzh.ifi.hase.soprafs24.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Collections;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 3; // 3 hours

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        String paddedSecret = secret.length() < 32 ? 
            String.format("%-32s", secret).replace(' ', 'X') : 
            secret.substring(0, 32);
        this.secretKey = Keys.hmacShaKeyFor(paddedSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Authentication getAuthentication(String token) {
        String userId = extractUserId(token);
        return new UsernamePasswordAuthenticationToken(
            userId, 
            null,
            Collections.singletonList(new SimpleGrantedAuthority("USER"))
        );
    }
}