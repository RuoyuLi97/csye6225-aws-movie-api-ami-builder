package com.example.model;

import java.util.List;

import lombok.Data;

@Data
public class Movie {
    private int movieId;
    private String title;
    private List<String> genres;

    public Movie(int movieId, String title, List<String> genres) {
        this.movieId = movieId;
        this.title = title;
        this.genres = genres;
    }
}
