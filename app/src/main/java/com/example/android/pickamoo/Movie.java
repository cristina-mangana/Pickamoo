package com.example.android.pickamoo;

import java.util.List;

/**
 * Created by Cristina on 28/03/2018.
 * This class represents a single movie. Each object has information about the movie, such as title,
 * release date, vote average, synopsis and image poster url.
 */

public class Movie {

    private int mId;
    private String mTitle, mReleaseDate, mCountries, mGenres, mSynopsis, mImageUrl, mDirector;
    private double mVoteAverage = -1.0;
    private List<String> mImagesList;
    private List<String[]> mCast, mTrailers, mReviews;
    private List<Movie> mRecommendations;

    /**
     * No arguments constructor to construct the object with the setter methods
     */
    public Movie() {
    }

    /**
     * Create a new {@link Movie} object with only basic information.
     * @param id is the movie id
     * @param imageUrl is the url to download the movie poster image
     */
    public Movie(int id, String imageUrl) {
        mId = id;
        mImageUrl = imageUrl;
    }

    /**
     * Create a new {@link Movie} object with all details.
     * @param id is the movie id
     * @param title is the movie title
     * @param releaseDate is the date of release
     * @param voteAverage is the average rating of the movie (0.0 to 10.0)
     * @param synopsis is a short description of the movie
     * @param imageUrl is the url to download the movie poster image
     * @param director is the movie director
     * @param countries are the production countries of the movie
     * @param genres are the movie genres (i.e. Action, Comedy, etc.)
     * @param imagesList is a list of urls to download backdrops images
     * @param cast is a list of the main cast of the movie (name and photo)
     * @param trailers is a list of trailers to see the movie (url and thumbnail)
     * @param reviews is a list of reviews of the movie (review, url and author)
     * @param recommendations is a list of recommended movies related to this one
     */
    public Movie(int id, String title, String releaseDate, double voteAverage, String synopsis,
                 String imageUrl, String director, String countries, String genres,
                 List<String> imagesList, List<String[]> cast, List<String[]> trailers,
                 List<String[]> reviews, List<Movie> recommendations) {
        mId = id;
        mTitle = title;
        mReleaseDate = releaseDate;
        mVoteAverage = voteAverage;
        mSynopsis = synopsis;
        mImageUrl = imageUrl;
        mDirector = director;
        mCountries = countries;
        mGenres = genres;
        mImagesList = imagesList;
        mCast = cast;
        mTrailers = trailers;
        mReviews = reviews;
        mRecommendations = recommendations;
    }

    /**
     * Get the movie title
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Get the release date
     */
    public String getReleaseDate() {
        return mReleaseDate;
    }

    /**
     * Get the movie synopsis
     */
    public String getSynopsis() {
        return mSynopsis;
    }

    /**
     * Get the movie poster url
     */
    public String getImageUrl() {
        return mImageUrl;
    }

    /**
     * Get the average rating of the movie
     */
    public double getVoteAverage() {
        return mVoteAverage;
    }

    /**
     * Get the movie id
     */
    public int getId() {
        return mId;
    }

    /**
     * Get the movie director
     */
    public String getDirector() {
        return mDirector;
    }

    /**
     * Get the production countries of the movie
     */
    public String getCountries() {
        return mCountries;
    }

    /**
     * Get the movie genres
     */
    public String getGenres() {
        return mGenres;
    }

    /**
     * Get a list of urls to download backdrops images
     */
    public List<String> getImagesList() {
        return mImagesList;
    }

    /**
     * Get a list of the main cast of the movie (name and photo)
     */
    public List<String[]> getCast() {
        return mCast;
    }

    /**
     * Get a list of trailers to see the movie
     */
    public List<String[]> getTrailers() {
        return mTrailers;
    }

    /**
     * Get a list of reviews of the movie (author and review)
     */
    public List<String[]> getReviews() {
        return mReviews;
    }

    /**
     * Get a list of recommended movies related to this one
     */
    public List<Movie> getRecommendations() {
        return mRecommendations;
    }

    /**
     * Set the movie title
     */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /**
     * Set the release date
     */
    public void setReleaseDate(String releaseDate) {
        this.mReleaseDate = releaseDate;
    }

    /**
     * Set the movie synopsis
     */
    public void setSynopsis(String synopsis) {
        this.mSynopsis = synopsis;
    }

    /**
     * Set the movie poster url
     */
    public void setImageUrl(String imageUrl) {
        this.mImageUrl = imageUrl;
    }

    /**
     * Set the average rating of the movie
     */
    public void setVoteAverage(double voteAverage) {
        this.mVoteAverage = voteAverage;
    }

    /**
     * Set the movie id
     */
    public void setId(int mId) {
        this.mId = mId;
    }

    /**
     * Set the movie director
     */
    public void setDirector(String mDirector) {
        this.mDirector = mDirector;
    }

    /**
     * Set the production countries of the movie
     */
    public void setCountries(String mCountries) {
        this.mCountries = mCountries;
    }

    /**
     * Set the movie genres
     */
    public void setGenres(String mGenres) {
        this.mGenres = mGenres;
    }

    /**
     * Set a list of urls to download backdrops images
     */
    public void setImagesList(List<String> mImagesList) {
        this.mImagesList = mImagesList;
    }

    /**
     * Set a list of the main cast of the movie (name and photo)
     */
    public void setCast(List<String[]> mCast) {
        this.mCast = mCast;
    }

    /**
     * Set a list of trailers to see the movie (name, video url and thumbnail url)
     */
    public void setTrailers(List<String[]> mTrailers) {
        this.mTrailers = mTrailers;
    }

    /**
     * Set a list of reviews of the movie (review, url and author)
     */
    public void setReviews(List<String[]> mReviews) {
        this.mReviews = mReviews;
    }

    /**
     * Set a list of recommended movies related to this one
     */
    public void setRecommendations(List<Movie> mRecommendations) {
        this.mRecommendations = mRecommendations;
    }
}
