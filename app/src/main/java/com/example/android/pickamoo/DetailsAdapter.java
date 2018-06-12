package com.example.android.pickamoo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Cristina on 07/06/2018.
 * This adapter provides access to the movie details items as cast, images or videos, creates views
 * for items, and replaces the content of some of the views with new data when the original item is
 * no longer visible.
 */

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.ViewHolder> {

    private Context mContext;
    private List<?> mList;
    private int mListType;
    static final int IMAGES_TYPE = 0;
    static final int CAST_TYPE = 1;
    static final int TRAILERS_TYPE = 2;
    static final int RECOMMENDATIONS_TYPE = 3;

    /**
     * Custom constructor
     *
     * @param context  is the activity context
     * @param list     is the list of details to populate in the RecyclerView (it could be a
     *                 List<{@link String}> or a List<{@link String[]}>)
     * @param listType is the type of the list (images, cast, or trailers)
     */
    public DetailsAdapter(Context context, List<?> list, int listType) {
        mContext = context;
        mList = list;
        mListType = listType;
    }

    /**
     * Provide a reference to the views for each data item. The ViewHolder is a static
     * class instance which is associated with a view when it's created, caching the child views.
     * If the view already exists, retrieve the holder instance and use its fields instead of
     * calling again findViewById.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image, playButton, castImage;
        final TextView castName;

        public ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.iv_image_item);
            playButton = view.findViewById(R.id.play_button);
            castImage = view.findViewById(R.id.cast_image);
            castName = view.findViewById(R.id.cast_name);
        }
    }

    // Create new views
    @NonNull
    @Override
    public DetailsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view
        View itemView;
        switch (mListType) {
            case IMAGES_TYPE:
            case TRAILERS_TYPE:
            case RECOMMENDATIONS_TYPE:
                itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.image_item, parent, false);
                break;
            case CAST_TYPE:
                itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.cast_item, parent, false);
                break;
            default:
                itemView = new View(mContext);
                Log.e(DetailActivity.LOG_TAG, "Invalid list type");
                break;
        }
        return new ViewHolder(itemView);
    }

    // Replace the contents of a view
    @Override
    public void onBindViewHolder(@NonNull DetailsAdapter.ViewHolder holder, int position) {
        switch (mListType) {
            case IMAGES_TYPE:
                // Get the imageLink to download the image
                String imageLink = (String) mList.get(position);
                // Download the image and attach it to the ImageView
                if (imageLink != null && imageLink.length() > 0) {
                    Picasso.get().load(imageLink).into(holder.image);
                }
                break;
            case TRAILERS_TYPE:
                // Get the imageLink to download the thumbnail
                String[] thumbnailsList = (String[]) mList.get(1);
                String thumbnailLink = thumbnailsList[position];
                // Download the image and attach it to the ImageView
                if (thumbnailLink != null && thumbnailLink.length() > 0) {
                    Picasso.get().load(thumbnailLink).resize(342, 0)
                            .into(holder.image);
                }
                // Make the play button visible
                holder.playButton.setVisibility(View.VISIBLE);
                // Get the video id
                String[] keysList = (String[]) mList.get(0);
                final String id = keysList[position];
                // Handle onClick events
                holder.playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Open the trailer on Youtube App if it's available on the device or else
                        // on navigator.
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("vnd.youtube:" + id));
                            mContext.startActivity(intent);
                        } catch (Exception e) {
                            mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://www.youtube.com/watch?v=" + id)));
                        }
                    }
                });
                break;
            case CAST_TYPE:
                // Get the imageLink to download the profile picture
                String[] picturesList = (String[]) mList.get(1);
                String pictureLink = picturesList[position];
                // Download the image and attach it to the ImageView
                Picasso.get().load(pictureLink)
                        .placeholder(mContext.getResources().getDrawable(R.drawable.ic_profile_image))
                        .into(holder.castImage);
                // Get the name
                String[] namesList = (String[]) mList.get(0);
                holder.castName.setText(namesList[position]);
                break;
            case RECOMMENDATIONS_TYPE:
                final Movie currentMovie = (Movie) mList.get(position);
                // Get the imageLink to download the poster
                String posterLink = currentMovie.getImageUrl();
                if (posterLink != null && posterLink.length() > 0) {
                    Picasso.get().load(posterLink).resize(0, 342)
                            .error(R.drawable.img_placeholder).into(holder.image);
                }
                // Handle onClick events
                holder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent openActivityDetail = new Intent(mContext, DetailActivity.class);
                        openActivityDetail.putExtra("movieId", currentMovie.getId());
                        mContext.startActivity(openActivityDetail);
                    }
                });
                break;
        }

    }

    // Return the size of your data set
    @Override
    public int getItemCount() {
        switch (mListType) {
            case IMAGES_TYPE:
            case RECOMMENDATIONS_TYPE:
                return mList.size();
            case TRAILERS_TYPE:
                String[] keysList = (String[]) mList.get(0);
                return keysList.length;
            case CAST_TYPE:
                String[] namesList = (String[]) mList.get(0);
                return namesList.length;
            default:
                return 0;
        }
    }
}
