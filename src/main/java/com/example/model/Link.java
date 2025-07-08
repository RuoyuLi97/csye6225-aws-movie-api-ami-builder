package com.example.model;

import lombok.Data;

@Data
public class Link {
    private int movieId;
    private String imdbId;
    private String tmdbId;

    public Link(int movieId, String imdbId, String tmdbId) {
        this.movieId = movieId;
        this.imdbId = imdbId;
        this.tmdbId = tmdbId;
    }
}