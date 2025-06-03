package com.example.cp470_proj_oct24_java;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder> {

    private static final String TAG= PhotosAdapter.class.getSimpleName();

    private List<Uri> photos;
    private Context context;

    public PhotosAdapter(Context context, List<Uri> photos) {
        Log.i(TAG,"inside constructor");
        this.context = context;
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG,"inside onCreateViewHolder");
        View view = LayoutInflater.from(context).inflate(R.layout.photo_item_layout, parent, false);

        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Log.i(TAG,"inside onBindViewHolder");
        Uri photoUri = photos.get(position);

        // Set image temporarily
        holder.photoImageView.setImageURI(photoUri);

        // Analyze photo using ML Kit
        PhotoAnalyzer.analyzePhoto(context, photoUri, (isValid, message) -> {
            if (isValid) {
                Log.i("PhotoAnalyzer", "com.example.cp470_proj_oct24_java.Photo valid: " + message);
            } else {
                Log.i("PhotoAnalyzer", "com.example.cp470_proj_oct24_java.Photo invalid: " + message);
                // Remove invalid photo from the list
                photos.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photo_image_view);

        }

    }
}
