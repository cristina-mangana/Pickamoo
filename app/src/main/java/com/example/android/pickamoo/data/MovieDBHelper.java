package com.example.android.pickamoo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.pickamoo.data.MovieContract.MovieEntry;


/**
 * Created by Cristina on 11/06/2018.
 * Manages database creation and version management.
 */
public class MovieDBHelper extends SQLiteOpenHelper {

    // Name of the database file
    private static final String DATABASE_NAME = "favorites.db";

    // Database version
    private static final int DATABASE_VERSION = 1;

    // SQL statement to create the products table
    private static final String SQL_CREATE_FAVORITES_TABLE =
            "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                    MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                    MovieEntry.COLUMN_MOVIE_POSTER + " TEXT," +
                    MovieEntry.COLUMN_MOVIE_TITLE + " TEXT," +
                    MovieEntry.COLUMN_MOVIE_DATE + " TEXT," +
                    MovieEntry.COLUMN_MOVIE_COUNTRIES + " TEXT," +
                    MovieEntry.COLUMN_MOVIE_GENRES + " TEXT," +
                    MovieEntry.COLUMN_MOVIE_SYNOPSIS + " TEXT," +
                    MovieEntry.COLUMN_MOVIE_DIRECTOR + " TEXT," +
                    MovieEntry.COLUMN_MOVIE_RATING + " REAL NOT NULL DEFAULT 0.0," +
                    // This table can only contain one entry per movie id
                    " UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

    /**
     * Constructs a new instance of {@link MovieDBHelper}.
     * @param context of the app
     */
    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
        }

    /**
     * This method is called when the database needs to be upgraded. This database is only a cache
     * for online data, so its upgrade policy is simply to discard the data and call through to
     * onCreate to recreate the table.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
