package com.example.android.pickamoo.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Cristina on 11/06/2018.
 * Contract class. Defines table and column names for the favorite movies database.
 */

public class MovieContract {

    /**
     * Empty constructor to prevent someone from accidentally instantiating the contract class.
     */
    private MovieContract() {}

    /**
     * Content authority string for the content provider.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.pickamoo";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's).
     */
    public static final String PATH_FAVORITES = "favorites";

    /**
     * Inner class that defines constant values for the favorite movies database table.
     * Each entry in the table represents a single movie.
     */
    public static final class MovieEntry implements BaseColumns {

        /**
         * The content URI to access the table data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FAVORITES);

        /**
         * The content URI to access one row of the table in the provider
         */
        public static final Uri ITEM_URI = Uri.withAppendedPath(CONTENT_URI, "#");

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of movies.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + PATH_FAVORITES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single movie.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + PATH_FAVORITES;

        /**
         * Name of the database table
         */
        public final static String TABLE_NAME = "favorites";

        /**
         * Unique ID number for the product (only for use in the database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Movie ID from theMovieDb API.
         * Type: INTEGER
         */
        public final static String COLUMN_MOVIE_ID = "movie_id";

        /**
         * Url of the movie poster.
         * Type: TEXT
         */
        public final static String COLUMN_MOVIE_POSTER = "poster_url";

        /**
         * Movie title.
         * Type: TEXT
         */
        public final static String COLUMN_MOVIE_TITLE = "title";

        /**
         * Movie release date in format yyyy-mm-dd.
         * Type: TEXT
         */
        public final static String COLUMN_MOVIE_DATE = "release_date";

        /**
         * Movie list of production countries, separated by a comma.
         * Type: TEXT
         */
        public final static String COLUMN_MOVIE_COUNTRIES = "production_countries";

        /**
         * Movie list of genres, separated by a comma.
         * Type: TEXT
         */
        public final static String COLUMN_MOVIE_GENRES = "genres";

        /**
         * Movie synopsis.
         * Type: TEXT
         */
        public final static String COLUMN_MOVIE_SYNOPSIS = "synopsis";

        /**
         * Movie director.
         * Type: TEXT
         */
        public final static String COLUMN_MOVIE_DIRECTOR = "director";

        /**
         * Movie vote average as a one decimal double variable.
         * Type: REAL
         */
        public final static String COLUMN_MOVIE_RATING = "vote_average";

        /**
         * Builds a URI that adds the movie id to the end of the content URI path. This is used to
         * query details about a single movie or delete it from the database.
         *
         * @param movieId is the movie id
         */
        public static Uri buildUriWithMovieId(int movieId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(movieId))
                    .build();
        }

    }
}
