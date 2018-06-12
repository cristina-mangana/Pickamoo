package com.example.android.pickamoo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.android.pickamoo.data.MovieContract;
import com.example.android.pickamoo.databinding.ActivityMainBinding;
import com.example.android.pickamoo.loaders.MoviesListLoader;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<Movie>>, SwipeRefreshLayout.OnRefreshListener,
        NavigationView.OnNavigationItemSelectedListener {

    public static final String LOG_TAG = MainActivity.class.getName();

    /**
     * API Key
     */
    private static final String API_KEY = BuildConfig.API_KEY;

    /**
     * URL to fetch data from the TheMovieDb API
     */
    private String requestUrl = POPULAR_REQUEST_URL;

    /**
     * URL for popular movies data from the TheMovieDb API
     */
    private static final String POPULAR_REQUEST_URL =
            "http://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY;

    /**
     * URL for top rated movies data from the TheMovieDb API
     */
    private static final String TOP_RATED_REQUEST_URL =
            "http://api.themoviedb.org/3/movie/top_rated?api_key=" + API_KEY;

    /**
     * URL for movies data ordered by genre from the TheMovieDb API
     */
    private static final String BASE_GENRE_REQUEST_URL =
            "https://api.themoviedb.org/3/discover/movie?api_key=" + API_KEY + "&with_genres=";

    /**
     * Genres values
     */
    private static final String ACTION_VALUE = "28";
    private static final String ANIMATION_VALUE = "16";
    private static final String COMEDY_VALUE = "35";
    private static final String DRAMA_VALUE = "18";
    private static final String ROMANCE_VALUE = "10749";
    private static final String FAMILY_VALUE = "10751";
    private static final String MUSIC_VALUE = "10402";
    private static final String SCIENCE_FICTION_VALUE = "878";
    private static final String THRILLER_VALUE = "53";
    private static final String DOCUMENTARY_VALUE = "99";
    private static final String HORROR_VALUE = "27";
    private static final String WESTERN_VALUE = "37";

    /* Adapter for the grid of movies */
    private MovieAdapter mAdapter;

    /* Unique identifier of the main Loader */
    private static final int ID_LOADER = 23;

    /* Unique identifier of the Loader to get the favorite movies */
    private static final int ID_FAV_LOADER = 17;

    /* Boolean to know whether or not the layout is refreshing */
    private boolean isRefreshing = false;

    /* Drawer toggle */
    private ActionBarDrawerToggle mDrawerToggle;

    /* Navigation drawer position*/
    private int position = 0;

    private static final int FAVORITES_POSITION = 2;

    private ActivityMainBinding mBinding;

    /* List to store the ids of the favorite movies */
    public static List<Integer> favoritesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Without internet connection, start activity from favorites
        if (!checkInternetConnection()) {
            requestUrl = MovieContract.MovieEntry.CONTENT_URI.toString();
            position = FAVORITES_POSITION;
            changeUI(position);
            mBinding.leftDrawer.setCheckedItem(R.id.favorite);
        }

        // Restore state from saved instance
        if (savedInstanceState != null) {
            // Apply current Url
            requestUrl = savedInstanceState.getString("currentUrl");
            // Apply correct heading
            mBinding.headTv.setText(savedInstanceState.getString("headText"));
            // Apply correct empty text
            position = savedInstanceState.getInt("currentPosition");
        } else {
            // Init loader to get the list of favorite movies ids only the first time the activity is created
            getSupportLoaderManager().initLoader(ID_FAV_LOADER, null, this);
        }

        //Toolbar settings
        setSupportActionBar(mBinding.toolbar);
        // No title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Open and close navigation drawer with toolbar icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                   /* host Activity */
                mBinding.drawerLayout,          /* DrawerLayout object */
                mBinding.toolbar,               /* Toolbar */
                R.string.drawer_open,           /* "open drawer" description */
                R.string.drawer_close           /* "close drawer" description */
        ) {

            /* Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /* Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        // Set the drawer toggle as the DrawerListener
        mBinding.drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        // Open/Close drawer on hamburger icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set the drawer list's click listener
        mBinding.leftDrawer.setNavigationItemSelectedListener(this);

        // Progress bar color
        // https://stackoverflow.com/questions/26962136/indeterminate-circle-progress-bar-on-android-is-white-despite-coloraccent-color
        if (mBinding.loadingSpinner.getIndeterminateDrawable() != null) {
            mBinding.loadingSpinner.getIndeterminateDrawable()
                    .setColorFilter(ContextCompat.getColor(this, R.color.colorAccent),
                            android.graphics.PorterDuff.Mode.SRC_IN);
        }

        // RecyclerView settings
        // Find a reference to the {@link RecyclerView} in the layout
        final RecyclerView mRecyclerView = mBinding.moviesGrid;
        // Use a grid layout manager
        final GridLayoutManager mLayoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.column_number));
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Create a new adapter that takes an empty list of movies as input
        final List<Movie> mMoviesList = new ArrayList<>();
        mAdapter = new MovieAdapter(this, mMoviesList, new MovieAdapter.MovieAdapterListener() {
            @Override
            public void OnClick(View v, int position) {
                Intent openActivityDetail = new Intent(getApplicationContext(),
                        DetailActivity.class);
                openActivityDetail.putExtra("movieId", mMoviesList.get(position).getId());
                startActivity(openActivityDetail);
            }
        });
        // Set the adapter on the {@link RecyclerView} so the list can be populated in the UI
        mRecyclerView.setAdapter(mAdapter);

        // Refresh Layout on pulling down
        mBinding.refresh.setOnRefreshListener(this);

        // Initialize the loader
        getSupportLoaderManager().initLoader(ID_LOADER, null, this);
    }

    // Fires when a configuration change occurs and activity needs to save state
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // Save custom values into the bundle
        savedInstanceState.putString("currentUrl", requestUrl);
        savedInstanceState.putString("headText", mBinding.headTv.getText().toString());
        savedInstanceState.putInt("currentPosition", position);
        super.onSaveInstanceState(savedInstanceState);
    }

    // Sync the toggle state after onRestoreInstanceState has occurred.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    // Sync the toggle onConfigurationChange
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    // Handle navigation view item clicks
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Hide the current view (empty state text or recyclerView)
        mBinding.emptyText.setVisibility(View.GONE);
        mBinding.moviesGrid.setVisibility(View.GONE);
        // Show the loading indicator
        mBinding.loadingSpinner.setVisibility(View.VISIBLE);
        // Choose proper url and head text
        switch (item.getItemId()) {
            case R.id.popular:
                // Popular movies
                requestUrl = POPULAR_REQUEST_URL;
                position = 0;
                break;
            case R.id.top_rated:
                // Top-rated movies
                requestUrl = TOP_RATED_REQUEST_URL;
                position = 1;
                break;
            case R.id.favorite:
                requestUrl = MovieContract.MovieEntry.CONTENT_URI.toString();
                position = FAVORITES_POSITION;
                break;
            case R.id.action:
                // Action movies
                requestUrl = BASE_GENRE_REQUEST_URL + ACTION_VALUE;
                position = 3;
                break;
            case R.id.animation:
                // Animation movies
                requestUrl = BASE_GENRE_REQUEST_URL + ANIMATION_VALUE;
                position = 4;
                break;
            case R.id.comedy:
                // Comedy movies
                requestUrl = BASE_GENRE_REQUEST_URL + COMEDY_VALUE;
                position = 5;
                break;
            case R.id.drama:
                // Drama movies
                requestUrl = BASE_GENRE_REQUEST_URL + DRAMA_VALUE;
                position = 6;
                break;
            case R.id.romance:
                // Romance movies
                requestUrl = BASE_GENRE_REQUEST_URL + ROMANCE_VALUE;
                position = 7;
                break;
            case R.id.family:
                // Family movies
                requestUrl = BASE_GENRE_REQUEST_URL + FAMILY_VALUE;
                position = 8;
                break;
            case R.id.music:
                // Music movies
                requestUrl = BASE_GENRE_REQUEST_URL + MUSIC_VALUE;
                position = 9;
                break;
            case R.id.sci_fi:
                // Sci-Fi movies
                requestUrl = BASE_GENRE_REQUEST_URL + SCIENCE_FICTION_VALUE;
                position = 10;
                break;
            case R.id.thriller:
                // Thriller movies
                requestUrl = BASE_GENRE_REQUEST_URL + THRILLER_VALUE;
                position = 11;
                break;
            case R.id.documentary:
                // Documentary movies
                requestUrl = BASE_GENRE_REQUEST_URL + DOCUMENTARY_VALUE;
                position = 12;
                break;
            case R.id.horror:
                // Horror movies
                requestUrl = BASE_GENRE_REQUEST_URL + HORROR_VALUE;
                position = 13;
                break;
            case R.id.western:
                // Western movies
                requestUrl = BASE_GENRE_REQUEST_URL + WESTERN_VALUE;
                position = 14;
                break;
        }
        // Restart Loader to set new data
        restartLoader();
        // Highlight the selected item
        item.setChecked(true);
        // Change head text
        changeUI(position);
        // Close the drawer
        mBinding.drawerLayout.closeDrawer(mBinding.leftDrawer);
        return true;
    }

    /**
     * Called by the {@link android.support.v4.app.LoaderManagerImpl} when a new Loader needs to be
     * created.
     */
    @NonNull
    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle bundle) {
        // Create a new loader for the correct URL
        Uri baseUri;
        switch (id) {
            case ID_FAV_LOADER:
                baseUri = MovieContract.MovieEntry.CONTENT_URI;
                Log.e("loader", "is created");
                break;
            case ID_LOADER:
            default:
                baseUri = Uri.parse(requestUrl);
                break;
        }
        return new MoviesListLoader(this, baseUri.toString());
    }

    /**
     * Called when a Loader has finished loading its data.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<List<Movie>> loader, List<Movie> movies) {
        int id = loader.getId();
        switch (id) {
            case ID_FAV_LOADER:
                // Add favorite movies ids to the list
                if (movies != null && !movies.isEmpty()) {
                    for (int i = 0; i < movies.size(); i++) {
                        favoritesList.add(movies.get(i).getId());
                    }
                }
                // Finish using this loader
                getLoaderManager().destroyLoader(id);
                break;
            case ID_LOADER:
            default:
                // Set empty/error text
                if (position == FAVORITES_POSITION) {
                    mBinding.emptyText.setText(R.string.empty);
                } else {
                    mBinding.emptyText.setText(R.string.error);
                }
                // Hide the refresh icon
                if (isRefreshing) {
                    mBinding.refresh.setRefreshing(false);
                    isRefreshing = false;
                }
                // Hide the loading indicator
                mBinding.loadingSpinner.setVisibility(View.GONE);
                // Hide empty state text
                mBinding.emptyText.setVisibility(View.GONE);
                // Clear the adapter of previous data
                mAdapter.clear();
                // Show the recyclerView
                mBinding.moviesGrid.setVisibility(View.VISIBLE);

                // If there is a valid list of {@link Movie}s, then add them to the adapter's
                // data set. This will trigger the RecyclerView to update.
                if (movies != null && !movies.isEmpty()) {
                    mAdapter.addAll(movies);
                } else {
                    // Show empty state text
                    mBinding.emptyText.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    /**
     * Called when a created loader is being reset.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<List<Movie>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    // Listen to refreshes made by the user
    @Override
    public void onRefresh() {
        mBinding.refresh.setRefreshing(true);
        isRefreshing = true;
        restartLoader();
    }

    // Restart the Loader to set new data
    public void restartLoader() {
            getSupportLoaderManager().restartLoader(ID_LOADER, null, this);
    }

    /**
     * Helper method to change UI based on the category selected in the drawer
     */
    private void changeUI(int position) {
        mBinding.headTv.setText(getResources().getStringArray(R.array.category_titles)[position]);
    }

    /**
     * Helper method to check for Internet connection
     */
    public boolean checkInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
