package com.example.android.pickamoo.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.example.android.pickamoo.Movie;
import com.example.android.pickamoo.data.MovieContract.MovieEntry;
import com.example.android.pickamoo.utilities.QueryUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cristina on 28/03/2018.
 * This class loads a list of movies by using an AsyncTask to perform the network request to
 * the given URL.
 */

public class MoviesListLoader extends AsyncTaskLoader<List<Movie>> {

    private String mUrl;

    public MoviesListLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Movie> loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        List<Movie> movies = new ArrayList<>();
        if (mUrl.equals(MovieEntry.CONTENT_URI.toString())) {
            // Data is in database
            String[] projection = {
                    MovieEntry._ID,
                    MovieEntry.COLUMN_MOVIE_ID,
                    MovieEntry.COLUMN_MOVIE_POSTER,
            };
            Cursor cursor = getContext().getContentResolver().query(Uri.parse(mUrl), projection,
                    null, null, null, null);
            if (cursor != null) {
                // Transform the cursor into a list of movies
                // From: https://stackoverflow.com/questions/1354006/how-can-i-create-a-list-array-with-the-cursor-data-in-android
                int idColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID);
                int posterColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_POSTER);
                for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    int movieId = cursor.getInt(idColumnIndex);
                    String moviePoster = cursor.getString(posterColumnIndex);
                    movies.add(new Movie(movieId, moviePoster));
                }
                cursor.close();
            }
        } else {
            // Perform the HTTP request and process the response.
            movies = QueryUtils.fetchMoviesListData(mUrl);
        }
        return movies;
    }
}
