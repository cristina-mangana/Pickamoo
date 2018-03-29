package com.example.android.pickamoo.utilities;

import android.text.TextUtils;
import android.util.Log;

import com.example.android.pickamoo.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.pickamoo.MainActivity.LOG_TAG;

/**
 * Created by Cristina on 28/03/2018.
 * Helper methods related to requesting and receiving movies data from TheMovieDb API.
 */

public final class QueryUtils {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the TheMovieDb dataset and return a List of {@link Movie}.
     */
    public static List<Movie> fetchMoviesData(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request", e);
        }

        // Extract relevant fields from the JSON response and create an List<Movie> object
        List<Movie> movies = extractFeatureFromJson(jsonResponse);

        // Return the List
        return movies;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                    Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Movie} objects that has been built up from parsing a JSON response.
     */
    private static List<Movie> extractFeatureFromJson(String movieJSON) {

        /* Key value for the "results" array. It is an array of Movie objects.*/
        final String RESULTS_KEY = "results";

        /* Key value for the "title" string.*/
        final String TITLE_KEY = "title";

        /* Key value for the "release_date" string.*/
        final String RELEASE_DATE_KEY = "release_date";

        /* Key value for the "vote_average" double.*/
        final String VOTE_AVERAGE_KEY = "vote_average";

        /* Key value for the "overview" string.*/
        final String SYNOPSIS_KEY = "overview";

        /* Key value for the "poster_path" string. This string is the url to download the poster.*/
        final String IMAGE_KEY = "poster_path";

        /* Base image link.*/
        final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w342/";

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(movieJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding movies to
        List<Movie> movies = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(movieJSON);
            if (baseJsonResponse.has(RESULTS_KEY)) {
                JSONArray results = baseJsonResponse.getJSONArray(RESULTS_KEY);

                // Loop through each feature in the array
                for (int i = 0; i < results.length(); i++) {
                    // Create an empty Movie Object so that we can start adding information about it
                    Movie movie = new Movie();

                    // Get movie JSONObject at position i
                    JSONObject movieObject = results.getJSONObject(i);

                    // Extract "title"
                    if (movieObject.has(TITLE_KEY)) {
                        String title = movieObject.getString(TITLE_KEY);
                        movie.setTitle(title);
                    }

                    // Extract "release_date" (release date is returned in format yyyy-mm-dd)
                    if (movieObject.has(RELEASE_DATE_KEY)) {
                        String releaseDate = movieObject.getString(RELEASE_DATE_KEY);
                        movie.setReleaseDate(releaseDate);
                    }

                    // Extract "vote_average"
                    if (movieObject.has(VOTE_AVERAGE_KEY)) {
                        double voteAverage = movieObject.getDouble(VOTE_AVERAGE_KEY);
                        movie.setVoteAverage(voteAverage);
                    }

                    // Extract "synopsis"
                    if (movieObject.has(SYNOPSIS_KEY)) {
                        String synopsis = movieObject.getString(SYNOPSIS_KEY);
                        movie.setSynopsis(synopsis);
                    }

                    // Extract "poster image"
                    StringBuilder imageUrl = new StringBuilder(BASE_IMAGE_URL);
                    if (movieObject.has(IMAGE_KEY)) {
                        imageUrl.append(movieObject.getString(IMAGE_KEY));
                        movie.setImageUrl(imageUrl.toString());
                    }

                    // Add movie Object to list of movies
                    movies.add(movie);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }

        // Return the list of movies
        return movies;
    }
}
