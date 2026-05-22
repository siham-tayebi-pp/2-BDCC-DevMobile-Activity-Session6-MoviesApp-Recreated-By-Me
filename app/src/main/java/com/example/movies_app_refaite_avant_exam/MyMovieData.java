package com.example.movies_app_refaite_avant_exam;

public class MyMovieData {
    private String movieTitle;
    private String moviData;
    private Integer movieImage;

    public MyMovieData(String movieTitle, String moviData, Integer movieImage) {
        this.movieTitle = movieTitle;
        this.moviData = moviData;
        this.movieImage = movieImage;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMoviData() {
        return moviData;
    }

    public void setMoviData(String moviData) {
        this.moviData = moviData;
    }

    public Integer getMovieImage() {
        return movieImage;
    }

    public void setMovieImage(Integer movieImage) {
        this.movieImage = movieImage;
    }
}
