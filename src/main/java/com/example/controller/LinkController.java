package com.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import com.example.model.Link;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

@RestController
@RequestMapping("/v1")
public class LinkController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        System.out.println("Application started and ready!");
    }

    @GetMapping("/link/{movieId}")
    private ResponseEntity<Object> getMovieLink(@PathVariable int movieId){
        try {
            String sql = "SELECT movieId, imdbId, tmdbId\n" + 
                        "FROM links\n" + 
                        "WHERE movieId = ? ;";
            
            List<Link> links = jdbcTemplate.query(sql, (rs, rowNum) -> {
                int linkMovieId = rs.getInt("movieId");
                String imdbId = rs.getString("imdbId");
                String tmdbId = rs.getString("tmdbId");
                return new Link(linkMovieId, imdbId, tmdbId);
            }, movieId);
            
            if (links.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                                    .header(HttpHeaders.PRAGMA, "no-cache")
                                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                    .header("X-Content-Type-Options", "nosniff")
                                    .body("{\"error\":\"No links found for this movie\"}");
            } else {
                Link link = links.get(0);
                String responseBody = "{\"movieId\":" + link.getMovieId() +
                                        ",\"imdbId\":\"" + link.getImdbId() +
                                        "\",\"tmdbId\":\"" + link.getTmdbId() + "\"}";
                return ResponseEntity.status(HttpStatus.OK)
                                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                                    .header(HttpHeaders.PRAGMA, "no-cache")
                                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                    .header("X-Content-Type-Options", "nosniff")
                                    .body(responseBody);
            }
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                            .header(HttpHeaders.PRAGMA, "no-cache")
                            .header(HttpHeaders.CONTENT_TYPE, "application/json")
                            .header("X-Content-Type-Options", "nosniff")
                            .build();
        }
    }
}