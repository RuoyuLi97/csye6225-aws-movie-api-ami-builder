package com.example.model;

import lombok.Data;

@Data
public class Rating {
    private int movieId;
    private double averageRating;

    public Rating(int movieId, double averageRating) {
        this.movieId = movieId;
        this.averageRating = averageRating;
    }
}
