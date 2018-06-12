package com.example.android.pickamoo.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.example.android.pickamoo.Movie;
import com.example.android.pickamoo.data.MovieContract.MovieEntry;
import com.example.android.pickamoo.utilities.QueryUtils;

/**
 * Created by Cristina on 02/06/2018.
 * This class loads the details of a movie by using an AsyncTask to perform the network request to
 * the given URL.
 */
public class MovieDetailsLoader extends AsyncTaskLoader<Movie> {

    private String mUrl;

    /* Member variable that will store the Movie data */
    private Movie mMovie;

    public MovieDetailsLoader(@NonNull Context context, String url) {
        super(context);
        mUrl = url;
    }

    /*
     * If there already are cached results, just deliver them. Else, force a load.
     */
    @Override
    protected void onStartLoading() {
        if (mMovie != null) {
            deliverResult(mMovie);
        } else {
            forceLoad();
        }
    }

    @Nullable
    @Override
    public Movie loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        Movie movie = new Movie();
        if (mUrl.contains(MovieEntry.CONTENT_URI.toString())) {
            // Data is in database
            String[] projection = {
                    MovieEntry._ID,
                    MovieEntry.COLUMN_MOVIE_ID,
                    MovieEntry.COLUMN_MOVIE_POSTER,
                    MovieEntry.COLUMN_MOVIE_TITLE,
                    MovieEntry.COLUMN_MOVIE_DATE,
                    MovieEntry.COLUMN_MOVIE_COUNTRIES,
                    MovieEntry.COLUMN_MOVIE_GENRES,
                    MovieEntry.COLUMN_MOVIE_SYNOPSIS,
                    MovieEntry.COLUMN_MOVIE_DIRECTOR,
                    MovieEntry.COLUMN_MOVIE_RATING
            };
            Cursor cursor = getContext().getContentResolver().query(Uri.parse(mUrl), projection,
                    null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID);
                movie.setId(cursor.getInt(idColumnIndex));
                int posterColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_POSTER);
                movie.setImageUrl(cursor.getString(posterColumnIndex));
                int titleColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_TITLE);
                movie.setTitle(cursor.getString(titleColumnIndex));
                int dateColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_DATE);
                movie.setReleaseDate(cursor.getString(dateColumnIndex));
                int countriesColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_COUNTRIES);
                movie.setCountries(cursor.getString(countriesColumnIndex));
                int genresColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_GENRES);
                movie.setGenres(cursor.getString(genresColumnIndex));
                int synopsisColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_SYNOPSIS);
                movie.setSynopsis(cursor.getString(synopsisColumnIndex));
                int directorColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_DIRECTOR);
                movie.setDirector(cursor.getString(directorColumnIndex));
                int ratingColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_RATING);
                movie.setVoteAverage(cursor.getDouble(ratingColumnIndex));
                cursor.close();
            }
        } else {
            // Perform the HTTP request and process the response.
            movie = QueryUtils.fetchMovieData(mUrl);
        }
        return movie;
    }

    /*
     * If the user navigates away from the activity and then returns, avoid extra load by caching
     * existent data.
     */
    @Override
    public void deliverResult(@Nullable Movie data) {
        mMovie = data;
        super.deliverResult(data);
    }
}
