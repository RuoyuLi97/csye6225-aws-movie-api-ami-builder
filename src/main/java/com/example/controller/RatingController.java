package com.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import com.example.model.Rating;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

@RestController
@RequestMapping("/v1")
public class RatingController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        System.out.println("Application started and ready!");
    }

    @GetMapping("/rating/{movieId}")
    private ResponseEntity<Object> getMovieRating(@PathVariable int movieId){
        try {
            String sql = "SELECT movieId, AVG(rating) AS avg_rating\n" + 
                        "FROM ratings\n" + 
                        "WHERE movieId = ? \n" + 
                        "GROUP BY movieId;";
            
            List<Rating> ratings = jdbcTemplate.query(sql, (rs, rowNum) -> {
                int ratingMovieId = rs.getInt("movieId");
                double averageRating = rs.getDouble("avg_rating");
                return new Rating(ratingMovieId, averageRating);
            }, movieId);
            
            if (ratings.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                                    .header(HttpHeaders.PRAGMA, "no-cache")
                                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                    .header("X-Content-Type-Options", "nosniff")
                                    .body("{\"error\":\"No ratings found for this movie\"}");
            } else {
                Rating rating = ratings.get(0);
                String responseBody = "{\"movieId\":" + rating.getMovieId() +
                                        ",\"average_rating\":" + rating.getAverageRating() + "}";
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