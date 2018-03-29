package com.example.android.pickamoo;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RadioGroup;

import com.example.android.pickamoo.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Movie>>{

    public static final String LOG_TAG = MainActivity.class.getName();

    /** API Key */
    private static final String API_KEY = BuildConfig.API_KEY;

    /** URL for popular movies data from the TheMovieDb API */
    private static final String POPULAR_REQUEST_URL =
            "http://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY;

    /** URL for top rated movies data from the TheMovieDb API */
    private static final String TOP_RATED_REQUEST_URL =
            "http://api.themoviedb.org/3/movie/top_rated?api_key=" + API_KEY;

    /** Adapter for the grid of movies */
    private MovieAdapter mAdapter;

    /** Boolean to check sort order */
    boolean isPopular = true;

    private ActivityMainBinding mBinding;

    private LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // RecyclerView settings
        // Find a reference to the {@link RecyclerView} in the layout
        final RecyclerView mRecyclerView = mBinding.moviesGrid;
        // Use a grid linear layout manager
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Create a new adapter that takes an empty list of movies as input
        final List<Movie> mMoviesList = new ArrayList<>();
        mAdapter = new MovieAdapter(this, mMoviesList, new MovieAdapter.MovieAdapterListener() {
            @Override
            public void OnClick(View v, int position) {
                Intent openActivityDetail = new Intent(getApplicationContext(),
                        DetailActivity.class);
                openActivityDetail.putExtra("movie", mMoviesList.get(position));
                startActivity(openActivityDetail);
            }
        });
        // Set the adapter on the {@link RecyclerView} so the list can be populated in the UI
        mRecyclerView.setAdapter(mAdapter);

        // Check for network connectivity
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            // Get a reference to the LoaderManager
            loaderManager = getLoaderManager();

            // Initialize the loader
            loaderManager.initLoader(0, null, this);
        } else {
            // Hide the loading indicator
            mBinding.loadingSpinner.setVisibility(View.GONE);
            // Show the empty view
            mBinding.emptyText.setVisibility(View.VISIBLE);
        }

        // Update activity on sort order change
        mBinding.rgSortOrder.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                isPopular = checkedId == R.id.rb_popular;
                mRecyclerView.setVisibility(View.GONE);
                mBinding.emptyText.setVisibility(View.GONE);
                mBinding.loadingSpinner.setVisibility(View.VISIBLE);
                loaderManager.restartLoader(0, null, MainActivity.this);
            }
        });
    }


    /**
     * Called by the {@link android.support.v4.app.LoaderManagerImpl} when a new Loader needs to be
     * created.
     */
    @Override
    public Loader<List<Movie>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the correct URL
        Uri baseUri;
        if (isPopular) {
            baseUri = Uri.parse(POPULAR_REQUEST_URL);
        } else {
            baseUri = Uri.parse(TOP_RATED_REQUEST_URL);
        }
        return new MovieLoader(this, baseUri.toString());
    }

    /**
     * Called when a Loader has finished loading its data.
     */
    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        // Hide the loading indicator
        mBinding.loadingSpinner.setVisibility(View.GONE);
        // Hide empty state text
        mBinding.emptyText.setVisibility(View.GONE);
        // Clear the adapter of previous data
        mAdapter.clear();
        // Show recyclerView
        mBinding.moviesGrid.setVisibility(View.VISIBLE);

        // If there is a valid list of {@link Movie}s, then add them to the adapter's
        // data set. This will trigger the RecyclerView to update.
        if (movies != null && !movies.isEmpty()) {
            mAdapter.addAll(movies);
        } else {
            // Set empty state text
            mBinding.emptyText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Called when a created loader is being reset.
     */
    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }
}
