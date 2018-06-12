package com.example.android.pickamoo.utilities;

import android.net.Uri;
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
    public static List<Movie> fetchMoviesListData(String requestUrl) {

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
        List<Movie> movies = extractListDataFromJson(jsonResponse);

        // Return the List
        return movies;
    }

    /**
     * Query the TheMovieDb dataset and return the details of a {@link Movie}.
     */
    public static Movie fetchMovieData(String requestUrl) {

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
        Movie movie = extractMovieDetailsFromJson(jsonResponse);

        // Return the List
        return movie;
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
     * Convert the {@link InputStream} into a String which contains the whole JSON response from
     * the server.
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
    private static List<Movie> extractListDataFromJson(String movieJSON) {

        /* Key value for the "results" array.*/
        final String RESULTS_KEY = "results";

        /* Key value for the "id" integer.*/
        final String ID_KEY = "id";

        /* Key value for the "poster_path" string. This string is the url to download the poster.*/
        final String POSTER_KEY = "poster_path";

        /* Base image link.*/
        final String BASE_IMAGE_URL_342 = "http://image.tmdb.org/t/p/w342/";

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

                    // Extract "id"
                    if (movieObject.has(ID_KEY)) {
                        int id = movieObject.getInt(ID_KEY);
                        movie.setId(id);
                    }

                    // Extract "poster image"
                    if (movieObject.has(POSTER_KEY)) {
                        String imageUrl = BASE_IMAGE_URL_342 + movieObject.getString(POSTER_KEY);
                        movie.setImageUrl(imageUrl);
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

    /**
     * Return a {@link Movie} object with detailed information that has been built up from parsing
     * a JSON response.
     */
    private static Movie extractMovieDetailsFromJson(String movieJSON) {

        /* Key value for the "results" array.*/
        final String RESULTS_KEY = "results";

        /* Key value for the "id" integer.*/
        final String ID_KEY = "id";

        /* Key value for the "poster_path" string. This string is the url to download the poster.*/
        final String POSTER_KEY = "poster_path";

        /* Key value for the "title" string.*/
        final String TITLE_KEY = "title";

        /* Key value for the "release_date" string.*/
        final String RELEASE_DATE_KEY = "release_date";

        /* Key value for the "vote_average" double.*/
        final String VOTE_AVERAGE_KEY = "vote_average";

        /* Key value for the "overview" string.*/
        final String SYNOPSIS_KEY = "overview";

        /* Key value for the "genres" array.*/
        final String GENRE_KEY = "genres";

        /* Key value for the "production_countries" array.*/
        final String COUNTRY_KEY = "production_countries";

        /* Key value for the country iso code string.*/
        final String COUNTRY_CODE = "iso_3166_1";

        /* Key value for the "images" object.*/
        final String IMAGES_KEY = "images";

        /* Key value for the "backdrops" array.*/
        final String BACKDROPS_KEY = "backdrops";

        /* Key value for the image "file_path" string. It is the url to download the image.*/
        final String IMAGE_PATH_KEY = "file_path";

        /* Key value for the "credits" object.*/
        final String CREDITS_KEY = "credits";

        /* Key value for the "cast" array.*/
        final String CAST_KEY = "cast";

        /* Key value for the cast image "profile_path" string. It is the url to download the cast
        person photo.*/
        final String PROFILE_IMAGE_KEY = "profile_path";

        /* Key value for the "crew" array.*/
        final String CREW_KEY = "crew";

        /* Key value for the "job" string.*/
        final String JOB_KEY = "job";

        /* Key value for the "videos" array.*/
        final String VIDEOS_KEY = "videos";

        /* Key value for the video "key" string.*/
        final String VIDEO_KEY_KEY = "key";

        /* Key value for the "reviews" object.*/
        final String REVIEWS_KEY = "reviews";

        /* Key value for the "author" string. It is the author of the review*/
        final String REVIEW_AUTHOR_KEY = "author";

        /* Key value for the review "content" string.*/
        final String REVIEW_CONTENT_KEY = "content";

        /* Key value for the review "url" string.*/
        final String REVIEW_URL_KEY = "url";

        /* Key value for the "recommendations" object.*/
        final String RECOMMENDATIONS_KEY = "recommendations";

        /* Key value for the "name" string.*/
        final String NAME_KEY = "name";

        /* Admitted crew job - Director.*/
        final String DIRECTING_JOB = "Director";

        /* Base image link.*/
        final String BASE_IMAGE_URL_342 = "http://image.tmdb.org/t/p/w342/";
        final String BASE_IMAGE_URL_500 = "http://image.tmdb.org/t/p/w500/";

        /* Base video thumbnail link.*/
        final String BASE_VIDEO_THUMBNAIL_URL = "//img.youtube.com/vi";

        /* Video thumbnail quality.*/
        final String VIDEO_THUMBNAIL_QUALITY_URL = "mqdefault.jpg";

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(movieJSON)) {
            return null;
        }

        // Create an empty Movie Object so that we can start adding information about it
        Movie movie = new Movie();

        try {
            JSONObject movieObject = new JSONObject(movieJSON);

            // Extract "id"
            if (movieObject.has(ID_KEY)) {
                int id = movieObject.getInt(ID_KEY);
                movie.setId(id);
            }

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
            if (movieObject.has(POSTER_KEY)) {
                String imageUrl = BASE_IMAGE_URL_500 + movieObject.getString(POSTER_KEY);
                movie.setImageUrl(imageUrl);
            }

            // Extract the list of "genres"
            if (movieObject.has(GENRE_KEY)) {
                JSONArray genres = movieObject.getJSONArray(GENRE_KEY);
                // Create an empty StringBuilder to add the genres
                StringBuilder genresList = new StringBuilder();
                // Loop through each feature in the array
                for (int i = 0; i < genres.length(); i++) {
                    // Get genre JSONObject at position i
                    JSONObject genreObject = genres.getJSONObject(i);
                    if (genreObject.has(NAME_KEY)) {
                        genresList.append(genreObject.getString(NAME_KEY));
                        genresList.append(", ");
                    }
                }
                // Delete 2 last characters of the StringBuilder as they are useless separators
                if (genresList.length() > 0) genresList.setLength(genresList.length() - 2);
                movie.setGenres(genresList.toString());
            }

            // Extract the list of "production countries"
            if (movieObject.has(COUNTRY_KEY)) {
                JSONArray countries = movieObject.getJSONArray(COUNTRY_KEY);
                // Create an empty StringBuilder to add the countries
                StringBuilder countriesList = new StringBuilder();
                // Loop through each feature in the array
                for (int i = 0; i < countries.length(); i++) {
                    // Get country JSONObject at position i
                    JSONObject countryObject = countries.getJSONObject(i);
                    if (countryObject.has(COUNTRY_CODE)) {
                        countriesList.append(countryObject.getString(COUNTRY_CODE));
                        countriesList.append(", ");
                    }
                }
                // Delete 2 last characters of the StringBuilder as they are useless separators
                if (countriesList.length() > 0) countriesList.setLength(countriesList.length() - 2);
                movie.setCountries(countriesList.toString());
            }

            // Extract the list of the movie "images"
            if (movieObject.has(IMAGES_KEY)) {
                JSONObject images = movieObject.getJSONObject(IMAGES_KEY);
                if (images.has(BACKDROPS_KEY)) {
                    JSONArray backdrops = images.getJSONArray(BACKDROPS_KEY);
                    if (backdrops.length() > 0) {
                        // Create an empty ArrayList of image paths
                        List<String> imagesList = new ArrayList<>();
                        // Limit the list to 10 images
                        int length;
                        if (backdrops.length() < 10) {
                            length = backdrops.length();
                        } else {
                            length = 10;
                        }
                        // Loop through each feature in the array
                        for (int i = 0; i < length; i++) {
                            // Get image JSONObject at position i
                            JSONObject imageObject = backdrops.getJSONObject(i);
                            if (imageObject.has(IMAGE_PATH_KEY)) {
                                String imageUrl = BASE_IMAGE_URL_342
                                        + imageObject.getString(IMAGE_PATH_KEY);
                                imagesList.add(imageUrl);
                            }
                        }
                        movie.setImagesList(imagesList);
                    }
                }
            }

            // Extract "cast" and "crew" details
            if (movieObject.has(CREDITS_KEY)) {
                JSONObject credits = movieObject.getJSONObject(CREDITS_KEY);
                // Extract the list of "cast" details
                if (credits.has(CAST_KEY)) {
                    JSONArray cast = credits.getJSONArray(CAST_KEY);
                    if (cast.length() > 0) {
                        // Limit the list to 10 person
                        int length;
                        if (cast.length() < 10) {
                            length = cast.length();
                        } else {
                            length = 10;
                        }
                        // Create an empty array of actor/actress names
                        String castNamesList[] = new String[length];
                        // Create an empty array of actor/actress photos urls
                        String castPhotosList[] = new String[length];
                        // Loop through each feature in the array
                        for (int i = 0; i < length; i++) {
                            // Get person JSONObject at position i
                            JSONObject castObject = cast.getJSONObject(i);
                            if (castObject.has(NAME_KEY)) {
                                castNamesList[i] = castObject.getString(NAME_KEY);
                                if (castObject.has(PROFILE_IMAGE_KEY)) {
                                    String imageUrl = BASE_IMAGE_URL_342
                                            + castObject.getString(PROFILE_IMAGE_KEY);
                                    castPhotosList[i] = imageUrl;
                                }
                            }
                        }
                        List<String[]> castList = new ArrayList<>();
                        castList.add(castNamesList);
                        castList.add(castPhotosList);
                        movie.setCast(castList);
                    }
                }
                // Extract the movie directors
                if (credits.has(CREW_KEY)) {
                    JSONArray crew = credits.getJSONArray(CREW_KEY);
                    // Create an empty StringBuilder to add the directors names
                    StringBuilder directors = new StringBuilder();
                    // Loop through each person in the array
                    for (int i = 0; i < crew.length(); i++) {
                        // Get person JSONObject at position i
                        JSONObject crewObject = crew.getJSONObject(i);
                        // Check if it's a director
                        if (crewObject.has(JOB_KEY)) {
                            if (crewObject.getString(JOB_KEY).equals(DIRECTING_JOB)) {
                                // It is a director
                                if (crewObject.has(NAME_KEY)) {
                                    directors.append(crewObject.getString(NAME_KEY));
                                    directors.append(", ");
                                }
                            }
                        }
                    }
                    // Delete 2 last characters of the StringBuilder as they are useless separators
                    if (directors.length() > 0) directors.setLength(directors.length() - 2);
                    movie.setDirector(directors.toString());
                }
            }

            // Extract the list of movie "trailers"
            if (movieObject.has(VIDEOS_KEY)) {
                JSONObject videosObject = movieObject.getJSONObject(VIDEOS_KEY);
                if (videosObject.has(RESULTS_KEY)) {
                    JSONArray videos = videosObject.getJSONArray(RESULTS_KEY);
                    if (videos.length() > 0) {
                        // Limit the list to 10 trailers
                        int length;
                        if (videos.length() < 10) {
                            length = videos.length();
                        } else {
                            length = 10;
                        }
                        // Create an empty array for video urls
                        String videoUrlsList[] = new String[length];
                        // Create an empty array for thumbnail urls
                        String videoThumbnailsList[] = new String[length];
                        // Loop through each feature in the array
                        for (int i = 0; i < length; i++) {
                            // Get video JSONObject at position i
                            JSONObject video = videos.getJSONObject(i);
                            if (video.has(VIDEO_KEY_KEY)) {
                                String key = video.getString(VIDEO_KEY_KEY);
                                videoUrlsList[i] = key;
                                Uri.Builder thumbnailBuilder = new Uri.Builder();
                                thumbnailBuilder.scheme("https").path(BASE_VIDEO_THUMBNAIL_URL)
                                        .appendPath(key)
                                        .appendPath(VIDEO_THUMBNAIL_QUALITY_URL);
                                videoThumbnailsList[i] = thumbnailBuilder.build().toString();
                            }
                        }
                        List<String[]> videosList = new ArrayList<>();
                        videosList.add(videoUrlsList);
                        videosList.add(videoThumbnailsList);
                        movie.setTrailers(videosList);
                    }
                }
            }

            // Extract the list of movie "reviews"
            if (movieObject.has(REVIEWS_KEY)) {
                JSONObject reviewsObject = movieObject.getJSONObject(REVIEWS_KEY);
                if (reviewsObject.has(RESULTS_KEY)) {
                    JSONArray reviews = reviewsObject.getJSONArray(RESULTS_KEY);
                    if (reviews.length() > 0) {
                        // Limit the list to 3 reviews
                        int length;
                        if (reviews.length() < 3) {
                            length = reviews.length();
                        } else {
                            length = 3;
                        }
                        // Create an empty array for reviews contents
                        String contentsList[] = new String[length];
                        // Create an empty array for reviews urls
                        String urlsList[] = new String[length];
                        // Create an empty array for authors of the reviews
                        String authorsList[] = new String[length];
                        // Loop through each review in the array
                        for (int i = 0; i < length; i++) {
                            // Get review JSONObject at position i
                            JSONObject review = reviews.getJSONObject(i);
                            if (review.has(REVIEW_CONTENT_KEY)) {
                                contentsList[i] = review.getString(REVIEW_CONTENT_KEY);
                                if (review.has(REVIEW_URL_KEY)) {
                                    urlsList[i] = review.getString(REVIEW_URL_KEY);
                                }
                                if (review.has(REVIEW_AUTHOR_KEY)) {
                                    authorsList[i] = review.getString(REVIEW_AUTHOR_KEY);
                                }
                            }
                        }
                        List<String[]> reviewsList = new ArrayList<>();
                        reviewsList.add(contentsList);
                        reviewsList.add(urlsList);
                        reviewsList.add(authorsList);
                        movie.setReviews(reviewsList);
                    }
                }
            }

            // Extract the list of recommended movies
            if (movieObject.has(RECOMMENDATIONS_KEY)) {
                JSONObject recommendationsObject = movieObject.getJSONObject(RECOMMENDATIONS_KEY);
                if (recommendationsObject.has(RESULTS_KEY)) {
                    JSONArray recommendations = recommendationsObject.getJSONArray(RESULTS_KEY);
                    if (recommendations.length() > 0) {
                        // Create an empty ArrayList that we can start adding recommendations to
                        List<Movie> recommendedMovies = new ArrayList<>();
                        // Limit the list to 10 movies
                        int length;
                        if (recommendations.length() < 10) {
                            length = recommendations.length();
                        } else {
                            length = 10;
                        }
                        // Loop through each movie in the array
                        for (int i = 0; i < length; i++) {
                            // Create an empty Movie Object to add information about it
                            Movie recommendedMovie = new Movie();
                            // Get movie JSONObject at position i
                            JSONObject recommendation = recommendations.getJSONObject(i);
                            // Extract "id"
                            if (recommendation.has(ID_KEY)) {
                                int id = recommendation.getInt(ID_KEY);
                                recommendedMovie.setId(id);
                            }
                            // Extract "poster image"
                            if (recommendation.has(POSTER_KEY)) {
                                String imageUrl = BASE_IMAGE_URL_342
                                        + recommendation.getString(POSTER_KEY);
                                recommendedMovie.setImageUrl(imageUrl);
                            }
                            // Add movie Object to list of recommendations
                            recommendedMovies.add(recommendedMovie);
                        }
                        movie.setRecommendations(recommendedMovies);
                    }
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }

        // Return the movie object with the detailed information
        return movie;
    }
}
