package com.example.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.security.JwtService;

@RestController
@RequestMapping("/v1")
public class AuthController {
    private final JwtService jwtService;
    private final Map<String, String> users = new HashMap<>();

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                            .header(HttpHeaders.PRAGMA, "no-cache")
                            .header(HttpHeaders.CONTENT_TYPE, "application/json")
                            .header("X-Content-Type-Options", "nosniff")
                            .body("Missing email or password");
        }

        if (!isValidEmail(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                            .header(HttpHeaders.PRAGMA, "no-cache")
                            .header(HttpHeaders.CONTENT_TYPE, "application/json")
                            .header("X-Content-Type-Options", "nosniff")
                            .body("Missing email or password");
            
        }

        users.put(email, password);
        System.out.println("User registered successfully: " + email);
        return ResponseEntity.status(HttpStatus.CREATED)
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                            .header(HttpHeaders.PRAGMA, "no-cache")
                            .header(HttpHeaders.CONTENT_TYPE, "application/json")
                            .header("X-Content-Type-Options", "nosniff")
                            .build();
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        System.out.println("Login attempt for email: " + email);

        if (email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                            .header(HttpHeaders.PRAGMA, "no-cache")
                            .header(HttpHeaders.CONTENT_TYPE, "application/json")
                            .header("X-Content-Type-Options", "nosniff")
                            .body("Missing email or password");
        }

        String storedPassword = users.get(email);
        if (storedPassword == null || !storedPassword.equals(password)) {
            System.out.println("Invalid login attempt for email: " + email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                            .header(HttpHeaders.PRAGMA, "no-cache")
                            .header(HttpHeaders.CONTENT_TYPE, "application/json")
                            .header("X-Content-Type-Options", "nosniff")
                            .body("Invalid email or password");
        }
        
        String token = jwtService.generateToken(email);
        System.out.println("Login successful for email: " + email + " with token: " + token);

        return ResponseEntity.status(HttpStatus.OK)
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                            .header(HttpHeaders.PRAGMA, "no-cache")
                            .header(HttpHeaders.CONTENT_TYPE, "application/json")
                            .header("X-Content-Type-Options", "nosniff")
                            .body("{\"token\": \"" + token + "\"}");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}