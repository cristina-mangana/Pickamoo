package com.example.android.pickamoo;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pickamoo.data.MovieContract.MovieEntry;
import com.example.android.pickamoo.databinding.ActivityDetailBinding;
import com.example.android.pickamoo.loaders.MovieDetailsLoader;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Movie> {

    public static final String LOG_TAG = DetailActivity.class.getName();

    /**
     * URL for get details from a movie from the TheMovieDb API
     */
    private static final String MOVIE_REQUEST_URL = "http://api.themoviedb.org/3/movie/";

    /**
     * Query parameter for the API key
     */
    private static final String API_QUERY_PARAMETER = "?api_key=";

    /**
     * API Key
     */
    private static final String API_KEY = BuildConfig.API_KEY;

    /**
     * Path for the append_to_response method filtered by language
     */
    private static final String APPEND_PATH = "&append_to_response=images,credits,videos,reviews,recommendations&language=";

    /**
     * Language value null. The main value is stored in res/strings to allow translations
     */
    private static final String LANGUAGE_VALUE = ",null";

    /* Unique identifier of the Loader */
    private static final int ID_LOADER = 12;

    private static final String SEPARATOR = "-";

    private String requestUrl;

    private ActivityDetailBinding mBinding;

    /* First trailer url */
    private String mSharedLink = "";

    /* Boolean to know if the movie is one of the user's favorites */
    boolean isFavorite = false;

    /* Boolean to know if the data is still loading. It becomes false when movie details are
    displayed on the UI */
    boolean isLoading = true;

    /* Movie details */
    int mMovieId;
    String mMoviePoster, mMovieTitle, mMovieDate, mMovieCountries, mMovieGenres, mMovieSynopsis,
            mMovieDirector;
    Double mMovieRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Restore state from saved instance
        if (savedInstanceState != null) {
            isFavorite = savedInstanceState.getBoolean("isFavoriteSaved");
        }

        if (getIntent() == null && !getIntent().hasExtra("movieId")) {
            showError();
        }

        mMovieId = getIntent().getExtras().getInt("movieId");

        // Check if the movie is a favorite
        isFavorite = MainActivity.favoritesList.contains(mMovieId);

        // Without internet connection, read movie from database
        if (!checkInternetConnection()) {
            requestUrl = MovieEntry.buildUriWithMovieId(mMovieId).toString();
            Toast.makeText(this, getString(R.string.offline), Toast.LENGTH_SHORT).show();
        } else {
            // Create the URL to perform the network request
            requestUrl = MOVIE_REQUEST_URL + String.valueOf(mMovieId)
                    + API_QUERY_PARAMETER + API_KEY
                    + APPEND_PATH + getString(R.string.language_code) + LANGUAGE_VALUE;
        }

        // Get a reference to the LoaderManager
        LoaderManager loaderManager = getSupportLoaderManager();
        // Initialize the loader
        loaderManager.initLoader(ID_LOADER, null, this);

        // Toolbar settings
        Toolbar myToolbar = mBinding.toolbar;
        setSupportActionBar(myToolbar);
        // Add back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set app bar dimensions (aspect ratio 16:9)
        //Get the device screen dimensions
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        ViewGroup.LayoutParams params = mBinding.appBar.getLayoutParams();
        if (screenHeight > screenWidth) {
            params.height = screenWidth * 9 / 16;
        } else {
            params.height = screenHeight / 2;
        }

        // Progress bar color
        if (mBinding.loadingSpinner.getIndeterminateDrawable() != null) {
            mBinding.loadingSpinner.getIndeterminateDrawable()
                    .setColorFilter(ContextCompat.getColor(this, R.color.colorAccent),
                            android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    // Fires when a configuration change occurs and fragment needs to save state
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("isFavoriteSaved", isFavorite);
        super.onSaveInstanceState(savedInstanceState);
    }

    // Inflate toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        if (isFavorite) {
            menu.findItem(R.id.action_favorite).getIcon()
                    .setColorFilter(ContextCompat.getColor(this, R.color.colorAccent),
                            PorterDuff.Mode.SRC_ATOP);
        } else {
            menu.findItem(R.id.action_favorite).getIcon()
                    .setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
        return true;
    }

    // Handle clicks on menu buttons
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share:
                if (mSharedLink != null && !mSharedLink.isEmpty()) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shared_text,
                            mSharedLink, getString(R.string.app_name)));
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
                return true;
            case R.id.action_favorite:
                if (!isFavorite) {
                    if (isLoading) {
                        Toast.makeText(this, getString(R.string.wait),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Create a new map of values, where column names are the keys
                        ContentValues values = new ContentValues();
                        values.put(MovieEntry.COLUMN_MOVIE_ID, mMovieId);
                        values.put(MovieEntry.COLUMN_MOVIE_POSTER, mMoviePoster);
                        values.put(MovieEntry.COLUMN_MOVIE_TITLE, mMovieTitle);
                        values.put(MovieEntry.COLUMN_MOVIE_DATE, mMovieDate);
                        values.put(MovieEntry.COLUMN_MOVIE_COUNTRIES, mMovieCountries);
                        values.put(MovieEntry.COLUMN_MOVIE_GENRES, mMovieGenres);
                        values.put(MovieEntry.COLUMN_MOVIE_SYNOPSIS, mMovieSynopsis);
                        values.put(MovieEntry.COLUMN_MOVIE_DIRECTOR, mMovieDirector);
                        values.put(MovieEntry.COLUMN_MOVIE_RATING, mMovieRating);
                        // Insert a new movie in the database
                        Uri newUri = getContentResolver()
                                .insert(MovieEntry.CONTENT_URI, values);
                        if (newUri == null) {
                            // If the new content URI is null, there was an error with insertion.
                            Toast.makeText(this, getString(R.string.insert_failed),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Otherwise, the insertion was successful.
                            item.getIcon().setColorFilter(ContextCompat.getColor(this,
                                    R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                            isFavorite = true;
                            MainActivity.favoritesList.add(mMovieId);
                        }
                    }
                    return true;
                } else {
                    // Show delete confirmation dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.delete_confirmation);
                    builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Delete from database
                            int rowsDeleted = getContentResolver().delete(
                                    MovieEntry.buildUriWithMovieId(mMovieId),
                                    null,
                                    null
                            );
                            if (rowsDeleted == 0) {
                                // If there are no rows deleted, then there was an error with the
                                // deletion.
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.delete_failed),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Otherwise, the deletion was successful.
                                item.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                                isFavorite = false;
                                MainActivity.favoritesList.remove(MainActivity.favoritesList
                                        .indexOf(mMovieId));
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked the "Cancel" button, so dismiss the dialog
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    // Handle back button on toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Called by the {@link android.support.v4.app.LoaderManagerImpl} when a new Loader needs to be
     * created.
     */
    @NonNull
    @Override
    public Loader<Movie> onCreateLoader(int id, @Nullable Bundle args) {
        // Create a new loader for the correct URL
        Uri baseUri = Uri.parse(requestUrl);
        return new MovieDetailsLoader(this, baseUri.toString());
    }

    /**
     * Called when a Loader has finished loading its data.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<Movie> loader, Movie movie) {
        // Hide the loading indicator
        mBinding.loadingSpinner.setVisibility(View.GONE);
        isLoading = false;
        // Show the main view
        mBinding.mainDetails.setVisibility(View.VISIBLE);

        // If there is a valid {@link Movie}, then display its details.
        if (movie != null) {
            // Set title
            if (movie.getTitle() != null && !movie.getTitle().isEmpty()) {
                mMovieTitle = movie.getTitle();
                mBinding.tvTitle.setText(mMovieTitle);
                // Change toolbar height for multiline text
                ViewGroup.LayoutParams params = mBinding.toolbar.getLayoutParams();
                if (mBinding.tvTitle.getLineCount() != 1) {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
            }

            // Set vote average
            if (movie.getVoteAverage() == -1.0) {
                mBinding.tvRating.setText(SEPARATOR);
            } else {
                mMovieRating = movie.getVoteAverage();
                mBinding.tvRating.setVisibility(View.VISIBLE);
                mBinding.tvRating.setText(String.valueOf(mMovieRating));
            }

            // Set poster image
            // Get the imageLink to download the image
            String imageLink = movie.getImageUrl();
            if (imageLink != null && imageLink.length() > 0) {
                Picasso.get().load(imageLink).error(R.drawable.img_placeholder)
                        .into(mBinding.ivPoster);
                mMoviePoster = imageLink;
            } else {
                Picasso.get().load(R.drawable.img_placeholder).into(mBinding.ivPoster);
            }

            // Set release year
            String date;
            if (movie.getReleaseDate() != null && !movie.getReleaseDate().isEmpty()) {
                date = movie.getReleaseDate();
                mMovieDate = date;
                // Show only year (date is returned in format yyyy-mm-dd)
                if (date.contains(SEPARATOR)) {
                    String[] parts = date.split(SEPARATOR);
                    date = parts[0];
                }
            } else {
                date = SEPARATOR;
            }
            mBinding.tvDate.setText(date);

            // Set country
            String country;
            if (movie.getCountries() != null && !movie.getCountries().isEmpty()) {
                country = movie.getCountries();
                mMovieCountries = country;
            } else {
                country = SEPARATOR;
            }
            mBinding.tvCountry.setText(country);

            // Set genre
            if (movie.getGenres() != null && !movie.getGenres().trim().isEmpty()) {
                mMovieGenres = movie.getGenres();
                mBinding.tvGenre.setText(mMovieGenres);
            } else {
                mBinding.genreLayout.setVisibility(View.GONE);
            }

            // Set director
            if (movie.getDirector() != null && !movie.getDirector().trim().isEmpty()) {
                mMovieDirector = movie.getDirector().trim();
                mBinding.tvDirector.setText(mMovieDirector);
            } else {
                mBinding.directionLayout.setVisibility(View.GONE);
            }

            // Set synopsis
            String synopsisLabel = getString(R.string.synopsis_label);
            String synopsis;
            if (movie.getSynopsis() != null && !movie.getSynopsis().isEmpty()) {
                synopsis = movie.getSynopsis();
                mMovieSynopsis = synopsis;
            } else {
                synopsis = getString(R.string.no_info);
            }
            SpannableStringBuilder spannableSynopsis =
                    new SpannableStringBuilder(synopsisLabel + " " + synopsis);
            spannableSynopsis.setSpan(new TextAppearanceSpan(this, R.style.label_style),
                    0, synopsisLabel.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableSynopsis.setSpan(new TextAppearanceSpan(this, R.style.body_style),
                    synopsisLabel.length() + 1, synopsis.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mBinding.tvSynopsis.setText(spannableSynopsis);

            // Set images
            if (movie.getImagesList() != null) {
                // use a horizontal linear layout manager
                LinearLayoutManager imagesLayoutManager = new LinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL, false);
                mBinding.rvPhotos.setLayoutManager(imagesLayoutManager);
                // specify an adapter and tell that it is a list of images
                DetailsAdapter imagesAdapter = new DetailsAdapter(this, movie.getImagesList(),
                        DetailsAdapter.IMAGES_TYPE);
                mBinding.rvPhotos.setAdapter(imagesAdapter);
            } else {
                mBinding.photosLabel.setVisibility(View.GONE);
                mBinding.rvPhotos.setVisibility(View.GONE);
            }

            // Set cast
            if (movie.getCast() != null) {
                // use a horizontal linear layout manager
                LinearLayoutManager castLayoutManager = new LinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL, false);
                mBinding.rvCast.setLayoutManager(castLayoutManager);
                // specify an adapter and tell that it is a list of actors/actress
                DetailsAdapter castAdapter = new DetailsAdapter(this, movie.getCast(),
                        DetailsAdapter.CAST_TYPE);
                mBinding.rvCast.setAdapter(castAdapter);
            } else {
                mBinding.castLabel.setVisibility(View.GONE);
                mBinding.rvCast.setVisibility(View.GONE);
            }

            // Set trailers
            if (movie.getTrailers() != null) {
                String videoUrlsList[] = movie.getTrailers().get(0);
                // use a horizontal linear layout manager
                LinearLayoutManager videosLayoutManager = new LinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL, false);
                mBinding.rvTrailers.setLayoutManager(videosLayoutManager);
                // specify an adapter and tell that it is a list of trailers
                DetailsAdapter videosAdapter = new DetailsAdapter(this, movie.getTrailers(),
                        DetailsAdapter.TRAILERS_TYPE);
                mBinding.rvTrailers.setAdapter(videosAdapter);

                // Set video url to share
                mSharedLink = "http://www.youtube.com/watch?v=" + videoUrlsList[0];
            } else {
                mBinding.trailersLabel.setVisibility(View.GONE);
                mBinding.rvTrailers.setVisibility(View.GONE);
            }

            // Set reviews
            if (movie.getReviews() != null) {
                String contentsList[] = movie.getReviews().get(0);
                final String urlsList[] = movie.getReviews().get(1);
                String authorsList[] = movie.getReviews().get(2);
                TypedArray viewsArray = getResources().obtainTypedArray(R.array.reviews_ids);
                for (int i = 0; i < contentsList.length; i++) {
                    CardView view = findViewById(viewsArray.getResourceId(i, 0));
                    TextView author = view.findViewById(R.id.author_tv);
                    TextView review = view.findViewById(R.id.review_tv);
                    TextView viewMore = view.findViewById(R.id.view_more);
                    view.setVisibility(View.VISIBLE);
                    SpannableStringBuilder spannableName =
                            new SpannableStringBuilder(getString(R.string.author_label,
                                    authorsList[i]));
                    spannableName.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                            0, authorsList[i].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    author.setText(spannableName);
                    review.setText(getString(R.string.review_text, contentsList[i]));
                    final String url = urlsList[i];
                    viewMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Open the review in the explorer
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        }
                    });
                }
                viewsArray.recycle();
            } else {
                mBinding.reviewsLabel.setVisibility(View.GONE);
                mBinding.reviewsLayout.setVisibility(View.GONE);
            }

            // Set recommendations
            if (movie.getRecommendations() != null) {
                final List<Movie> recommendedMovies = movie.getRecommendations();
                // use a horizontal linear layout manager
                LinearLayoutManager recommendationsLayoutManager =
                        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                                false);
                mBinding.rvRecommendations.setLayoutManager(recommendationsLayoutManager);
                // Specify an adapter and tell that it is a list of recommendations
                DetailsAdapter recommendationsAdapter = new DetailsAdapter(this,
                        recommendedMovies, DetailsAdapter.RECOMMENDATIONS_TYPE);
                mBinding.rvRecommendations.setAdapter(recommendationsAdapter);
            } else {
                mBinding.recommendationsLabel.setVisibility(View.GONE);
                mBinding.rvRecommendations.setVisibility(View.GONE);
            }
        } else {
            showError();
        }
    }

    /**
     * Called when a created loader is being reset.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<Movie> loader) {
        // No need to do anything
    }

    /**
     * Helper method to finish the activity when an error occur.
     */
    private void showError() {
        Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        finish();
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
