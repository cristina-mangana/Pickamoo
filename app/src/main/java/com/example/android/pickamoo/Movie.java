package com.example.android.pickamoo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Cristina on 28/03/2018.
 * This class represents a single movie. Each object has information about the movie, such as title,
 * release date, vote average, synopsis and image poster url.
 */

public class Movie implements Parcelable {

    private String mTitle, mReleaseDate, mSynopsis, mImageUrl;
    private double mVoteAverage = -1.0;

    /**
     * No arguments constructor for use in serialization
     */
    public Movie() {
    }

    /**
     * Create a new {@link Movie} object
     * @param title is the movie title
     * @param releaseDate is the date of release
     * @param voteAverage is the average rating of the movie (0.0 to 10.0)
     * @param synopsis is a short description of the movie
     * @param imageUrl is the url to download the movie poster image
     */
    public Movie(String title, String releaseDate, double voteAverage, String synopsis,
                 String imageUrl) {
        mTitle = title;
        mReleaseDate = releaseDate;
        mVoteAverage = voteAverage;
        mSynopsis = synopsis;
        mImageUrl = imageUrl;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTitle);
        parcel.writeString(mReleaseDate);
        parcel.writeString(mSynopsis);
        parcel.writeString(mImageUrl);
        parcel.writeDouble(mVoteAverage);
    }

    // Creator
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    // De-parcel object
    private Movie(Parcel parcel) {
        mTitle = parcel.readString();
        mReleaseDate = parcel.readString();
        mSynopsis = parcel.readString();
        mImageUrl = parcel.readString();
        mVoteAverage = parcel.readDouble();
    }
}
