package com.example.android.pickamoo;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.example.android.pickamoo.utilities.QueryUtils;

import java.util.List;

/**
 * Created by Cristina on 28/09/2018.
 * This class loads a list of movies by using an AsyncTask to perform the network request to
 * the given URL.
 */

public class MovieLoader extends AsyncTaskLoader<List<Movie>> {

    private String mUrl;

    public MovieLoader(Context context, String url) {
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

        // Perform the HTTP request and process the response.
        List<Movie> movies = QueryUtils.fetchMoviesData(mUrl);
        return movies;
    }
}
