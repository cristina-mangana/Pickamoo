package com.example.android.pickamoo.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.pickamoo.data.MovieContract.MovieEntry;


/**
 * Created by Cristina on 11/06/2018.
 * {@link ContentProvider} for Pickamoo app.
 */
public class MovieProvider extends ContentProvider {

    public static final String LOG_TAG = MovieProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the whole table */
    public static final int MOVIES = 100;

    /** URI matcher code for the content URI for a single movie in the table */
    public static final int MOVIE_ID = 101;

    /** URI matcher object to match a content URI to a corresponding code */
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    /** Database helper that will provide access to the database */
    private MovieDBHelper mDbHelper;

    /**
     * Creates the UriMatcher.
     * @return A UriMatcher that correctly matches the constants for MOVIES and MOVIE_ID
     */
    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        /* This URI is "content://com.example.android.pickamoo/favorites". It is used to provide
        access to MULTIPLE rows of the database table. */
        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_FAVORITES, MOVIES);

        /*
         * This URI is "content://com.example.android.pickamoo/favorites/#". It is used to provide
         * access to ONE row of the database table. The # symbol indicates the movie id.
         */
        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_FAVORITES + "/#",
                MOVIE_ID);

        return matcher;
    }

    /**
     * Initialize the provider.
     */
    @Override
    public boolean onCreate() {
        // Create and initialize a MovieDbHelper object to gain access to the database.
        mDbHelper = new MovieDBHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                // Query the whole table
                cursor = database.query(MovieEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case MOVIE_ID:
                //This indicate that it is necessary to return the information for a particular
                // movie given its id
                // Extract out the movie id from the URI
                selection = MovieEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[] {uri.getLastPathSegment()};

                // Perform a query to return a cursor containing a single row
                cursor = database.query(MovieEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                // Get the data repository in write mode
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                // Insert the new row, returning the ID of the new row
                long id = db.insert(MovieEntry.TABLE_NAME, null, values);

                // If the ID is -1, then the insertion failed. Log an error and return null.
                if (id == -1) {
                    Log.e(LOG_TAG, "Failed to insert row for " + uri);
                    return null;
                }

                // Notify all listeners that the data has changed for the content URI
                getContext().getContentResolver().notifyChange(uri, null);

                // Return the URI with the ID of the inserted row appended to its end
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new UnsupportedOperationException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;

        switch (sUriMatcher.match(uri)) {
            case MOVIE_ID:
                // Delete a single row given by the movie ID in the URI
                selection = MovieEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[] {uri.getLastPathSegment()};
                rowsDeleted = database.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        throw new RuntimeException("Update is not supported in Pickamoo.");
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return MovieEntry.CONTENT_LIST_TYPE;
            case MOVIE_ID:
                return MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI " + uri + " with match " + match);
        }
    }
}
