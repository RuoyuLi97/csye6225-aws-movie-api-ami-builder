package com.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import com.example.model.Movie;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

@RestController
@RequestMapping("/v1")
public class MovieController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        System.out.println("Application started and ready!");
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Object> getMovieByPath(@PathVariable int movieId) {
        return getMovieResponse(movieId);
    }

    @GetMapping("/movie")
    public ResponseEntity<Object> getMovieByQuery(@RequestParam int id) {
        return getMovieResponse(id);
    }
    
    private ResponseEntity<Object> getMovieResponse(Integer movieId){
        try {
            String sql = "SELECT m.movieId, m.title, GROUP_CONCAT(g.genre) AS genres\n" + 
                        "FROM movies m\n" + 
                        "JOIN movies_genres mg ON m.movieId = mg.movieId\n" + 
                        "JOIN genres g ON mg.genreId = g.genreId\n" + 
                        "WHERE m.movieId = ? \n" + 
                        "GROUP BY m.movieId, m.title;";
            
            List<Movie> movies = jdbcTemplate.query(sql, (rs, rowNum) -> {
                String genresString = rs.getString("genres");
                List<String> genres = genresString != null ? Arrays.asList(genresString.split(",")) : null;
                return new Movie(
                    rs.getInt("movieId"),
                    rs.getString("title"),
                    genres
                );
            }, movieId);
            
            if (movies.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                                    .header(HttpHeaders.PRAGMA, "no-cache")
                                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                    .header("X-Content-Type-Options", "nosniff")
                                    .body("{\"error\":\"Movie not found\"}");
            } else {
                Movie movie = movies.get(0);
                String quotedGenres = movie.getGenres().stream()
                                            .map(genre -> "\"" + genre + "\"")
                                            .collect(Collectors.joining(","));
                String responseBody = "{\"movieId\":" + movie.getMovieId() +
                                        ",\"title\":\"" + movie.getTitle() + 
                                        "\",\"genres\":[" + quotedGenres + "]}";
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