package com.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/v1")
public class HealthCheckController{
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    @Autowired
    private DataSource dataSource;
    
    @GetMapping("/healthcheck")
    public ResponseEntity<Object> healthCheck(@RequestParam Map<String, String> param){
        
        if (!param.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                                .header(HttpHeaders.PRAGMA, "no-cache")
                                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                .header("X-Content-Type-Options", "nosniff")
                                .build();
        }
        
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                logger.info("Database is connected.");
                return ResponseEntity.status(HttpStatus.OK)
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                        .header(HttpHeaders.PRAGMA, "no-cache")
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .header("X-Content-Type-Options", "nosniff")
                        .build();
            } else {
                logger.error("Database connection is not valid.");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                        .header(HttpHeaders.PRAGMA, "no-cache")
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .header("X-Content-Type-Options", "nosniff")
                        .build();
            }
        } catch (SQLException e) {
            logger.error("Database connection failed", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header("X-Content-Type-Options", "nosniff")
                    .build();
        }
    }

    @RequestMapping(value = "/healthcheck", method = {RequestMethod.HEAD, 
                                                        RequestMethod.POST, 
                                                        RequestMethod.PUT, 
                                                        RequestMethod.PATCH, 
                                                        RequestMethod.DELETE,
                                                        RequestMethod.TRACE,
                                                        RequestMethod.OPTIONS})

    public ResponseEntity<Object> invalidMethod(@RequestParam(required = false) String param){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                            .header(HttpHeaders.PRAGMA, "no-cache")
                            .header(HttpHeaders.CONTENT_TYPE, "application/json")
                            .header("X-Content-Type-Options", "nosniff")
                            .build();
    }
}
