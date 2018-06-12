package com.example.android.pickamoo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Cristina on 28/03/2018.
 * This adapter provides access to the movie items in the data set, creates views for
 * items, and replaces the content of some of the views with new data when the original item
 * is no longer visible.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private final Context mContext;
    private List<Movie> mMovies;
    private static MovieAdapterListener mOnClickListener;
    private int mWidth, mHeight;

    // Handle button click
    public interface MovieAdapterListener {
        void OnClick(View v, int position);
    }

    /**
     * Custom constructor
     * @param movies is the list of {@link Movie} objects to be populated in the RecyclerView
     */
    public MovieAdapter(Context context, List<Movie> movies, MovieAdapterListener listener) {
        mContext = context;
        mMovies = movies;
        mOnClickListener = listener;
    }

    /** Provide a reference to the views for each data item. The ViewHolder is a static
     * class instance which is associated with a view when it's created, caching the child views.
     * If the view already exists, retrieve the holder instance and use its fields instead of
     * calling again findViewById.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView moviePoster;

        public ViewHolder(View view) {
            super(view);
            moviePoster = view.findViewById(R.id.iv_poster_grid);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickListener.OnClick(v, getAdapterPosition());
                }
            });
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.movie_grid_item, parent, false);
        // Calculate the image width based on the RecyclerView total width, divided by the column
        // number and minus the margins
        mWidth = parent.getWidth() / parent.getResources().getInteger(R.integer.column_number)
                - (int) parent.getResources().getDimension(R.dimen.smallSeparation) * 4;
        // Calculate the image height as the width multiplied by the image ratio (1.5)
        mHeight = mWidth * 3 / 2;
        return new ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MovieAdapter.ViewHolder holder, int position) {
        Movie currentMovie = mMovies.get(position);

        // Get the imageLink to download the poster image
        String imageLink = currentMovie.getImageUrl();
        // Download the image and attach it to the ImageView. If imageLink is null, no image is
        // assigned or is not recognized as such. Then, a default image is assigned.
        if (imageLink != null && imageLink.length() > 0) {
            Picasso.get().load(imageLink).resize(mWidth, mHeight).error(R.drawable.img_placeholder)
                    .into(holder.moviePoster);
        } else {
            Picasso.get().load(R.drawable.img_placeholder).resize(mWidth, mHeight).into(holder.moviePoster);
        }
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    // Clear the adapter data
    public void clear() {
        int size = mMovies.size();
        mMovies.clear();
        notifyItemRangeRemoved(0, size);
    }

    // Add new data to the adapter's data set
    public void addAll(List<Movie> newList) {
        mMovies.addAll(newList);
        notifyDataSetChanged();
    }
}
