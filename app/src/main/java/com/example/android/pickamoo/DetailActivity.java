package com.example.android.pickamoo;

import android.databinding.DataBindingUtil;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Toast;

import com.example.android.pickamoo.databinding.ActivityDetailBinding;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        if (getIntent() == null && !getIntent().hasExtra("movie")) {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            finish();
        }

        final Movie movie = getIntent().getExtras().getParcelable("movie");

        // Toolbar settings
        Toolbar myToolbar = mBinding.toolbar;
        setSupportActionBar(myToolbar);
        // Add back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Set title
        if (!movie.getTitle().isEmpty()) {
            getSupportActionBar().setTitle(movie.getTitle());
        } else {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set poster image
        // Get the imageLink to download the image
        String imageLink = movie.getImageUrl();
        if (imageLink != null && imageLink.length() > 0) {
            Picasso.get().load(imageLink).into(mBinding.ivPoster);
        } else {
            Picasso.get().load(R.drawable.img_placeholder).into(mBinding.ivPoster);
        }
        // TopCrop image, from: https://stackoverflow.com/questions/6330084/imageview-scaling-top-crop/38049348#38049348
        Matrix matrix = mBinding.ivPoster.getImageMatrix();
        float imageWidth = mBinding.ivPoster.getDrawable().getIntrinsicWidth();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float scaleRatio = screenWidth / imageWidth;
        matrix.postScale(scaleRatio, scaleRatio);
        mBinding.ivPoster.setImageMatrix(matrix);

        // Set vote average
        if (movie.getVoteAverage() == -1.0) {
            mBinding.tvRating.setVisibility(View.GONE);
        } else {
            mBinding.tvRating.setVisibility(View.VISIBLE);
            mBinding.tvRating.setText(String.valueOf(movie.getVoteAverage()));
        }

        // Set release date
        String dateLabel = getString(R.string.date_label);
        String date;
        if (!movie.getReleaseDate().isEmpty()) {
            date = movie.getReleaseDate();
        } else {
            date = getString(R.string.no_info);
        }
        SpannableStringBuilder spannableDate =
                new SpannableStringBuilder(dateLabel + " " + date);
        spannableDate.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                0,dateLabel.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.tvDate.setText(spannableDate);

        // Set synopsis
        String synopsisLabel = getString(R.string.synopsis_label);
        String synopsis;
        if (!movie.getSynopsis().isEmpty()) {
            synopsis = movie.getSynopsis();
        } else {
            synopsis = getString(R.string.no_info);
        }
        SpannableStringBuilder spannableSynopsis =
                new SpannableStringBuilder(synopsisLabel + " " + synopsis);
        spannableSynopsis.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                0,synopsisLabel.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.tvSynopsis.setText(spannableSynopsis);
    }

    // Handle back button on toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
