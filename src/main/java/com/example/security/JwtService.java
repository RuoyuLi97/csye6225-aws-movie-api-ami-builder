package com.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParserBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {
    private final String secretKeyValue = "your-256-bit-secret1234567890123";
    
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = new SecretKeySpec(secretKeyValue.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String generateToken(String email) {
        System.out.println("Generating token for email: " + email);
        return Jwts.builder()
                .subject(email)
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            System.out.println("Validating token: " + token);

            Claims claims = extractClaims(token);
            String email = claims.getSubject();         
            if (email != null) {
                Date expiration = claims.getExpiration();
                Date now = new Date();
                System.out.println("Token expiration: " + expiration);
                System.out.println("Current time: " + now);
                System.out.println("Token validated for email: " + email);
                return expiration.after(now);

            } else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token claims");
            }
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token", e);
        }
    }

    private Claims extractClaims(String token) {
        JwtParserBuilder parserBuilder = Jwts.parser()
                                            .verifyWith(secretKey);
        Claims claims = parserBuilder.build()
                                    .parseSignedClaims(token)
                                    .getPayload();
        return claims;
    }

    public String extractUsername(String token) {
        Claims claims = extractClaims(token);
        return claims.getSubject();
    }
}
